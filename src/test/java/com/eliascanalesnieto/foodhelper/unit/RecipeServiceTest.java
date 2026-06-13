package com.eliascanalesnieto.foodhelper.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.eliascanalesnieto.foodhelper.application.MediaService;
import com.eliascanalesnieto.foodhelper.application.RecipeService;
import com.eliascanalesnieto.foodhelper.domain.NutritionalValues;
import com.eliascanalesnieto.foodhelper.domain.Product;
import com.eliascanalesnieto.foodhelper.domain.ProductRepository;
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
                        .producedGrams(new BigDecimal("400"))
                        .gramsPerUnit(new BigDecimal("100"))
                        .build()))
                .thenReturn(Optional.of(RecipeDerivedProduct.builder()
                        .productId(15L)
                        .producedGrams(new BigDecimal("400"))
                        .gramsPerUnit(new BigDecimal("100"))
                        .build()));

        Recipe updated = service.update(
                7L,
                "Chicken prep",
                "Updated description",
                "Updated instructions",
                List.of(ingredient(1L, "250")),
                null
        );

        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).update(eq(15L), productCaptor.capture());
        assertThat(productCaptor.getValue().getNutritionalValues().getCalories()).isEqualByComparingTo("412.50");
        assertThat(updated.getDerivedProduct()).isNotNull();
        assertThat(updated.getDerivedProduct().getUnitsProduced()).isEqualByComparingTo("4.00");
    }

    @Test
    void shouldRejectSecondDerivedProductCreation() {
        when(recipeRepository.findDerivedProductByRecipeId(3L))
                .thenReturn(Optional.of(RecipeDerivedProduct.builder()
                        .productId(8L)
                        .producedGrams(new BigDecimal("300"))
                        .gramsPerUnit(new BigDecimal("100"))
                        .build()));

        assertThatThrownBy(() -> service.createDerivedProduct(3L, new BigDecimal("300"), new BigDecimal("100")))
                .isInstanceOf(DuplicateResourceException.class)
                .hasMessage("Recipe already has a derived product");
    }

    private RecipeIngredient ingredient(Long productId, String grams) {
        return RecipeIngredient.builder()
                .productId(productId)
                .grams(new BigDecimal(grams))
                .build();
    }

    private Product product(Long id, String name, String description, String calories, String carbohydrates, String proteins, String fats) {
        return Product.builder()
                .id(id)
                .name(name)
                .description(description)
                .gramsPerUnit(new BigDecimal("100"))
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
