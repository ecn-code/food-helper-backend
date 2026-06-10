package com.eliascanalesnieto.foodhelper.unit;

import static org.assertj.core.api.Assertions.assertThat;

import com.eliascanalesnieto.foodhelper.domain.NutritionalValues;
import com.eliascanalesnieto.foodhelper.domain.Product;
import com.eliascanalesnieto.foodhelper.presentation.ProductApiMapper;
import com.eliascanalesnieto.foodhelper.presentation.ProductResponse;
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
                .nutritionalValues(NutritionalValues.builder()
                        .productId(1L)
                        .calories(new java.math.BigDecimal("52"))
                        .carbohydrates(new java.math.BigDecimal("14"))
                        .proteins(new java.math.BigDecimal("0.3"))
                        .fats(new java.math.BigDecimal("0.2"))
                        .build())
                .build();

        ProductResponse response = mapper.toResponse(domain);

        assertThat(response.id()).isEqualTo(1L);
        assertThat(response.name()).isEqualTo("Apple");
        assertThat(response.description()).isEqualTo("Fresh apple");
        assertThat(response.nutritionalValues().calories()).isEqualByComparingTo("52");
    }
}
