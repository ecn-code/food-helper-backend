package com.eliascanalesnieto.foodhelper.application;

import com.eliascanalesnieto.foodhelper.domain.NutritionalValues;
import com.eliascanalesnieto.foodhelper.domain.Product;
import com.eliascanalesnieto.foodhelper.domain.ProductRepository;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenu;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuDay;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuProduct;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuRepository;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuSection;
import com.eliascanalesnieto.foodhelper.presentation.error.ResourceNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProposedWeekMenuService {
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");
    private static final BigDecimal DEFAULT_UNITS = BigDecimal.ONE;
    private static final int SCALE = 2;

    private final ProposedWeekMenuRepository menuRepository;
    private final ProductRepository productRepository;

    @Transactional
    public ProposedWeekMenu create(LocalDate startDate, LocalDate endDate) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date must be on or after start date");
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
        ProposedWeekMenuDay completedDay = completeDefaultGrams(day);
        return enrich(menuRepository.upsertDay(menuId, completedDay));
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

    private BigDecimal scale(BigDecimal value) {
        return value.setScale(SCALE, RoundingMode.HALF_UP);
    }
}
