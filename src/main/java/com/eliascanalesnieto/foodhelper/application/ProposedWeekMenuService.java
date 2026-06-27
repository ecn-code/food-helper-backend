package com.eliascanalesnieto.foodhelper.application;

import com.eliascanalesnieto.foodhelper.domain.NutritionalValues;
import com.eliascanalesnieto.foodhelper.domain.Product;
import com.eliascanalesnieto.foodhelper.domain.ProductRepository;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenu;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuDay;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuProduct;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuRepository;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuSection;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuStockRequirement;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuStockSummary;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuStockSummaryCalories;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuStockSummaryDayCalories;
import com.eliascanalesnieto.foodhelper.domain.CurrentWeekMenuStatsRepository;
import com.eliascanalesnieto.foodhelper.domain.StockEntry;
import com.eliascanalesnieto.foodhelper.domain.StockRepository;
import com.eliascanalesnieto.foodhelper.presentation.error.ResourceNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.HashSet;
import java.util.Set;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProposedWeekMenuService {
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private static final BigDecimal DEFAULT_UNITS = BigDecimal.ONE;
    private static final int SCALE = 2;
    private static final long MAX_MENU_DAYS = 16;

    private final ProposedWeekMenuRepository menuRepository;
    private final ProductRepository productRepository;
    private final StockRepository stockRepository;
    private final CurrentWeekMenuStatsRepository currentWeekMenuStatsRepository;

    @Transactional
    public ProposedWeekMenu create(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date must be on or after start date");
        }
        if (ChronoUnit.DAYS.between(startDate, endDate) + 1 > MAX_MENU_DAYS) {
            throw new IllegalArgumentException("Planning cannot span more than 16 days");
        }
        return enrich(menuRepository.create(ProposedWeekMenu.builder()
                .startDate(startDate)
                .endDate(endDate)
                .days(List.of())
                .build()));
    }

    @Transactional(readOnly = true)
    public ProposedWeekMenu findById(Long id) {
        return enrich(menuRepository.findById(id));
    }

    @Transactional
    public ProposedWeekMenu upsertDay(Long menuId, ProposedWeekMenuDay day) {
        ensureMenuIsOpen(menuId);
        validateDayParts(day);
        validateProductSortOrders(day);
        ProposedWeekMenuDay completedDay = completeDefaultGrams(day);
        return enrich(menuRepository.upsertDay(menuId, completedDay));
    }

    private void ensureMenuIsOpen(Long menuId) {
        try {
            currentWeekMenuStatsRepository.findByProposedWeekMenuId(menuId);
            throw new IllegalArgumentException("Planning is already closed");
        } catch (ResourceNotFoundException ignored) {
            // Planning is still open for edits or has not been closed yet.
        }
    }

    private void validateDayParts(ProposedWeekMenuDay day) {
        Set<Long> dayPartIds = day.getSections().stream()
                .map(ProposedWeekMenuSection::getDayPartId)
                .collect(java.util.stream.Collectors.toSet());
        if (dayPartIds.size() != day.getSections().size()) {
            throw new IllegalArgumentException("Day parts must be unique within a day");
        }
    }

    private void validateProductSortOrders(ProposedWeekMenuDay day) {
        for (ProposedWeekMenuSection section : day.getSections()) {
            Set<Integer> sortOrders = new HashSet<>();
            for (ProposedWeekMenuProduct product : section.getProducts()) {
                if (!sortOrders.add(product.getSortOrder())) {
                    throw new IllegalArgumentException("Product sortOrder must be unique within each section");
                }
            }
        }
    }

    private ProposedWeekMenuDay completeDefaultGrams(ProposedWeekMenuDay day) {
        Map<Long, Product> productsById = loadProductsFromSections(day.getSections());
        List<ProposedWeekMenuSection> sections = day.getSections().stream()
                .map(section -> section.toBuilder()
                        .products(section.getProducts().stream()
                                .map(product -> {
                                    Product linkedProduct = productsById.get(product.getProductId());
                                    BigDecimal units = product.getUnits() == null ? DEFAULT_UNITS : product.getUnits();
                                    BigDecimal grams = product.getGrams() == null
                                            ? linkedProduct.getGramsPerUnit().multiply(units)
                                            : product.getGrams();
                                    return product.toBuilder()
                                            .units(scale(units))
                                            .grams(scale(grams))
                                            .build();
                                })
                                .toList())
                        .build())
                .toList();
        return day.toBuilder().sections(sections).build();
    }

    private ProposedWeekMenu enrich(ProposedWeekMenu menu) {
        Map<Long, Product> productsById = loadProducts(menu.getDays());
        List<ProposedWeekMenuDay> enrichedDays = menu.getDays().stream()
                .map(day -> enrichDay(day, productsById))
                .toList();
        return menu.toBuilder()
                .days(enrichedDays)
                .nutritionalValues(sumDays(enrichedDays))
                .stockSummary(buildStockSummary(enrichedDays, productsById))
                .build();
    }

    private ProposedWeekMenuDay enrichDay(ProposedWeekMenuDay day, Map<Long, Product> productsById) {
        List<ProposedWeekMenuSection> sections = day.getSections().stream()
                .map(section -> enrichSection(section, productsById))
                .toList();
        return day.toBuilder()
                .sections(sections)
                .nutritionalValues(sumSections(sections))
                .build();
    }

    private ProposedWeekMenuSection enrichSection(ProposedWeekMenuSection section, Map<Long, Product> productsById) {
        List<ProposedWeekMenuProduct> products = section.getProducts().stream()
                .map(product -> enrichProduct(product, productsById.get(product.getProductId())))
                .toList();
        return section.toBuilder()
                .products(products)
                .nutritionalValues(sumProducts(products))
                .build();
    }

    private ProposedWeekMenuProduct enrichProduct(ProposedWeekMenuProduct menuProduct, Product product) {
        return menuProduct.toBuilder()
                .productName(product.getName())
                .units(scale(menuProduct.getUnits()))
                .grams(scale(menuProduct.getGrams()))
                .nutritionalValues(calculateContribution(product.getNutritionalValues(), menuProduct.getGrams()))
                .build();
    }

    private Map<Long, Product> loadProducts(List<ProposedWeekMenuDay> days) {
        List<ProposedWeekMenuSection> sections = days.stream()
                .flatMap(day -> day.getSections().stream())
                .toList();
        return loadProductsFromSections(sections);
    }

    private Map<Long, Product> loadProductsFromProducts(List<ProposedWeekMenuProduct> products) {
        List<Long> ids = products.stream()
                .map(ProposedWeekMenuProduct::getProductId)
                .distinct()
                .toList();
        List<Product> loadedProducts = productRepository.findByIds(ids);
        if (loadedProducts.size() != ids.size()) {
            throw new ResourceNotFoundException("One or more products were not found");
        }
        Map<Long, Product> productsById = new LinkedHashMap<>();
        loadedProducts.forEach(product -> productsById.put(product.getId(), product));
        return productsById;
    }

    private Map<Long, Product> loadProductsFromSections(List<ProposedWeekMenuSection> sections) {
        return loadProductsFromProducts(sections.stream()
                .flatMap(section -> section.getProducts().stream())
                .toList());
    }

    private ProposedWeekMenuStockSummary buildStockSummary(List<ProposedWeekMenuDay> days, Map<Long, Product> productsById) {
        if (days.isEmpty()) {
            return ProposedWeekMenuStockSummary.builder()
                    .plannedDays(0)
                    .distinctProducts(0)
                    .calories(ProposedWeekMenuStockSummaryCalories.builder()
                            .averagePerPlannedDay(ZERO)
                            .build())
                    .estimatedCost(ZERO)
                    .requirements(List.of())
                    .build();
        }

        Map<Long, StockRequirementAccumulator> requirements = new LinkedHashMap<>();
        List<ProposedWeekMenuStockSummaryDayCalories> dayCalories = new ArrayList<>(days.size());
        BigDecimal totalCalories = BigDecimal.ZERO;

        for (ProposedWeekMenuDay day : days) {
            BigDecimal calories = normalize(day.getNutritionalValues().getCalories());
            totalCalories = totalCalories.add(calories);
            dayCalories.add(ProposedWeekMenuStockSummaryDayCalories.builder()
                    .date(day.getDate())
                    .calories(calories)
                    .build());

            for (ProposedWeekMenuSection section : day.getSections()) {
                for (ProposedWeekMenuProduct product : section.getProducts()) {
                    Product linkedProduct = productsById.get(product.getProductId());
                    BigDecimal requiredUnits = normalizeRequiredUnits(product, linkedProduct);
                    StockRequirementAccumulator accumulator = requirements.computeIfAbsent(
                            product.getProductId(),
                            productId -> new StockRequirementAccumulator(productId, linkedProduct.getName())
                    );
                    accumulator.requiredUnits = accumulator.requiredUnits.add(requiredUnits);
                }
            }
        }

        Map<Long, List<StockEntry>> stockByProduct = stockRepository.findStock(null, requirements.keySet()).stream()
                .collect(Collectors.groupingBy(
                        StockEntry::getProductId,
                        LinkedHashMap::new,
                        Collectors.toList()
                ));

        BigDecimal estimatedCost = BigDecimal.ZERO;
        for (StockRequirementAccumulator accumulator : requirements.values()) {
            accumulator.applyStock(stockByProduct.getOrDefault(accumulator.productId, List.of()));
            estimatedCost = estimatedCost.add(accumulator.estimatedCost);
        }

        return ProposedWeekMenuStockSummary.builder()
                .plannedDays(days.size())
                .distinctProducts(requirements.size())
                .calories(ProposedWeekMenuStockSummaryCalories.builder()
                        .averagePerPlannedDay(scale(totalCalories.divide(BigDecimal.valueOf(days.size()), SCALE, RoundingMode.HALF_UP)))
                        .maxDay(dayCalories.stream().max(dayCaloriesComparator()).orElse(null))
                        .minDay(dayCalories.stream().min(dayCaloriesComparator()).orElse(null))
                        .build())
                .estimatedCost(scale(estimatedCost))
                .requirements(requirements.values().stream()
                        .map(StockRequirementAccumulator::toDomain)
                        .toList())
                .build();
    }

    private NutritionalValues calculateContribution(NutritionalValues nutritionalValues, BigDecimal grams) {
        return NutritionalValues.builder()
                .calories(scale(calculateValue(nutritionalValues.getCalories(), grams)))
                .carbohydrates(scale(calculateValue(nutritionalValues.getCarbohydrates(), grams)))
                .proteins(scale(calculateValue(nutritionalValues.getProteins(), grams)))
                .fats(scale(calculateValue(nutritionalValues.getFats(), grams)))
                .build();
    }

    private NutritionalValues sumDays(List<ProposedWeekMenuDay> days) {
        return sum(days.stream().map(ProposedWeekMenuDay::getNutritionalValues).toList());
    }

    private NutritionalValues sumSections(List<ProposedWeekMenuSection> sections) {
        return sum(sections.stream().map(ProposedWeekMenuSection::getNutritionalValues).toList());
    }

    private NutritionalValues sumProducts(List<ProposedWeekMenuProduct> products) {
        return sum(products.stream().map(ProposedWeekMenuProduct::getNutritionalValues).toList());
    }

    private NutritionalValues sum(List<NutritionalValues> values) {
        BigDecimal calories = BigDecimal.ZERO;
        BigDecimal carbohydrates = BigDecimal.ZERO;
        BigDecimal proteins = BigDecimal.ZERO;
        BigDecimal fats = BigDecimal.ZERO;
        for (NutritionalValues value : values) {
            calories = calories.add(value.getCalories());
            carbohydrates = carbohydrates.add(value.getCarbohydrates());
            proteins = proteins.add(value.getProteins());
            fats = fats.add(value.getFats());
        }
        return NutritionalValues.builder()
                .calories(scale(calories))
                .carbohydrates(scale(carbohydrates))
                .proteins(scale(proteins))
                .fats(scale(fats))
                .build();
    }

    private BigDecimal calculateValue(BigDecimal perHundredGramsValue, BigDecimal grams) {
        return perHundredGramsValue.multiply(grams).divide(ONE_HUNDRED, SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal normalizeRequiredUnits(ProposedWeekMenuProduct product, Product linkedProduct) {
        if (product.getUnits() != null) {
            return scale(product.getUnits());
        }
        if (product.getGrams() != null) {
            return scale(product.getGrams().divide(linkedProduct.getGramsPerUnit(), SCALE, RoundingMode.HALF_UP));
        }
        return DEFAULT_UNITS;
    }

    private Comparator<ProposedWeekMenuStockSummaryDayCalories> dayCaloriesComparator() {
        return Comparator
                .comparing(ProposedWeekMenuStockSummaryDayCalories::getCalories)
                .thenComparing(ProposedWeekMenuStockSummaryDayCalories::getDate);
    }

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(SCALE, RoundingMode.HALF_UP);
    }

    private BigDecimal normalize(BigDecimal value) {
        return value == null ? ZERO : scale(value);
    }

    private final class StockRequirementAccumulator {
        private final Long productId;
        private final String productName;
        private BigDecimal requiredUnits = BigDecimal.ZERO;
        private BigDecimal availableUnits = BigDecimal.ZERO;
        private BigDecimal coveredUnits = BigDecimal.ZERO;
        private BigDecimal missingUnits = BigDecimal.ZERO;
        private BigDecimal estimatedCost = BigDecimal.ZERO;

        private StockRequirementAccumulator(Long productId, String productName) {
            this.productId = productId;
            this.productName = productName;
        }

        private void applyStock(List<StockEntry> stockEntries) {
            BigDecimal remainingRequiredUnits = requiredUnits;
            for (StockEntry stockEntry : stockEntries) {
                BigDecimal quantity = scale(stockEntry.getQuantity());
                availableUnits = availableUnits.add(quantity);
                if (remainingRequiredUnits.signum() <= 0) {
                    continue;
                }
                BigDecimal coveredByEntry = quantity.min(remainingRequiredUnits);
                coveredUnits = coveredUnits.add(coveredByEntry);
                estimatedCost = estimatedCost.add(coveredByEntry.multiply(scale(stockEntry.getPrice())));
                remainingRequiredUnits = remainingRequiredUnits.subtract(coveredByEntry);
            }
            missingUnits = requiredUnits.subtract(coveredUnits);
        }

        private ProposedWeekMenuStockRequirement toDomain() {
            return ProposedWeekMenuStockRequirement.builder()
                    .productId(productId)
                    .productName(productName)
                    .requiredUnits(scale(requiredUnits))
                    .availableUnits(scale(availableUnits))
                    .coveredUnits(scale(coveredUnits))
                    .missingUnits(scale(missingUnits))
                    .estimatedCost(scale(estimatedCost))
                    .build();
        }
    }
}
