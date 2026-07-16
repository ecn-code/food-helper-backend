package com.eliascanalesnieto.foodhelper.application;

import com.eliascanalesnieto.foodhelper.domain.Media;
import com.eliascanalesnieto.foodhelper.domain.MediaUpload;
import com.eliascanalesnieto.foodhelper.domain.NutritionBasis;
import com.eliascanalesnieto.foodhelper.domain.NutritionalValues;
import com.eliascanalesnieto.foodhelper.domain.Product;
import com.eliascanalesnieto.foodhelper.domain.ProductRepository;
import com.eliascanalesnieto.foodhelper.domain.ProductSearchCriteria;
import com.eliascanalesnieto.foodhelper.domain.RecipeRepository;
import com.eliascanalesnieto.foodhelper.domain.RecipeDerivedProduct;
import com.eliascanalesnieto.foodhelper.domain.RecipeIngredient;
import com.eliascanalesnieto.foodhelper.domain.SupermarketRepository;
import com.eliascanalesnieto.foodhelper.presentation.error.ResourceNotFoundException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class ProductService {
    private static final int PRICE_SCALE = 4;
    private final ProductRepository repository;
    private final RecipeRepository recipeRepository;
    private final SupermarketRepository supermarketRepository;
    private final MediaService mediaService;

    @Transactional
    public Product create(String name, String description, BigDecimal gramsPerUnit, boolean isStockInUnits, BigDecimal calories, BigDecimal carbohydrates, BigDecimal proteins, BigDecimal fats, BigDecimal defaultPrice, MediaUpload photoUpload) {
        return create(name, description, gramsPerUnit, isStockInUnits, calories, carbohydrates, proteins, fats, defaultPrice, photoUpload, List.of());
    }

    @Transactional
    public Product create(String name, String description, BigDecimal gramsPerUnit, BigDecimal calories, BigDecimal carbohydrates, BigDecimal proteins, BigDecimal fats, BigDecimal defaultPrice, MediaUpload photoUpload) {
        return create(name, description, gramsPerUnit, false, calories, carbohydrates, proteins, fats, defaultPrice, photoUpload, List.of());
    }

    @Transactional
    public Product create(String name, String description, BigDecimal gramsPerUnit, boolean isStockInUnits, BigDecimal calories, BigDecimal carbohydrates, BigDecimal proteins, BigDecimal fats, BigDecimal defaultPrice, MediaUpload photoUpload, List<Long> supermarketIds) {
        var supermarkets = supermarketRepository.findByIds(supermarketIds == null ? List.of() : supermarketIds);
        Media photo = mediaService.createOptimized(photoUpload);
        Product created = repository.create(Product.builder()
                .name(name)
                .description(description)
                .gramsPerUnit(scale(gramsPerUnit))
                .stockInUnits(isStockInUnits)
                .nutritionBasis(NutritionBasis.PER_100_GRAMS)
                .defaultPrice(scaleOptional(defaultPrice))
                .nutritionalValues(buildNutritionalValues(calories, carbohydrates, proteins, fats))
                .photo(photo)
                .supermarkets(supermarkets)
                .build());
        return attachDerived(created.toBuilder().photo(photo).build());
    }

    @Transactional
    public Product update(Long id, String name, String description, BigDecimal gramsPerUnit, boolean isStockInUnits, BigDecimal calories, BigDecimal carbohydrates, BigDecimal proteins, BigDecimal fats, BigDecimal defaultPrice, MediaUpload photoUpload) {
        return update(id, name, description, gramsPerUnit, isStockInUnits, calories, carbohydrates, proteins, fats, defaultPrice, photoUpload, List.of());
    }

    @Transactional
    public Product update(Long id, String name, String description, BigDecimal gramsPerUnit, BigDecimal calories, BigDecimal carbohydrates, BigDecimal proteins, BigDecimal fats, BigDecimal defaultPrice, MediaUpload photoUpload) {
        return update(id, name, description, gramsPerUnit, false, calories, carbohydrates, proteins, fats, defaultPrice, photoUpload, List.of());
    }

    @Transactional
    public Product update(Long id, String name, String description, BigDecimal gramsPerUnit, boolean isStockInUnits, BigDecimal calories, BigDecimal carbohydrates, BigDecimal proteins, BigDecimal fats, BigDecimal defaultPrice, MediaUpload photoUpload, List<Long> supermarketIds) {
        Product existing = repository.findById(id);
        var supermarkets = supermarketRepository.findByIds(supermarketIds == null ? List.of() : supermarketIds);
        Media photo = photoUpload == null ? existing.getPhoto() : mediaService.createOptimized(photoUpload);
        BigDecimal resolvedDefaultPrice = defaultPrice == null ? existing.getDefaultPrice() : scaleOptional(defaultPrice);
        Product updated = repository.update(id, Product.builder()
                .name(name)
                .description(description)
                .gramsPerUnit(scale(gramsPerUnit))
                .stockInUnits(isStockInUnits)
                .nutritionBasis(existing.getNutritionBasis() == null ? NutritionBasis.PER_100_GRAMS : existing.getNutritionBasis())
                .defaultPrice(resolvedDefaultPrice)
                .nutritionalValues(buildNutritionalValues(calories, carbohydrates, proteins, fats))
                .photo(photo)
                .supermarkets(supermarkets)
                .build());
        if (photoUpload != null && existing.getPhoto() != null) {
            mediaService.delete(existing.getPhoto().getId());
        }
        return attachDerived(updated.toBuilder().photo(photo).build());
    }

    @Transactional(readOnly = true)
    public List<Product> findAll() {
        return repository.findAll().stream()
                .map(this::attachDerived)
                .toList();
    }

    @Transactional(readOnly = true)
    public PageResult<Product> findPage(PaginationRequest pagination) {
        return findPage(pagination, ProductSearchCriteria.empty());
    }

    @Transactional(readOnly = true)
    public PageResult<Product> findPage(PaginationRequest pagination, ProductSearchCriteria searchCriteria) {
        List<Product> items = repository.findPage(pagination.offset(), pagination.size(), searchCriteria).stream()
                .map(this::attachDerived)
                .toList();
        return new PageResult<>(items, pagination.page(), pagination.size(), repository.count(searchCriteria));
    }

    @Transactional(readOnly = true)
    public Product findById(Long id) {
        return attachDerived(repository.findById(id));
    }

    @Transactional
    public void delete(Long id) {
        Product existing = repository.findById(id);
        repository.delete(id);
        if (existing.getPhoto() != null) {
            mediaService.delete(existing.getPhoto().getId());
        }
    }

    private Product attachDerived(Product product) {
        return product.toBuilder()
                .derivedProduct(recipeRepository.findDerivedProductByProductId(product.getId())
                        .map(this::completeDerivedProduct)
                        .orElse(null))
                .build();
    }

    private RecipeDerivedProduct completeDerivedProduct(RecipeDerivedProduct derivedProduct) {
        Map<Long, Product> productsById = loadProducts(derivedProduct.getIngredients());
        return RecipeDerivedProduct.builder()
                .recipeId(derivedProduct.getRecipeId())
                .productId(derivedProduct.getProductId())
                .name(derivedProduct.getName())
                .unitsProduced(derivedProduct.getUnitsProduced())
                .stockFromComposition(derivedProduct.isStockFromComposition())
                .ingredients(derivedProduct.getIngredients().stream()
                        .map(ingredient -> enrichIngredient(ingredient, productsById.get(ingredient.getProductId())))
                        .toList())
                .build();
    }

    private Map<Long, Product> loadProducts(List<RecipeIngredient> ingredients) {
        List<Long> ids = ingredients.stream()
                .map(RecipeIngredient::getProductId)
                .distinct()
                .toList();
        List<Product> products = repository.findByIds(ids);
        if (products.size() != ids.size()) {
            throw new ResourceNotFoundException("One or more products were not found");
        }
        Map<Long, Product> productsById = new LinkedHashMap<>();
        products.forEach(product -> productsById.put(product.getId(), product));
        return productsById;
    }

    private RecipeIngredient enrichIngredient(RecipeIngredient ingredient, Product product) {
        return RecipeIngredient.builder()
                .productId(product.getId())
                .productName(product.getName())
                .quantity(ingredient.getQuantity())
                .quantityType(ingredient.getQuantityType())
                .nutritionalValues(ingredient.getNutritionalValues())
                .build();
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
        return value.setScale(PRICE_SCALE, RoundingMode.HALF_UP);
    }
}
