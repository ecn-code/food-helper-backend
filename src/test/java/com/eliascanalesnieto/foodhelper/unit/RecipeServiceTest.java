package com.eliascanalesnieto.foodhelper.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.eliascanalesnieto.foodhelper.application.MediaService;
import com.eliascanalesnieto.foodhelper.application.RecipeService;
import com.eliascanalesnieto.foodhelper.domain.NutritionalValues;
import com.eliascanalesnieto.foodhelper.domain.NutritionBasis;
import com.eliascanalesnieto.foodhelper.domain.Product;
import com.eliascanalesnieto.foodhelper.domain.ProductRepository;
import com.eliascanalesnieto.foodhelper.domain.QuantityType;
import com.eliascanalesnieto.foodhelper.domain.Recipe;
import com.eliascanalesnieto.foodhelper.domain.RecipeDerivedProduct;
import com.eliascanalesnieto.foodhelper.domain.RecipeIngredient;
import com.eliascanalesnieto.foodhelper.domain.RecipeRepository;
import com.eliascanalesnieto.foodhelper.presentation.error.DuplicateResourceException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class RecipeServiceTest {

    @Mock
    private RecipeRepository recipeRepository;

    @Mock
    private ProductRepository productRepository;

    @Mock
    private MediaService mediaService;

    @InjectMocks
    private RecipeService service;

    @Test
    void shouldCalculateNutritionalTotalsFromAssignedGrams() {
        when(productRepository.findByIds(List.of(1L, 2L))).thenReturn(List.of(
                product(1L, "Chicken", "Chicken breast", "165", "0", "31", "3.6"),
                product(2L, "Rice", "White rice", "130", "28", "2.7", "0.3")
        ));
        when(recipeRepository.create(any(Recipe.class))).thenAnswer(invocation -> {
            Recipe recipe = invocation.getArgument(0);
            return recipe.toBuilder().id(10L).build();
        });

        Recipe created = service.create(
                "Chicken rice",
                "Chicken with rice",
                "Cook and mix.",
                null,
                List.of(
                        ingredient(1L, "150"),
                        ingredient(2L, "200")
                ),
                null
        );

        assertThat(created.getId()).isEqualTo(10L);
        assertThat(created.getNutritionalValues().getCalories()).isEqualByComparingTo("507.50");
        assertThat(created.getNutritionalValues().getCarbohydrates()).isEqualByComparingTo("56.00");
        assertThat(created.getNutritionalValues().getProteins()).isEqualByComparingTo("51.90");
        assertThat(created.getNutritionalValues().getFats()).isEqualByComparingTo("6.00");
        assertThat(created.getIngredients()).hasSize(2);
        assertThat(created.getIngredients().getFirst().getProductName()).isEqualTo("Chicken");
    }

    @Test
    void shouldSyncDerivedProductWhenRecipeIsUpdated() {
        when(productRepository.findByIds(List.of(1L))).thenReturn(List.of(
                product(1L, "Chicken", "Chicken breast", "165", "0", "31", "3.6")
        ));
        when(recipeRepository.findById(7L)).thenReturn(Recipe.builder()
                .id(7L)
                .name("Chicken prep")
                .description("Original description")
                .instructions("Original instructions")
                .ingredients(List.of(ingredient(1L, "200")))
                .build());
        when(recipeRepository.update(eq(7L), any(Recipe.class))).thenAnswer(invocation -> {
            Recipe recipe = invocation.getArgument(1);
            return recipe.toBuilder().id(7L).build();
        });
        when(recipeRepository.findDerivedProductByRecipeId(7L))
                .thenReturn(Optional.of(RecipeDerivedProduct.builder()
                        .productId(15L)
                        .name("Chicken mix")
                        .unitsProduced(new BigDecimal("4"))
                        .stockFromComposition(true)
                        .ingredients(List.of(ingredient(1L, "50")))
                        .build()))
                .thenReturn(Optional.of(RecipeDerivedProduct.builder()
                        .productId(15L)
                        .name("Chicken mix")
                        .unitsProduced(new BigDecimal("4"))
                        .stockFromComposition(true)
                        .ingredients(List.of(ingredient(1L, "50")))
                        .build()));
        when(productRepository.findById(15L)).thenReturn(product(15L, "Chicken mix", "Chicken", "165", "0", "31", "3.6"));

        Recipe updated = service.update(
                7L,
                "Chicken prep revised",
                "Updated description",
                "Updated instructions",
                null,
                null,
                List.of(ingredient(1L, "250")),
                null
        );

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).update(eq(15L), productCaptor.capture());
        assertThat(productCaptor.getValue().getName()).isEqualTo("Chicken mix");
        assertThat(productCaptor.getValue().getNutritionBasis()).isEqualTo(NutritionBasis.PER_UNIT);
        assertThat(productCaptor.getValue().getNutritionalValues().getCalories()).isEqualByComparingTo("103.13");
        assertThat(updated.getDerivedProduct()).isNotNull();
        assertThat(updated.getDerivedProduct().getUnitsProduced()).isEqualByComparingTo("4.00");
        assertThat(updated.getDerivedProduct().isStockFromComposition()).isTrue();
    }

    @Test
    void shouldRejectSecondDerivedProductCreation() {
        when(recipeRepository.findDerivedProductByRecipeId(3L))
                .thenReturn(Optional.of(RecipeDerivedProduct.builder()
                        .productId(8L)
                        .unitsProduced(new BigDecimal("3"))
                        .stockFromComposition(true)
                        .ingredients(List.of(ingredient(1L, "100")))
                        .build()));

        assertThatThrownBy(() -> service.createDerivedProduct(3L, "Soup base", new BigDecimal("3"), true))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Recipe already has a derived product");
    }

    @Test
    void shouldCreateDerivedProductWithSelfStockModeWhenRequested() {
        when(recipeRepository.findDerivedProductByRecipeId(3L)).thenReturn(Optional.empty());
        when(productRepository.findByName("Soup base")).thenReturn(Optional.empty());
        when(recipeRepository.findById(3L)).thenReturn(Recipe.builder()
                .id(3L)
                .name("Soup")
                .description("Soup description")
                .instructions("Cook")
                .ingredients(List.of(ingredient(1L, "200")))
                .nutritionalValues(NutritionalValues.builder()
                        .productId(3L)
                        .calories(new BigDecimal("100"))
                        .carbohydrates(BigDecimal.ZERO)
                        .proteins(BigDecimal.ZERO)
                        .fats(BigDecimal.ZERO)
                        .build())
                .build());
        when(productRepository.findByIds(List.of(1L))).thenReturn(List.of(product(1L, "Chicken", "Chicken breast", "165", "0", "31", "3.6")));
        when(productRepository.create(any(Product.class))).thenAnswer(invocation -> {
            Product product = invocation.getArgument(0);
            return product.toBuilder().id(9L).build();
        });
        when(recipeRepository.saveDerivedProduct(eq(3L), eq(9L), eq("Soup base"), any(BigDecimal.class), eq(false), anyList()))
                .thenAnswer(invocation -> RecipeDerivedProduct.builder()
                        .productId(9L)
                        .name("Soup base")
                        .unitsProduced(invocation.getArgument(3))
                        .stockFromComposition(false)
                        .ingredients(invocation.getArgument(5))
                        .build());

        RecipeDerivedProduct created = service.createDerivedProduct(3L, "Soup base", new BigDecimal("3"), false);

        assertThat(created.isStockFromComposition()).isFalse();
    }

    private RecipeIngredient ingredient(Long productId, String grams) {
        return RecipeIngredient.builder()
                .productId(productId)
                .quantity(new BigDecimal(grams))
                .quantityType(QuantityType.GRAMS)
                .build();
    }

    private Product product(Long id, String name, String description, String calories, String carbohydrates, String proteins, String fats) {
        return Product.builder()
                .id(id)
                .name(name)
                .description(description)
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
}
