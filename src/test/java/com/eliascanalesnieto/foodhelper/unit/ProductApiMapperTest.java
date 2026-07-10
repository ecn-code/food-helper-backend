package com.eliascanalesnieto.foodhelper.unit;

import static org.assertj.core.api.Assertions.assertThat;

import com.eliascanalesnieto.foodhelper.domain.NutritionalValues;
import com.eliascanalesnieto.foodhelper.domain.NutritionBasis;
import com.eliascanalesnieto.foodhelper.domain.Product;
import com.eliascanalesnieto.foodhelper.domain.QuantityType;
import com.eliascanalesnieto.foodhelper.domain.Recipe;
import com.eliascanalesnieto.foodhelper.domain.RecipeDerivedProduct;
import com.eliascanalesnieto.foodhelper.domain.RecipeIngredient;
import com.eliascanalesnieto.foodhelper.presentation.ProductApiMapper;
import com.eliascanalesnieto.foodhelper.presentation.ProductResponse;
import com.eliascanalesnieto.foodhelper.presentation.RecipeResponse;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.mapstruct.factory.Mappers;

class ProductApiMapperTest {

    private final ProductApiMapper mapper = Mappers.getMapper(ProductApiMapper.class);

    @Test
    void shouldMapDomainToResponse() {
        Product domain = Product.builder()
                .id(1L)
                .name("Apple")
                .description("Fresh apple")
                .gramsPerUnit(new BigDecimal("150"))
                .nutritionBasis(NutritionBasis.PER_100_GRAMS)
                .defaultPrice(new BigDecimal("2.49"))
                .nutritionalValues(NutritionalValues.builder()
                        .productId(1L)
                        .calories(new java.math.BigDecimal("52"))
                        .carbohydrates(new java.math.BigDecimal("14"))
                        .proteins(new java.math.BigDecimal("0.3"))
                        .fats(new java.math.BigDecimal("0.2"))
                        .build())
                .derivedProduct(RecipeDerivedProduct.builder()
                        .recipeId(8L)
                        .productId(9L)
                        .name("Apple compote")
                        .unitsProduced(new BigDecimal("4"))
                        .stockFromComposition(true)
                        .ingredients(List.of())
                        .build())
                .build();

        ProductResponse response = mapper.toResponse(domain);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Apple");
        assertThat(response.description()).isEqualTo("Fresh apple");
        assertThat(response.gramsPerUnit()).isEqualByComparingTo("150");
        assertThat(response.defaultPrice()).isEqualByComparingTo("2.49");
        assertThat(response.nutritionalValues().calories()).isEqualByComparingTo("52");
        assertThat(response.derivedProduct()).isNotNull();
        assertThat(response.derivedProduct().recipeId()).isEqualTo(8L);
        assertThat(response.derivedProduct().productId()).isEqualTo(9L);
    }

    @Test
    void shouldMapRecipeDomainToResponse() {
        Recipe domain = Recipe.builder()
                .id(3L)
                .name("Curry")
                .description("Homemade curry")
                .instructions("Cook slowly.")
                .nutritionalValues(NutritionalValues.builder()
                        .calories(new BigDecimal("500"))
                        .carbohydrates(new BigDecimal("20"))
                        .proteins(new BigDecimal("30"))
                        .fats(new BigDecimal("10"))
                        .build())
                .ingredients(List.of(RecipeIngredient.builder()
                        .productId(1L)
                        .productName("Chicken")
                        .quantity(new BigDecimal("200"))
                        .quantityType(QuantityType.GRAMS)
                        .nutritionalValues(NutritionalValues.builder()
                                .calories(new BigDecimal("330"))
                                .carbohydrates(new BigDecimal("0"))
                                .proteins(new BigDecimal("62"))
                                .fats(new BigDecimal("7.2"))
                                .build())
                        .build()))
                .derivedProduct(RecipeDerivedProduct.builder()
                        .recipeId(3L)
                        .productId(9L)
                        .name("Curry base")
                        .unitsProduced(new BigDecimal("4"))
                        .stockFromComposition(true)
                        .ingredients(List.of(RecipeIngredient.builder()
                                .productId(1L)
                                .productName("Chicken")
                                .quantity(new BigDecimal("50"))
                                .quantityType(QuantityType.GRAMS)
                                .build()))
                        .build())
                .build();

        RecipeResponse response = mapper.toResponse(domain);

        assertThat(response.id()).isEqualTo(3L);
        assertThat(response.products()).hasSize(1);
        assertThat(response.products().getFirst().productName()).isEqualTo("Chicken");
        assertThat(response.derivedProduct()).isNotNull();
        assertThat(response.derivedProduct().recipeId()).isEqualTo(3L);
        assertThat(response.derivedProduct().name()).isEqualTo("Curry base");
        assertThat(response.derivedProduct().unitsProduced()).isEqualByComparingTo("4");
        assertThat(response.derivedProduct().stockFromComposition()).isTrue();
        assertThat(response.derivedProduct().ingredients()).hasSize(1);
    }
}
