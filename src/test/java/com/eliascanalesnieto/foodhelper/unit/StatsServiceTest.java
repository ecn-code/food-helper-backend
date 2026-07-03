package com.eliascanalesnieto.foodhelper.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.when;

import com.eliascanalesnieto.foodhelper.application.StatsService;
import com.eliascanalesnieto.foodhelper.domain.NutritionalValues;
import com.eliascanalesnieto.foodhelper.domain.NutritionBasis;
import com.eliascanalesnieto.foodhelper.domain.Product;
import com.eliascanalesnieto.foodhelper.domain.ProductRepository;
import com.eliascanalesnieto.foodhelper.domain.QuantityType;
import com.eliascanalesnieto.foodhelper.domain.Recipe;
import com.eliascanalesnieto.foodhelper.domain.RecipeDerivedProduct;
import com.eliascanalesnieto.foodhelper.domain.RecipeIngredient;
import com.eliascanalesnieto.foodhelper.domain.RecipeRepository;
import com.eliascanalesnieto.foodhelper.domain.StockEntry;
import com.eliascanalesnieto.foodhelper.domain.StockRepository;
import com.eliascanalesnieto.foodhelper.presentation.ProductStatsExpirationResponse;
import com.eliascanalesnieto.foodhelper.presentation.ProductStatsResponse;
import com.eliascanalesnieto.foodhelper.presentation.ProductStatsSummaryResponse;
import com.eliascanalesnieto.foodhelper.presentation.RecipeStatsResponse;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class StatsServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private StockRepository stockRepository;

    @Mock
    private RecipeRepository recipeRepository;

    @InjectMocks
    private StatsService service;

    @BeforeEach
    void setUp() {
        lenient().when(recipeRepository.findDerivedProductByProductId(org.mockito.ArgumentMatchers.any()))
                .thenReturn(Optional.empty());
    }

    @Test
    void shouldReturnStableDefaultsWhenCatalogIsEmpty() {
        when(productRepository.findAll()).thenReturn(List.of());
        when(stockRepository.findStock(null, null)).thenReturn(List.of());
        when(recipeRepository.findAll()).thenReturn(List.of());

        ProductStatsResponse productStats = service.getProductStats();
        RecipeStatsResponse recipeStats = service.getRecipeStats();

        assertThat(productStats.caloriesTop().productId()).isNull();
        assertThat(productStats.caloriesTop().productName()).isEqualTo("Sin datos");
        assertThat(productStats.caloriesTop().value()).isEqualByComparingTo("0.00");
        assertThat(productStats.caloriesTop().message()).isEqualTo("Sin datos");
        assertThat(productStats.carbohydratesTop().productId()).isNull();
        assertThat(productStats.carbohydratesTop().productName()).isEqualTo("Sin datos");
        assertThat(productStats.proteinsTop().productName()).isEqualTo("Sin datos");
        assertThat(productStats.fatsTop().value()).isEqualByComparingTo("0.00");
        assertThat(productStats.stock().totalQuantity()).isEqualByComparingTo("0.00");
        assertThat(productStats.stock().batchCount()).isZero();
        assertThat(productStats.earliestExpiration()).isEqualTo(new ProductStatsExpirationResponse(null, "Sin lotes", null, null, "Sin lotes"));
        assertThat(productStats.summaries()).isEmpty();

        assertThat(recipeStats).isEqualTo(new RecipeStatsResponse(0, new BigDecimal("0.00"), 0, 0));
    }

    @Test
    void shouldAggregateProductAndRecipeStatsDeterministically() {
        Product chicken = product(2L, "Chicken", "165", "0", "31", "3.6");
        Product rice = product(1L, "Rice", "165", "28", "2.7", "0.3");
        Product beans = product(3L, "Beans", "120", "22", "8", "1.2");
        when(productRepository.findAll()).thenReturn(List.of(chicken, rice, beans));
        when(stockRepository.findStock(null, null)).thenReturn(List.of(
                stockEntry(11L, 2L, "Chicken", "5.00", LocalDate.of(2026, 6, 10), LocalDate.of(2026, 6, 1)),
                stockEntry(7L, 1L, "Rice", "3.50", LocalDate.of(2026, 6, 10), LocalDate.of(2026, 5, 31)),
                stockEntry(13L, 3L, "Beans", "2.00", null, LocalDate.of(2026, 6, 2))
        ));
        when(recipeRepository.findAll()).thenReturn(List.of(
                recipe(1L, "Meal one", "100.00", 2, false),
                recipe(2L, "Meal two", "200.00", 3, true)
        ));

        ProductStatsResponse productStats = service.getProductStats();
        RecipeStatsResponse recipeStats = service.getRecipeStats();

        assertThat(productStats.caloriesTop().productId()).isEqualTo(1L);
        assertThat(productStats.caloriesTop().productName()).isEqualTo("Rice");
        assertThat(productStats.caloriesTop().value()).isEqualByComparingTo("165.00");
        assertThat(productStats.caloriesTop().message()).isNull();
        assertThat(productStats.carbohydratesTop().productId()).isEqualTo(1L);
        assertThat(productStats.carbohydratesTop().productName()).isEqualTo("Rice");
        assertThat(productStats.carbohydratesTop().value()).isEqualByComparingTo("28.00");
        assertThat(productStats.proteinsTop().productId()).isEqualTo(2L);
        assertThat(productStats.proteinsTop().productName()).isEqualTo("Chicken");
        assertThat(productStats.proteinsTop().value()).isEqualByComparingTo("31.00");
        assertThat(productStats.fatsTop().productId()).isEqualTo(2L);
        assertThat(productStats.fatsTop().productName()).isEqualTo("Chicken");
        assertThat(productStats.fatsTop().value()).isEqualByComparingTo("3.60");
        assertThat(productStats.stock().totalQuantity()).isEqualByComparingTo("10.50");
        assertThat(productStats.stock().batchCount()).isEqualTo(3);
        assertThat(productStats.earliestExpiration()).isEqualTo(new ProductStatsExpirationResponse(1L, "Rice", new BigDecimal("3.50"), LocalDate.of(2026, 6, 10), null));
        assertThat(productStats.summaries()).containsExactly(
                new ProductStatsSummaryResponse(2L, "Chicken", new BigDecimal("5.00"), 1, LocalDate.of(2026, 6, 10), null),
                new ProductStatsSummaryResponse(1L, "Rice", new BigDecimal("3.50"), 1, LocalDate.of(2026, 6, 10), null),
                new ProductStatsSummaryResponse(3L, "Beans", new BigDecimal("2.00"), 1, null, "Sin caducidad")
        );

        assertThat(recipeStats.activeRecipes()).isEqualTo(2);
        assertThat(recipeStats.averageCalories()).isEqualByComparingTo("150.00");
        assertThat(recipeStats.totalIngredients()).isEqualTo(5);
        assertThat(recipeStats.recipesWithDerivedProduct()).isEqualTo(1);
    }

    @Test
    void shouldHandleSingleProductAndSingleRecipe() {
        Product chicken = product(1L, "Chicken", "165", "0", "31", "3.6");
        when(productRepository.findAll()).thenReturn(List.of(chicken));
        when(stockRepository.findStock(null, null)).thenReturn(List.of(
                stockEntry(1L, 1L, "Chicken", "2.00", null, LocalDate.of(2026, 6, 1))
        ));
        when(recipeRepository.findAll()).thenReturn(List.of(
                recipe(1L, "Meal one", "165.00", 1, false)
        ));

        ProductStatsResponse productStats = service.getProductStats();
        RecipeStatsResponse recipeStats = service.getRecipeStats();

        assertThat(productStats.caloriesTop().productName()).isEqualTo("Chicken");
        assertThat(productStats.stock().totalQuantity()).isEqualByComparingTo("2.00");
        assertThat(productStats.stock().batchCount()).isEqualTo(1);
        assertThat(productStats.earliestExpiration().message()).isEqualTo("Sin caducidad");
        assertThat(productStats.summaries()).hasSize(1);
        assertThat(productStats.summaries().getFirst().nextExpirationMessage()).isEqualTo("Sin caducidad");

        assertThat(recipeStats.activeRecipes()).isEqualTo(1);
        assertThat(recipeStats.averageCalories()).isEqualByComparingTo("165.00");
        assertThat(recipeStats.totalIngredients()).isEqualTo(1);
        assertThat(recipeStats.recipesWithDerivedProduct()).isZero();
    }

    @Test
    void shouldExpandDerivedProductStockIntoItsIngredientProducts() {
        Product chicken = product(1L, "Chicken", "165", "0", "31", "3.6");
        Product rice = product(2L, "Rice", "130", "28", "2.7", "0.3");
        RecipeDerivedProduct derivedProduct = RecipeDerivedProduct.builder()
                .productId(3L)
                .name("Curry base")
                .unitsProduced(new BigDecimal("2.00"))
                .stockFromComposition(true)
                .ingredients(List.of(
                        RecipeIngredient.builder()
                                .productId(1L)
                                .productName("Chicken")
                                .quantity(new BigDecimal("100.00"))
                                .quantityType(QuantityType.GRAMS)
                                .build(),
                        RecipeIngredient.builder()
                                .productId(2L)
                                .productName("Rice")
                                .quantity(new BigDecimal("50.00"))
                                .quantityType(QuantityType.GRAMS)
                                .build()
                ))
                .build();
        Product curryBase = product(3L, "Curry base", "290", "14", "16", "12");

        when(productRepository.findAll()).thenReturn(List.of(chicken, rice, curryBase));
        when(recipeRepository.findDerivedProductByProductId(3L)).thenReturn(Optional.of(derivedProduct));
        when(stockRepository.findStock(null, null)).thenReturn(List.of(
                stockEntry(10L, 3L, "Curry base", "2.00", LocalDate.of(2026, 6, 20), LocalDate.of(2026, 6, 1))
        ));

        ProductStatsResponse productStats = service.getProductStats();

        assertThat(productStats.stock().totalQuantity()).isEqualByComparingTo("300.00");
        assertThat(productStats.stock().batchCount()).isEqualTo(2);
        assertThat(productStats.earliestExpiration()).isEqualTo(new ProductStatsExpirationResponse(1L, "Chicken", new BigDecimal("200.00"), LocalDate.of(2026, 6, 20), null));
        assertThat(productStats.summaries()).containsExactly(
                new ProductStatsSummaryResponse(1L, "Chicken", new BigDecimal("200.00"), 1, LocalDate.of(2026, 6, 20), null),
                new ProductStatsSummaryResponse(2L, "Rice", new BigDecimal("100.00"), 1, LocalDate.of(2026, 6, 20), null),
                new ProductStatsSummaryResponse(3L, "Curry base", new BigDecimal("0.00"), 0, null, "Sin lotes")
        );
    }

    @Test
    void shouldKeepDerivedProductStockAsSelfWhenCompositionModeIsDisabled() {
        Product chicken = product(1L, "Chicken", "165", "0", "31", "3.6");
        Product rice = product(2L, "Rice", "130", "28", "2.7", "0.3");
        RecipeDerivedProduct derivedProduct = RecipeDerivedProduct.builder()
                .productId(3L)
                .name("Curry base")
                .unitsProduced(new BigDecimal("2.00"))
                .stockFromComposition(false)
                .ingredients(List.of(
                        RecipeIngredient.builder()
                                .productId(1L)
                                .productName("Chicken")
                                .quantity(new BigDecimal("100.00"))
                                .quantityType(QuantityType.GRAMS)
                                .build(),
                        RecipeIngredient.builder()
                                .productId(2L)
                                .productName("Rice")
                                .quantity(new BigDecimal("50.00"))
                                .quantityType(QuantityType.GRAMS)
                                .build()
                ))
                .build();
        Product curryBase = product(3L, "Curry base", "290", "14", "16", "12");

        when(productRepository.findAll()).thenReturn(List.of(chicken, rice, curryBase));
        when(recipeRepository.findDerivedProductByProductId(3L)).thenReturn(Optional.of(derivedProduct));
        when(stockRepository.findStock(null, null)).thenReturn(List.of(
                stockEntry(10L, 3L, "Curry base", "2.00", LocalDate.of(2026, 6, 20), LocalDate.of(2026, 6, 1))
        ));

        ProductStatsResponse productStats = service.getProductStats();

        assertThat(productStats.stock().totalQuantity()).isEqualByComparingTo("2.00");
        assertThat(productStats.stock().batchCount()).isEqualTo(1);
        assertThat(productStats.earliestExpiration()).isEqualTo(new ProductStatsExpirationResponse(3L, "Curry base", new BigDecimal("2.00"), LocalDate.of(2026, 6, 20), null));
        assertThat(productStats.summaries()).containsExactly(
                new ProductStatsSummaryResponse(1L, "Chicken", new BigDecimal("0.00"), 0, null, "Sin lotes"),
                new ProductStatsSummaryResponse(2L, "Rice", new BigDecimal("0.00"), 0, null, "Sin lotes"),
                new ProductStatsSummaryResponse(3L, "Curry base", new BigDecimal("2.00"), 1, LocalDate.of(2026, 6, 20), null)
        );
    }

    private Product product(Long id, String name, String calories, String carbohydrates, String proteins, String fats) {
        return Product.builder()
                .id(id)
                .name(name)
                .description(name + " description")
                .gramsPerUnit(new BigDecimal("100"))
                .nutritionBasis(NutritionBasis.PER_100_GRAMS)
                .nutritionalValues(NutritionalValues.builder()
                        .productId(id)
                        .calories(new BigDecimal(calories))
                        .carbohydrates(new BigDecimal(carbohydrates))
                        .proteins(new BigDecimal(proteins))
                        .fats(new BigDecimal(fats))
                        .build())
                .build();
    }

    private StockEntry stockEntry(Long id, Long productId, String productName, String quantity, LocalDate expirationDate, LocalDate entryDate) {
        return StockEntry.builder()
                .id(id)
                .productId(productId)
                .productName(productName)
                .quantity(new BigDecimal(quantity))
                .expirationDate(expirationDate)
                .entryDate(entryDate)
                .build();
    }

    private Recipe recipe(Long id, String name, String calories, int ingredients, boolean derivedProduct) {
        return Recipe.builder()
                .id(id)
                .name(name)
                .description(name + " description")
                .instructions("Cook")
                .ingredients(java.util.stream.IntStream.range(0, ingredients)
                        .mapToObj(index -> RecipeIngredient.builder()
                                .productId((long) index + 1)
                                .quantity(new BigDecimal("100"))
                                .quantityType(QuantityType.GRAMS)
                                .build())
                        .toList())
                .nutritionalValues(NutritionalValues.builder()
                        .productId(id)
                        .calories(new BigDecimal(calories))
                        .carbohydrates(BigDecimal.ZERO)
                        .proteins(BigDecimal.ZERO)
                        .fats(BigDecimal.ZERO)
                        .build())
                .derivedProduct(derivedProduct ? RecipeDerivedProduct.builder()
                        .productId(id)
                        .unitsProduced(new BigDecimal("4.00"))
                        .stockFromComposition(true)
                        .ingredients(List.of())
                        .build() : null)
                .build();
    }
}
