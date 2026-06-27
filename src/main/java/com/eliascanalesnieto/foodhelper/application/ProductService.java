package com.eliascanalesnieto.foodhelper.application;

import com.eliascanalesnieto.foodhelper.domain.Media;
import com.eliascanalesnieto.foodhelper.domain.MediaUpload;
import com.eliascanalesnieto.foodhelper.domain.NutritionalValues;
import com.eliascanalesnieto.foodhelper.domain.ProductSearchCriteria;
import com.eliascanalesnieto.foodhelper.domain.Product;
import com.eliascanalesnieto.foodhelper.domain.ProductRepository;
import com.eliascanalesnieto.foodhelper.domain.SupermarketRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {
    private final ProductRepository repository;
    private final SupermarketRepository supermarketRepository;
    private final MediaService mediaService;

    @Transactional
    public Product create(String name, String description, BigDecimal gramsPerUnit, BigDecimal calories, BigDecimal carbohydrates, BigDecimal proteins, BigDecimal fats, BigDecimal defaultPrice, MediaUpload photoUpload) {
        return create(name, description, gramsPerUnit, calories, carbohydrates, proteins, fats, defaultPrice, photoUpload, List.of());
    }

    @Transactional
    public Product create(String name, String description, BigDecimal gramsPerUnit, BigDecimal calories, BigDecimal carbohydrates, BigDecimal proteins, BigDecimal fats, BigDecimal defaultPrice, MediaUpload photoUpload, List<Long> supermarketIds) {
        var supermarkets = supermarketRepository.findByIds(supermarketIds == null ? List.of() : supermarketIds);
        Media photo = mediaService.createOptimized(photoUpload);
        Product created = repository.create(Product.builder()
                .name(name)
                .description(description)
                .gramsPerUnit(scale(gramsPerUnit))
                .defaultPrice(scaleOptional(defaultPrice))
                .nutritionalValues(buildNutritionalValues(calories, carbohydrates, proteins, fats))
                .photo(photo)
                .supermarkets(supermarkets)
                .build());
        return created.toBuilder()
                .photo(photo)
                .build();
    }

    @Transactional
    public Product update(Long id, String name, String description, BigDecimal gramsPerUnit, BigDecimal calories, BigDecimal carbohydrates, BigDecimal proteins, BigDecimal fats, BigDecimal defaultPrice, MediaUpload photoUpload) {
        return update(id, name, description, gramsPerUnit, calories, carbohydrates, proteins, fats, defaultPrice, photoUpload, List.of());
    }

    @Transactional
    public Product update(Long id, String name, String description, BigDecimal gramsPerUnit, BigDecimal calories, BigDecimal carbohydrates, BigDecimal proteins, BigDecimal fats, BigDecimal defaultPrice, MediaUpload photoUpload, List<Long> supermarketIds) {
        Product existing = repository.findById(id);
        var supermarkets = supermarketRepository.findByIds(supermarketIds == null ? List.of() : supermarketIds);
        Media photo = photoUpload == null ? existing.getPhoto() : mediaService.createOptimized(photoUpload);
        BigDecimal resolvedDefaultPrice = defaultPrice == null ? existing.getDefaultPrice() : scaleOptional(defaultPrice);
        Product updated = repository.update(id, Product.builder()
                .name(name)
                .description(description)
                .gramsPerUnit(scale(gramsPerUnit))
                .defaultPrice(resolvedDefaultPrice)
                .nutritionalValues(buildNutritionalValues(calories, carbohydrates, proteins, fats))
                .photo(photo)
                .supermarkets(supermarkets)
                .build());
        if (photoUpload != null && existing.getPhoto() != null) {
            mediaService.delete(existing.getPhoto().getId());
        }
        return updated.toBuilder()
                .photo(photo)
                .build();
    }

    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return repository.findAll();
    }

    @Transactional(readOnly = true)
    public PageResult<Product> findPage(PaginationRequest pagination) {
        return findPage(pagination, ProductSearchCriteria.empty());
    }

    @Transactional(readOnly = true)
    public PageResult<Product> findPage(PaginationRequest pagination, ProductSearchCriteria searchCriteria) {
        List<Product> items = repository.findPage(pagination.offset(), pagination.size(), searchCriteria);
        return new PageResult<>(items, pagination.page(), pagination.size(), repository.count(searchCriteria));
    }

    @Transactional
    public void delete(Long id) {
        Product existing = repository.findById(id);
        repository.delete(id);
        if (existing.getPhoto() != null) {
            mediaService.delete(existing.getPhoto().getId());
        }
    }

    private NutritionalValues buildNutritionalValues(BigDecimal calories, BigDecimal carbohydrates, BigDecimal proteins, BigDecimal fats) {
        return NutritionalValues.builder()
                .calories(calories)
                .carbohydrates(carbohydrates)
                .proteins(proteins)
                .fats(fats)
                .build();
    }

    private BigDecimal scale(BigDecimal value) {
        if (value == null) {
            throw new IllegalArgumentException("Grams per unit is required");
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private BigDecimal scaleOptional(BigDecimal value) {
        if (value == null) {
            return null;
        }
        return value.setScale(2, RoundingMode.HALF_UP);
    }
}
