package com.eliascanalesnieto.foodhelper.infra;

import com.eliascanalesnieto.foodhelper.domain.NutritionalValues;
import com.eliascanalesnieto.foodhelper.domain.Product;
import com.eliascanalesnieto.foodhelper.domain.ProductRepository;
import com.eliascanalesnieto.foodhelper.presentation.error.DuplicateResourceException;
import com.eliascanalesnieto.foodhelper.presentation.error.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class JdbcProductRepository implements ProductRepository {
    private final ProductCrudRepository productRepository;
    private final NutritionalValuesCrudRepository nutritionalValuesRepository;

    @Override
    @Transactional
    public Product create(Product product) {
        try {
            ProductEntity savedProduct = productRepository.save(new ProductEntity(null, product.getName(), product.getDescription()));
            NutritionalValuesEntity savedValues = nutritionalValuesRepository.save(toEntity(savedProduct.id(), product.getNutritionalValues()));
            return toDomain(savedProduct, savedValues);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateResourceException("Product name already exists");
        }
    }

    @Override
    @Transactional
    public Product update(Long id, Product product) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found");
        }
        try {
            ProductEntity savedProduct = productRepository.save(new ProductEntity(id, product.getName(), product.getDescription()));
            NutritionalValuesEntity savedValues = nutritionalValuesRepository.save(toEntity(id, product.getNutritionalValues()));
            return toDomain(savedProduct, savedValues);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateResourceException("Product name already exists");
        }
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!productRepository.existsById(id)) {
            throw new ResourceNotFoundException("Product not found");
        }
        productRepository.deleteById(id);
    }

    private NutritionalValuesEntity toEntity(Long productId, NutritionalValues values) {
        return new NutritionalValuesEntity(
                productId,
                values.getCalories(),
                values.getCarbohydrates(),
                values.getProteins(),
                values.getFats()
        );
    }

    private Product toDomain(ProductEntity product, NutritionalValuesEntity values) {
        return Product.builder()
                .id(product.id())
                .name(product.name())
                .description(product.description())
                .nutritionalValues(NutritionalValues.builder()
                        .productId(values.productId())
                        .calories(values.calories())
                        .carbohydrates(values.carbohydrates())
                        .proteins(values.proteins())
                        .fats(values.fats())
                        .build())
                .build();
    }
}
