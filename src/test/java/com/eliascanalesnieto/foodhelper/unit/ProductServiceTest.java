package com.eliascanalesnieto.foodhelper.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.eliascanalesnieto.foodhelper.application.MediaService;
import com.eliascanalesnieto.foodhelper.application.ProductService;
import com.eliascanalesnieto.foodhelper.domain.Media;
import com.eliascanalesnieto.foodhelper.domain.MediaUpload;
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

    @Mock
    private MediaService mediaService;

    @InjectMocks
    private ProductService service;

    @Test
    void shouldCreateProduct() {
        Product created = Product.builder()
                .id(1L)
                .name("Apple")
                .description("Fresh apple")
                .gramsPerUnit(new BigDecimal("150.00"))
                .nutritionalValues(NutritionalValues.builder()
                        .productId(1L)
                        .calories(new BigDecimal("52"))
                        .carbohydrates(new BigDecimal("14"))
                        .proteins(new BigDecimal("0.3"))
                        .fats(new BigDecimal("0.2"))
                        .build())
                .build();
        when(repository.create(any(Product.class))).thenReturn(created);

        Product result = service.create(
                "Apple",
                "Fresh apple",
                new BigDecimal("150"),
                new BigDecimal("52"),
                new BigDecimal("14"),
                new BigDecimal("0.3"),
                new BigDecimal("0.2"),
                null
        );

        assertThat(result.getId()).isEqualTo(1L);
        assertThat(result.getName()).isEqualTo("Apple");
        assertThat(result.getDescription()).isEqualTo("Fresh apple");
        assertThat(result.getGramsPerUnit()).isEqualByComparingTo("150.00");
    }

    @Test
    void shouldUpdateProductAndOptimizeReplacementPhoto() {
        MediaUpload photoUpload = MediaUpload.builder()
                .fileName("updated.png")
                .contentType("image/png")
                .base64Data("raw-base64-data")
                .build();
        Product existing = Product.builder()
                .id(1L)
                .name("Apple")
                .description("Fresh apple")
                .gramsPerUnit(new BigDecimal("150.00"))
                .nutritionalValues(NutritionalValues.builder()
                        .productId(1L)
                        .calories(new BigDecimal("52"))
                        .carbohydrates(new BigDecimal("14"))
                        .proteins(new BigDecimal("0.3"))
                        .fats(new BigDecimal("0.2"))
                        .build())
                .build();
        Media optimizedPhoto = Media.builder()
                .id(9L)
                .fileName("updated.jpg")
                .contentType("image/jpeg")
                .sizeBytes(1234)
                .build();
        Product updated = Product.builder()
                .id(1L)
                .name("Green Apple")
                .description("Green apple")
                .gramsPerUnit(new BigDecimal("140.00"))
                .nutritionalValues(NutritionalValues.builder()
                        .productId(1L)
                        .calories(new BigDecimal("48"))
                        .carbohydrates(new BigDecimal("13"))
                        .proteins(new BigDecimal("0.4"))
                        .fats(new BigDecimal("0.1"))
                        .build())
                .build();

        when(repository.findById(1L)).thenReturn(existing);
        when(mediaService.createOptimized(photoUpload)).thenReturn(optimizedPhoto);
        when(repository.update(any(Long.class), any(Product.class))).thenReturn(updated);

        Product result = service.update(
                1L,
                "Green Apple",
                "Green apple",
                new BigDecimal("140"),
                new BigDecimal("48"),
                new BigDecimal("13"),
                new BigDecimal("0.4"),
                new BigDecimal("0.1"),
                photoUpload
        );

        assertThat(result.getPhoto()).isEqualTo(optimizedPhoto);
        verify(mediaService).createOptimized(photoUpload);
    }

    @Test
    void shouldReturnAllProducts() {
        when(repository.findAll()).thenReturn(List.of(
                Product.builder().id(1L).name("Apple").description("Fresh apple").gramsPerUnit(new BigDecimal("150")).build(),
                Product.builder().id(2L).name("Banana").description("Fresh banana").gramsPerUnit(new BigDecimal("120")).build()
        ));

        List<Product> result = service.findAll();

        assertThat(result).hasSize(2);
        assertThat(result.getFirst().getName()).isEqualTo("Apple");
        assertThat(result.get(1).getName()).isEqualTo("Banana");
    }

    @Test
    void shouldPropagateNotFoundOnDeleteThroughRepository() {
        when(repository.findById(99L)).thenThrow(new ResourceNotFoundException("Product not found"));

        assertThatThrownBy(() -> service.delete(99L))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Product not found");
    }
}
