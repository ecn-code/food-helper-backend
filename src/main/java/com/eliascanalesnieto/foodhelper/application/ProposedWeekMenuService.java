package com.eliascanalesnieto.foodhelper.application;

import com.eliascanalesnieto.foodhelper.domain.NutritionalValues;
import com.eliascanalesnieto.foodhelper.domain.NutritionBasis;
import com.eliascanalesnieto.foodhelper.domain.Product;
import com.eliascanalesnieto.foodhelper.domain.ProductRepository;
import com.eliascanalesnieto.foodhelper.domain.RecipeIngredient;
import com.eliascanalesnieto.foodhelper.domain.Recipe;
import com.eliascanalesnieto.foodhelper.domain.RecipeDerivedProduct;
import com.eliascanalesnieto.foodhelper.domain.RecipeRepository;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenu;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuDay;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuProduct;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuRecipeProduction;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuRepository;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuSection;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuStockRequirement;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuStockSummary;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuStockSummaryCalories;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuStockSummaryDayCalories;
import com.eliascanalesnieto.foodhelper.domain.PlanningSummary;
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
    private final RecipeRepository recipeRepository;
    private final MenuProductResolver menuProductResolver;
    private final StockRepository stockRepository;
    private final CurrentWeekMenuStatsRepository currentWeekMenuStatsRepository;

    @Transactional
    public ProposedWeekMenu create(LocalDate startDate, LocalDate endDate) {
        return create(startDate, endDate, 1);
    }

    @Transactional
    public ProposedWeekMenu create(LocalDate startDate, LocalDate endDate, Integer users) {
        if (endDate.isBefore(startDate)) {
            throw new IllegalArgumentException("End date must be on or after start date");
        }
        if (ChronoUnit.DAYS.between(startDate, endDate) + 1 > MAX_MENU_DAYS) {
            throw new IllegalArgumentException("Planning cannot span more than 16 days");
        }
        int normalizedUsers = normalizeUsers(users);
        return enrich(menuRepository.create(ProposedWeekMenu.builder()
                .users(normalizedUsers)
                .startDate(startDate)
                .endDate(endDate)
                .days(List.of())
                .build()));
    }

    @Transactional(readOnly = true)
    public ProposedWeekMenu findById(Long id) {
        return enrich(menuRepository.findById(id));
    }

    public ProposedWeekMenu withUsers(ProposedWeekMenu menu, Integer users) {
        return enrich(menu.toBuilder()
                .users(normalizeUsers(users))
                .build());
    }

    @Transactional(readOnly = true)
    public List<PlanningSummary> findAllSummaries() {
        return menuRepository.findAllSummaries();
    }

    @Transactional
    public void delete(Long id) {
        menuRepository.delete(id);
    }

    @Transactional
    public ProposedWeekMenu upsertDay(Long menuId, ProposedWeekMenuDay day) {
        ensureMenuIsOpen(menuId);
        validateDayContent(day);
        validateDayParts(day);
        validateProductModes(day);
        validateProductSortOrders(day);
        validateRecipeSortOrders(day);
        ProposedWeekMenuDay completedDay = completeDefaultGrams(day);
        completedDay = completeRecipeProductions(completedDay);
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

    private void ensureNoOverlappingPlanning(LocalDate startDate, LocalDate endDate) {
        boolean overlaps = menuRepository.findAllSummaries().stream()
                .anyMatch(summary -> overlaps(summary.startDate(), summary.endDate(), startDate, endDate));
        if (overlaps) {
            throw new IllegalArgumentException("Planning overlaps an existing menu");
        }
    }

    private void validateDayParts(ProposedWeekMenuDay day) {
        Set<Long> dayPartIds = safeSections(day).stream()
                .map(ProposedWeekMenuSection::getDayPartId)
                .collect(java.util.stream.Collectors.toSet());
        if (dayPartIds.size() != safeSections(day).size()) {
            throw new IllegalArgumentException("Day parts must be unique within a day");
        }
    }

    private void validateProductSortOrders(ProposedWeekMenuDay day) {
        for (ProposedWeekMenuSection section : safeSections(day)) {
            Set<Integer> sortOrders = new HashSet<>();
            for (ProposedWeekMenuProduct product : section.getProducts()) {
                if (!sortOrders.add(product.getSortOrder())) {
                    throw new IllegalArgumentException("Product sortOrder must be unique within each section");
                }
            }
        }
    }

    private void validateRecipeSortOrders(ProposedWeekMenuDay day) {
        Set<Integer> sortOrders = new HashSet<>();
        for (ProposedWeekMenuRecipeProduction recipeProduction : safeRecipeProductions(day)) {
            if (!sortOrders.add(recipeProduction.getSortOrder())) {
                throw new IllegalArgumentException("Recipe production sortOrder must be unique within the day");
            }
        }
    }

    private boolean overlaps(LocalDate firstStart, LocalDate firstEnd, LocalDate secondStart, LocalDate secondEnd) {
        return !firstEnd.isBefore(secondStart) && !firstStart.isAfter(secondEnd);
    }

    private void validateDayContent(ProposedWeekMenuDay day) {
        if (safeSections(day).isEmpty() && safeRecipeProductions(day).isEmpty()) {
            throw new IllegalArgumentException("A planned day must contain at least one section or recipe production");
        }
    }

    private ProposedWeekMenuDay completeDefaultGrams(ProposedWeekMenuDay day) {
        Map<Long, Product> productsById = loadProductsFromSections(safeSections(day));
        List<ProposedWeekMenuSection> sections = safeSections(day).stream()
                .map(section -> section.toBuilder()
                        .products(section.getProducts().stream()
                                .map(product -> product.getProductId() == null
                                        ? completeProduct(product, null)
                                        : completeProduct(product, productsById.get(product.getProductId())))
                                .toList())
                        .build())
                .toList();
        return day.toBuilder().sections(sections).build();
    }

    private ProposedWeekMenuProduct completeProduct(ProposedWeekMenuProduct product, Product linkedProduct) {
        if (product.getProductId() == null) {
            validateManualProductQuantity(product);
            return product.toBuilder()
                    .units(null)
                    .grams(null)
                    .nutritionalValues(scale(product.getNutritionalValues()))
                    .build();
        }
        BigDecimal units = product.getUnits() == null ? DEFAULT_UNITS : product.getUnits();
        BigDecimal grams = product.getGrams() == null
                ? linkedProduct.getGramsPerUnit().multiply(units)
                : product.getGrams();
        return product.toBuilder()
                .productName(product.getProductName() == null ? linkedProduct.getName() : product.getProductName())
                .units(scale(units))
                .grams(scale(grams))
                .nutritionalValues(calculateContribution(linkedProduct, scale(units), scale(grams)))
                .build();
    }

    private ProposedWeekMenuDay completeRecipeProductions(ProposedWeekMenuDay day) {
        List<ProposedWeekMenuRecipeProduction> recipeProductions = safeRecipeProductions(day);
        if (recipeProductions.isEmpty()) {
            return day.toBuilder().recipeProductions(List.of()).build();
        }
        Map<Long, Recipe> recipesById = loadRecipes(recipeProductions);
        Map<Long, Product> productsById = loadProductsFromRecipes(recipesById.values());
        List<ProposedWeekMenuRecipeProduction> completedRecipeProductions = recipeProductions.stream()
                .map(recipeProduction -> completeRecipeProduction(
                        recipeProduction,
                        recipesById.get(recipeProduction.getRecipeId()),
                        productsById
                ))
                .toList();
        return day.toBuilder().recipeProductions(completedRecipeProductions).build();
    }

    private ProposedWeekMenu enrich(ProposedWeekMenu menu) {
        Map<Long, Product> productsById = loadProducts(menu.getDays());
        List<ProposedWeekMenuDay> enrichedDays = menu.getDays().stream()
                .map(day -> enrichDay(day, productsById))
                .toList();
        return menu.toBuilder()
                .days(enrichedDays)
                .nutritionalValues(sumDays(enrichedDays))
                .stockSummary(buildStockSummary(enrichedDays, productsById, menu.getUsers()))
                .build();
    }

    private ProposedWeekMenuDay enrichDay(ProposedWeekMenuDay day, Map<Long, Product> productsById) {
        List<ProposedWeekMenuSection> sections = safeSections(day).stream()
                .map(section -> enrichSection(section, productsById))
                .toList();
        List<ProposedWeekMenuRecipeProduction> recipeProductions = enrichRecipeProductions(day.getRecipeProductions());
        return day.toBuilder()
                .sections(sections)
                .recipeProductions(recipeProductions)
                .nutritionalValues(sumSections(sections))
                .build();
    }

    private ProposedWeekMenuSection enrichSection(ProposedWeekMenuSection section, Map<Long, Product> productsById) {
        List<ProposedWeekMenuProduct> products = section.getProducts().stream()
                .map(product -> product.getProductId() == null
                        ? enrichProduct(product, null)
                        : enrichProduct(product, productsById.get(product.getProductId())))
                .toList();
        return section.toBuilder()
                .products(products)
                .nutritionalValues(sumProducts(products))
                .build();
    }

    private ProposedWeekMenuProduct enrichProduct(ProposedWeekMenuProduct menuProduct, Product product) {
        if (menuProduct.getProductId() == null) {
            return menuProduct.toBuilder()
                    .units(null)
                    .grams(null)
                    .nutritionalValues(scale(menuProduct.getNutritionalValues()))
                    .build();
        }
        BigDecimal units = menuProduct.getUnits() == null ? DEFAULT_UNITS : menuProduct.getUnits();
        BigDecimal grams = menuProduct.getGrams() == null
                ? product.getGramsPerUnit().multiply(units)
                : menuProduct.getGrams();
        return menuProduct.toBuilder()
                .productName(menuProduct.getProductName() == null ? product.getName() : menuProduct.getProductName())
                .units(scale(units))
                .grams(scale(grams))
                .nutritionalValues(calculateContribution(product, scale(units), scale(grams)))
                .build();
    }

    private Map<Long, Product> loadProducts(List<ProposedWeekMenuDay> days) {
        List<ProposedWeekMenuSection> sections = days.stream()
                .flatMap(day -> safeSections(day).stream())
                .toList();
        return loadProductsFromSections(sections);
    }

    private Map<Long, Product> loadProductsFromProducts(List<ProposedWeekMenuProduct> products) {
        List<Long> ids = products.stream()
                .map(ProposedWeekMenuProduct::getProductId)
                .filter(java.util.Objects::nonNull)
                .distinct()
                .toList();
        return menuProductResolver.loadByIds(ids, "One or more products were not found");
    }

    private Map<Long, Product> loadProductsFromSections(List<ProposedWeekMenuSection> sections) {
        return loadProductsFromProducts(sections.stream()
                .flatMap(section -> section.getProducts().stream())
                .toList());
    }

    private Map<Long, Product> loadProductsFromRecipes(Iterable<Recipe> recipes) {
        List<Long> productIds = new ArrayList<>();
        for (Recipe recipe : recipes) {
            RecipeDerivedProduct derivedProduct = recipe.getDerivedProduct();
            if (derivedProduct == null) {
                throw new IllegalArgumentException("Recipe must have a derived product");
            }
            productIds.add(derivedProduct.getProductId());
        }
        List<Long> uniqueProductIds = productIds.stream().distinct().toList();
        return menuProductResolver.loadByIds(uniqueProductIds, "Derived product not found for recipe production");
    }

    private Map<Long, Product> loadCompositionProducts(Map<Long, Product> productsById) {
        return menuProductResolver.loadCompositionProducts(productsById, "One or more ingredient products were not found");
    }

    private void accumulateCompositionRequirements(
            Map<Long, StockRequirementAccumulator> requirements,
            Map<Long, Product> productsById,
            Product linkedProduct,
            BigDecimal requiredUnits
    ) {
        RecipeDerivedProduct derivedProduct = linkedProduct.getDerivedProduct();
        if (derivedProduct == null || derivedProduct.getIngredients() == null || derivedProduct.getIngredients().isEmpty()) {
            StockRequirementAccumulator accumulator = requirements.computeIfAbsent(
                    linkedProduct.getId(),
                    productId -> new StockRequirementAccumulator(productId, linkedProduct.getName())
            );
            accumulator.requiredUnits = accumulator.requiredUnits.add(requiredUnits);
            return;
        }

        for (RecipeIngredient ingredient : derivedProduct.getIngredients()) {
            Product ingredientProduct = productsById.get(ingredient.getProductId());
            if (ingredientProduct == null) {
                throw new ResourceNotFoundException("One or more ingredient products were not found");
            }
            BigDecimal ingredientRequiredUnits = scale(requiredUnits.multiply(normalizeIngredientUnits(ingredient)));
            StockRequirementAccumulator accumulator = requirements.computeIfAbsent(
                    ingredientProduct.getId(),
                    productId -> new StockRequirementAccumulator(productId, ingredientProduct.getName())
            );
            accumulator.requiredUnits = accumulator.requiredUnits.add(ingredientRequiredUnits);
        }
    }

    private BigDecimal normalizeIngredientUnits(RecipeIngredient ingredient) {
        BigDecimal quantity = scale(ingredient.getQuantity());
        return quantity;
    }

    private boolean usesCompositionStock(Product product) {
        return menuProductResolver.usesCompositionStock(product);
    }

    private Map<Long, Recipe> loadRecipes(List<ProposedWeekMenuRecipeProduction> recipeProductions) {
        Map<Long, Recipe> recipesById = new LinkedHashMap<>();
        for (Long recipeId : recipeProductions.stream().map(ProposedWeekMenuRecipeProduction::getRecipeId).distinct().toList()) {
            Recipe recipe = recipeRepository.findById(recipeId);
            if (recipe.getDerivedProduct() == null) {
                throw new IllegalArgumentException("Recipe must have a derived product");
            }
            recipesById.put(recipeId, recipe);
        }
        return recipesById;
    }

    private List<ProposedWeekMenuRecipeProduction> enrichRecipeProductions(List<ProposedWeekMenuRecipeProduction> recipeProductions) {
        if (recipeProductions == null || recipeProductions.isEmpty()) {
            return List.of();
        }
        Map<Long, Recipe> recipesById = loadRecipes(recipeProductions);
        Map<Long, Product> productsById = loadProductsFromRecipes(recipesById.values());
        return recipeProductions.stream()
                .map(recipeProduction -> completeRecipeProduction(
                        recipeProduction,
                        recipesById.get(recipeProduction.getRecipeId()),
                        productsById
                ))
                .toList();
    }

    private ProposedWeekMenuRecipeProduction completeRecipeProduction(
            ProposedWeekMenuRecipeProduction recipeProduction,
            Recipe recipe,
            Map<Long, Product> productsById
    ) {
        RecipeDerivedProduct derivedProduct = recipe.getDerivedProduct();
        Product product = productsById.get(derivedProduct.getProductId());
        if (product == null) {
            throw new ResourceNotFoundException("Derived product not found for recipe production");
        }
        return recipeProduction.toBuilder()
                .recipeName(recipe.getName())
                .productId(product.getId())
                .productName(product.getName())
                .units(scale(recipeProduction.getUnits()))
                .build();
    }

    private ProposedWeekMenuStockSummary buildStockSummary(List<ProposedWeekMenuDay> days, Map<Long, Product> productsById, Integer users) {
        BigDecimal userMultiplier = BigDecimal.valueOf(normalizeUsers(users));
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
        Map<Long, Product> compositionProductsById = loadCompositionProducts(productsById);
        Map<Long, Product> productsForStock = new LinkedHashMap<>(productsById);
        productsForStock.putAll(compositionProductsById);

        for (ProposedWeekMenuDay day : days) {
            BigDecimal calories = normalize(day.getNutritionalValues().getCalories());
            totalCalories = totalCalories.add(calories);
            dayCalories.add(ProposedWeekMenuStockSummaryDayCalories.builder()
                    .date(day.getDate())
                    .calories(calories)
                    .build());

            for (ProposedWeekMenuSection section : day.getSections()) {
                for (ProposedWeekMenuProduct product : section.getProducts()) {
                    if (product.getProductId() == null) {
                        continue;
                    }
                    Product linkedProduct = productsById.get(product.getProductId());
                    BigDecimal requiredUnits = scale(normalizeRequiredUnits(product, linkedProduct).multiply(userMultiplier));
                    if (usesCompositionStock(linkedProduct)) {
                        accumulateCompositionRequirements(requirements, productsForStock, linkedProduct, requiredUnits);
                    } else {
                        StockRequirementAccumulator accumulator = requirements.computeIfAbsent(
                                product.getProductId(),
                                productId -> new StockRequirementAccumulator(productId, linkedProduct.getName())
                        );
                        accumulator.requiredUnits = accumulator.requiredUnits.add(requiredUnits);
                    }
                }
            }
        }

        Map<Long, List<StockEntry>> stockByProduct = stockRepository.findStock(null, null).stream()
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

    private NutritionalValues calculateContribution(Product product, BigDecimal units, BigDecimal grams) {
        if (product.getNutritionBasis() == NutritionBasis.PER_UNIT) {
            return NutritionalValues.builder()
                    .calories(scale(product.getNutritionalValues().getCalories().multiply(units)))
                    .carbohydrates(scale(product.getNutritionalValues().getCarbohydrates().multiply(units)))
                    .proteins(scale(product.getNutritionalValues().getProteins().multiply(units)))
                    .fats(scale(product.getNutritionalValues().getFats().multiply(units)))
                    .build();
        }
        return calculateContribution(product.getNutritionalValues(), grams);
    }

    private NutritionalValues scale(NutritionalValues nutritionalValues) {
        if (nutritionalValues == null) {
            return null;
        }
        return NutritionalValues.builder()
                .productId(nutritionalValues.getProductId())
                .calories(nutritionalValues.getCalories() == null ? null : scale(nutritionalValues.getCalories()))
                .carbohydrates(nutritionalValues.getCarbohydrates() == null ? null : scale(nutritionalValues.getCarbohydrates()))
                .proteins(nutritionalValues.getProteins() == null ? null : scale(nutritionalValues.getProteins()))
                .fats(nutritionalValues.getFats() == null ? null : scale(nutritionalValues.getFats()))
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

    private int normalizeUsers(Integer users) {
        return users == null || users < 1 ? 1 : users;
    }

    private void validateProductModes(ProposedWeekMenuDay day) {
        for (ProposedWeekMenuSection section : safeSections(day)) {
            for (ProposedWeekMenuProduct product : section.getProducts()) {
                boolean linkedProduct = product.getProductId() != null;
                if (linkedProduct) {
                    if (product.getProductName() != null || product.getNutritionalValues() != null) {
                        throw new IllegalArgumentException("Linked products must not include manual product data");
                    }
                    continue;
                }

                if (product.getProductName() == null || product.getProductName().isBlank()) {
                    throw new IllegalArgumentException("Manual products require a product name");
                }
                if (product.getUnits() != null || product.getGrams() != null) {
                    throw new IllegalArgumentException("Manual products must not include units or grams");
                }
                if (product.getNutritionalValues() == null
                        || product.getNutritionalValues().getCalories() == null
                        || product.getNutritionalValues().getCarbohydrates() == null
                        || product.getNutritionalValues().getProteins() == null
                        || product.getNutritionalValues().getFats() == null) {
                    throw new IllegalArgumentException("Manual products require absolute calories, carbohydrates, proteins, and fats values");
                }
            }
        }
    }

    private void validateManualProductQuantity(ProposedWeekMenuProduct product) {
        if (product.getUnits() != null || product.getGrams() != null) {
            throw new IllegalArgumentException("Manual products must not include units or grams");
        }
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

    private List<ProposedWeekMenuSection> safeSections(ProposedWeekMenuDay day) {
        return day.getSections() == null ? List.of() : day.getSections();
    }

    private List<ProposedWeekMenuRecipeProduction> safeRecipeProductions(ProposedWeekMenuDay day) {
        return day.getRecipeProductions() == null ? List.of() : day.getRecipeProductions();
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
            applyDirectStock(stockEntries);
        }

        private void applyStock(Product product, Map<Long, Product> productsById, Map<Long, List<StockEntry>> stockByProduct) {
            List<StockEntry> directStockEntries = stockByProduct.getOrDefault(productId, List.of());
            if (usesCompositionStock(product) && directStockEntries.isEmpty()) {
                applyCompositionStock(product, productsById, stockByProduct);
                return;
            }
            applyDirectStock(directStockEntries);
        }

        private void applyDirectStock(List<StockEntry> stockEntries) {
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

        private void applyCompositionStock(Product product, Map<Long, Product> productsById, Map<Long, List<StockEntry>> stockByProduct) {
            RecipeDerivedProduct derivedProduct = product.getDerivedProduct();
            if (derivedProduct == null || derivedProduct.getIngredients() == null || derivedProduct.getIngredients().isEmpty()) {
                applyDirectStock(List.of());
                return;
            }

            BigDecimal availableProductUnits = null;
            for (RecipeIngredient ingredient : derivedProduct.getIngredients()) {
                BigDecimal ingredientQuantity = normalize(ingredient.getQuantity());
                if (ingredientQuantity.signum() <= 0) {
                    continue;
                }
                Product ingredientProduct = productsById.get(ingredient.getProductId());
                BigDecimal availableIngredientQuantity = stockByProduct.getOrDefault(ingredient.getProductId(), List.of()).stream()
                        .map(stockEntry -> toIngredientBasisQuantity(stockEntry, ingredientProduct, ingredient))
                        .reduce(BigDecimal.ZERO, BigDecimal::add);
                BigDecimal unitsFromIngredient = scale(availableIngredientQuantity.divide(ingredientQuantity, SCALE, RoundingMode.HALF_UP));
                availableProductUnits = availableProductUnits == null ? unitsFromIngredient : availableProductUnits.min(unitsFromIngredient);
            }

            if (availableProductUnits == null) {
                missingUnits = requiredUnits;
                return;
            }

            availableUnits = availableProductUnits;
            coveredUnits = requiredUnits.min(availableProductUnits);

            for (RecipeIngredient ingredient : derivedProduct.getIngredients()) {
                BigDecimal ingredientQuantity = normalize(ingredient.getQuantity());
                if (ingredientQuantity.signum() <= 0) {
                    continue;
                }
                Product ingredientProduct = productsById.get(ingredient.getProductId());
                BigDecimal quantityToConsume = coveredUnits.multiply(ingredientQuantity);
                estimatedCost = estimatedCost.add(costForQuantity(
                        stockByProduct.getOrDefault(ingredient.getProductId(), List.of()),
                        quantityToConsume,
                        ingredientProduct,
                        ingredient
                ));
            }
            missingUnits = requiredUnits.subtract(coveredUnits);
        }

        private boolean usesCompositionStock(Product product) {
            RecipeDerivedProduct derivedProduct = product == null ? null : product.getDerivedProduct();
            return derivedProduct != null
                    && derivedProduct.isStockFromComposition()
                    && derivedProduct.getIngredients() != null
                    && !derivedProduct.getIngredients().isEmpty();
        }

        private BigDecimal costForQuantity(List<StockEntry> stockEntries, BigDecimal quantityToConsume, Product ingredientProduct, RecipeIngredient ingredient) {
            BigDecimal remaining = quantityToConsume;
            BigDecimal cost = BigDecimal.ZERO;
            for (StockEntry stockEntry : stockEntries) {
                if (remaining.signum() <= 0) {
                    break;
                }
                BigDecimal available = toIngredientBasisQuantity(stockEntry, ingredientProduct, ingredient);
                BigDecimal consumed = available.min(remaining);
                if (consumed.signum() <= 0) {
                    continue;
                }
                BigDecimal consumedStockUnits = toStockUnits(consumed, ingredientProduct, ingredient);
                cost = cost.add(consumedStockUnits.multiply(scale(stockEntry.getPrice())));
                remaining = remaining.subtract(consumed);
            }
            return cost;
        }

        private BigDecimal toIngredientBasisQuantity(StockEntry stockEntry, Product ingredientProduct, RecipeIngredient ingredient) {
            BigDecimal stockUnits = scale(stockEntry.getQuantity());
            if (ingredient.getQuantityType() == com.eliascanalesnieto.foodhelper.domain.QuantityType.UNITS) {
                return stockUnits;
            }
            BigDecimal gramsPerUnit = ingredientProduct == null || ingredientProduct.getGramsPerUnit() == null
                    ? BigDecimal.ONE
                    : ingredientProduct.getGramsPerUnit();
            return stockUnits.multiply(gramsPerUnit);
        }

        private BigDecimal toStockUnits(BigDecimal ingredientBasisQuantity, Product ingredientProduct, RecipeIngredient ingredient) {
            if (ingredient.getQuantityType() == com.eliascanalesnieto.foodhelper.domain.QuantityType.UNITS) {
                return ingredientBasisQuantity;
            }
            BigDecimal gramsPerUnit = ingredientProduct == null || ingredientProduct.getGramsPerUnit() == null
                    ? BigDecimal.ONE
                    : ingredientProduct.getGramsPerUnit();
            return ingredientBasisQuantity.divide(gramsPerUnit, SCALE, RoundingMode.HALF_UP);
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
