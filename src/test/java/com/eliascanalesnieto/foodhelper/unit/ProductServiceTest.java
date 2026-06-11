package com.eliascanalesnieto.foodhelper.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.eliascanalesnieto.foodhelper.application.ProductService;
import com.eliascanalesnieto.foodhelper.domain.NutritionalValues;
import com.eliascanalesnieto.foodhelper.domain.Product;
import com.eliascanalesnieto.foodhelper.domain.ProductRepository;
import com.eliascanalesnieto.foodhelper.presentation.error.ResourceNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository repository;

    @InjectMocks
    private ProductService service;

    @Test
    void shouldCreateProduct() {
        Product created = Product.builder()
                .id(1L)
                .name("Apple")
                .description("Fresh apple")
                .nutritionalValues(NutritionalValues.builder()
                        .productId(1L)
                        .calories(new BigDecimal("52"))
                        .carbohydrates(new BigDecimal("14"))
                        .proteins(new BigDecimal("0.3"))
                        .fats(new BigDecimal("0.2"))
                        .build())
                .build();
        when(repository.create(org.mockito.ArgumentMatchers.any(Product.class))).thenReturn(created);

        Product result = service.create(
                "Apple",
                "Fresh apple",
                new BigDecimal("52"),
                new BigDecimal("14"),
                new BigDecimal("0.3"),
                new BigDecimal("0.2")
        );

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Apple");
        assertThat(result.getDescription()).isEqualTo("Fresh apple");
    }

    @Test
    void shouldReturnAllProducts() {
        when(repository.findAll()).thenReturn(List.of(
                Product.builder().id(1L).name("Apple").description("Fresh apple").build(),
                Product.builder().id(2L).name("Banana").description("Fresh banana").build()
        ));

        List<Product> result = service.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getName()).isEqualTo("Apple");
        assertThat(result.get(1).getName()).isEqualTo("Banana");
    }

    @Test
    void shouldPropagateNotFoundOnDeleteThroughRepository() {
        org.mockito.Mockito.doThrow(new ResourceNotFoundException("Product not found"))
                .when(repository).delete(99L);

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product not found");
    }
}
