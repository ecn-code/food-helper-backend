package com.eliascanalesnieto.foodhelper.application;

import com.eliascanalesnieto.foodhelper.domain.Product;
import com.eliascanalesnieto.foodhelper.domain.NutritionBasis;
import com.eliascanalesnieto.foodhelper.domain.ProductRepository;
import com.eliascanalesnieto.foodhelper.domain.QuantityType;
import com.eliascanalesnieto.foodhelper.domain.Recipe;
import com.eliascanalesnieto.foodhelper.domain.RecipeDerivedProduct;
import com.eliascanalesnieto.foodhelper.domain.RecipeRepository;
import com.eliascanalesnieto.foodhelper.domain.RecipeIngredient;
import com.eliascanalesnieto.foodhelper.domain.StockEntry;
import com.eliascanalesnieto.foodhelper.domain.StockRepository;
import com.eliascanalesnieto.foodhelper.presentation.ProductStatsExpirationResponse;
import com.eliascanalesnieto.foodhelper.presentation.ProductStatsMetricResponse;
import com.eliascanalesnieto.foodhelper.presentation.ProductStatsResponse;
import com.eliascanalesnieto.foodhelper.presentation.ProductStatsSummaryResponse;
import com.eliascanalesnieto.foodhelper.presentation.ProductStatsTotalsResponse;
import com.eliascanalesnieto.foodhelper.presentation.RecipeStatsResponse;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class StatsService {
    private static final BigDecimal ZERO = new BigDecimal("0.00");
    private static final String NO_DATA = "Sin datos";
    private static final String NO_LOTS = "Sin lotes";
    private static final String NO_EXPIRATION = "Sin caducidad";

    private final ProductRepository productRepository;
    private final StockRepository stockRepository;
    private final RecipeRepository recipeRepository;

    @Transactional(readOnly = true)
    public ProductStatsResponse getProductStats() {
        List<Product> products = productRepository.findAll().stream()
                .map(this::attachDerivedProduct)
                .toList();
        Map<Long, Product> productsById = products.stream()
                .collect(Collectors.toMap(Product::getId, product -> product, (left, right) -> left, LinkedHashMap::new));
        List<StockEntry> stockEntries = expandDerivedProductStock(stockRepository.findStock(null, null), productsById);
        Map<Long, ProductStockAccumulator> stockByProduct = initializeProductAccumulators(products);

        for (StockEntry stockEntry : stockEntries) {
            ProductStockAccumulator accumulator = stockByProduct.get(stockEntry.getProductId());
            if (accumulator == null) {
                continue;
            }
            accumulator.totalQuantity = accumulator.totalQuantity.add(stockEntry.getQuantity());
            accumulator.batchCount++;
            if (stockEntry.getExpirationDate() != null && accumulator.nextExpirationDate == null) {
                accumulator.nextExpirationDate = stockEntry.getExpirationDate();
            }
        }

        return new ProductStatsResponse(
                topMetric(products, product -> product.getNutritionalValues().getCalories()),
                topMetric(products, product -> product.getNutritionalValues().getCarbohydrates()),
                topMetric(products, product -> product.getNutritionalValues().getProteins()),
                topMetric(products, product -> product.getNutritionalValues().getFats()),
                new ProductStatsTotalsResponse(
                        stockEntries.stream()
                                .map(StockEntry::getQuantity)
                                .reduce(BigDecimal.ZERO, BigDecimal::add)
                                .setScale(2, RoundingMode.HALF_UP),
                        stockEntries.size()
                ),
                earliestExpiration(stockEntries),
                productSummaries(products, stockByProduct)
        );
    }

    @Transactional(readOnly = true)
    public RecipeStatsResponse getRecipeStats() {
        List<Recipe> recipes = recipeRepository.findAll();
        if (recipes.isEmpty()) {
            return new RecipeStatsResponse(0, ZERO, 0, 0);
        }

        BigDecimal totalCalories = BigDecimal.ZERO;
        long totalIngredients = 0;
        long recipesWithDerivedProduct = 0;

        for (Recipe recipe : recipes) {
            totalCalories = totalCalories.add(recipeCalories(recipe));
            totalIngredients += recipe.getIngredients().size();
            if (recipe.getDerivedProduct() != null) {
                recipesWithDerivedProduct++;
            }
        }

        return new RecipeStatsResponse(
                recipes.size(),
                totalCalories.divide(BigDecimal.valueOf(recipes.size()), 2, RoundingMode.HALF_UP),
                totalIngredients,
                recipesWithDerivedProduct
        );
    }

    private BigDecimal recipeCalories(Recipe recipe) {
        if (recipe.getNutritionalValues() != null && recipe.getNutritionalValues().getCalories() != null) {
            return recipe.getNutritionalValues().getCalories();
        }
        List<Long> productIds = recipe.getIngredients().stream()
                .map(RecipeIngredient::getProductId)
                .distinct()
                .toList();
        Map<Long, Product> productsById = productRepository.findByIds(productIds).stream()
                .collect(java.util.stream.Collectors.toMap(Product::getId, product -> product));
        BigDecimal totalCalories = BigDecimal.ZERO;
        for (RecipeIngredient ingredient : recipe.getIngredients()) {
            Product product = productsById.get(ingredient.getProductId());
            if (product == null || product.getNutritionalValues() == null || product.getNutritionalValues().getCalories() == null) {
                continue;
            }
            totalCalories = totalCalories.add(calculateContribution(product, ingredient));
        }
        return totalCalories;
    }

    private ProductStatsMetricResponse topMetric(List<Product> products, java.util.function.Function<Product, BigDecimal> metricExtractor) {
        if (products.isEmpty()) {
            return new ProductStatsMetricResponse(null, NO_DATA, ZERO, NO_DATA);
        }

        Product topProduct = products.stream()
                .sorted(Comparator
                        .comparing((Product product) -> normalize(metricExtractor.apply(product))).reversed()
                        .thenComparing(Product::getId))
                .findFirst()
                .orElseThrow();

        return new ProductStatsMetricResponse(
                topProduct.getId(),
                topProduct.getName(),
                normalize(metricExtractor.apply(topProduct)),
                null
        );
    }

    private ProductStatsExpirationResponse earliestExpiration(List<StockEntry> stockEntries) {
        if (stockEntries.isEmpty()) {
            return new ProductStatsExpirationResponse(null, NO_LOTS, null, null, NO_LOTS);
        }

        StockEntry firstExpiringEntry = stockEntries.stream()
                .filter(stockEntry -> stockEntry.getExpirationDate() != null)
                .min(expiringEntryComparator())
                .orElse(null);

        if (firstExpiringEntry == null) {
            return new ProductStatsExpirationResponse(null, NO_EXPIRATION, null, null, NO_EXPIRATION);
        }

        return new ProductStatsExpirationResponse(
                firstExpiringEntry.getProductId(),
                firstExpiringEntry.getProductName(),
                firstExpiringEntry.getQuantity().setScale(2, RoundingMode.HALF_UP),
                firstExpiringEntry.getExpirationDate(),
                null
        );
    }

    private List<ProductStatsSummaryResponse> productSummaries(List<Product> products, Map<Long, ProductStockAccumulator> stockByProduct) {
        List<ProductStatsSummaryResponse> summaries = new ArrayList<>(products.size());
        for (Product product : products) {
            ProductStockAccumulator accumulator = stockByProduct.get(product.getId());
            String nextExpirationMessage = null;
            LocalDate nextExpirationDate = null;
            if (accumulator.batchCount == 0) {
                nextExpirationMessage = NO_LOTS;
            } else if (accumulator.nextExpirationDate == null) {
                nextExpirationMessage = NO_EXPIRATION;
            } else {
                nextExpirationDate = accumulator.nextExpirationDate;
            }
            summaries.add(new ProductStatsSummaryResponse(
                    product.getId(),
                    product.getName(),
                    accumulator.totalQuantity.setScale(2, RoundingMode.HALF_UP),
                    accumulator.batchCount,
                    nextExpirationDate,
                    nextExpirationMessage
            ));
        }
        return summaries;
    }

    private Map<Long, ProductStockAccumulator> initializeProductAccumulators(List<Product> products) {
        Map<Long, ProductStockAccumulator> accumulators = new LinkedHashMap<>();
        for (Product product : products) {
            accumulators.put(product.getId(), new ProductStockAccumulator());
        }
        return accumulators;
    }

    private Product attachDerivedProduct(Product product) {
        return product.toBuilder()
                .derivedProduct(recipeRepository.findDerivedProductByProductId(product.getId()).orElse(null))
                .build();
    }

    private List<StockEntry> expandDerivedProductStock(List<StockEntry> stockEntries, Map<Long, Product> productsById) {
        List<StockEntry> expandedStockEntries = new ArrayList<>();
        for (StockEntry stockEntry : stockEntries) {
            Product product = productsById.get(stockEntry.getProductId());
            if (product == null
                    || product.getDerivedProduct() == null
                    || !product.getDerivedProduct().isStockFromComposition()
                    || product.getDerivedProduct().getIngredients() == null
                    || product.getDerivedProduct().getIngredients().isEmpty()) {
                expandedStockEntries.add(stockEntry);
                continue;
            }
            expandedStockEntries.addAll(expandDerivedProductStockEntry(stockEntry, product.getDerivedProduct()));
        }
        return expandedStockEntries;
    }

    private List<StockEntry> expandDerivedProductStockEntry(StockEntry stockEntry, RecipeDerivedProduct derivedProduct) {
        List<StockEntry> expandedStockEntries = new ArrayList<>(derivedProduct.getIngredients().size());
        for (RecipeIngredient ingredient : derivedProduct.getIngredients()) {
            expandedStockEntries.add(StockEntry.builder()
                    .id(stockEntry.getId())
                    .productId(ingredient.getProductId())
                    .productName(ingredient.getProductName())
                    .quantity(stockEntry.getQuantity().multiply(ingredient.getQuantity()).setScale(2, RoundingMode.HALF_UP))
                    .price(stockEntry.getPrice())
                    .expirationDate(stockEntry.getExpirationDate())
                    .entryDate(stockEntry.getEntryDate())
                    .build());
        }
        return expandedStockEntries;
    }

    private Comparator<StockEntry> expiringEntryComparator() {
        return Comparator
                .comparing(StockEntry::getExpirationDate)
                .thenComparing(StockEntry::getEntryDate)
                .thenComparing(StockEntry::getId);
    }

    private BigDecimal normalize(BigDecimal value) {
        if (value == null) {
            return ZERO;
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateValue(BigDecimal perHundredGramsValue, BigDecimal grams) {
        return perHundredGramsValue.multiply(grams).divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
    }

    private BigDecimal calculateContribution(Product product, RecipeIngredient ingredient) {
        if (ingredient.getQuantityType() == QuantityType.UNITS || product.getNutritionBasis() == NutritionBasis.PER_UNIT) {
            return product.getNutritionalValues().getCalories().multiply(ingredient.getQuantity());
        }
        return calculateValue(product.getNutritionalValues().getCalories(), ingredient.getQuantity());
    }

    private static final class ProductStockAccumulator {
        private BigDecimal totalQuantity = BigDecimal.ZERO;
        private long batchCount;
        private LocalDate nextExpirationDate;
    }
}
