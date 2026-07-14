package com.eliascanalesnieto.foodhelper.application;

import com.eliascanalesnieto.foodhelper.domain.Product;
import com.eliascanalesnieto.foodhelper.domain.ProductRepository;
import com.eliascanalesnieto.foodhelper.domain.RecipeDerivedProduct;
import com.eliascanalesnieto.foodhelper.domain.RecipeIngredient;
import com.eliascanalesnieto.foodhelper.domain.RecipeRepository;
import com.eliascanalesnieto.foodhelper.presentation.error.ResourceNotFoundException;
import java.util.Collection;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

/** Loads catalog products with the derived-product information required by menu calculations. */
@Service
@RequiredArgsConstructor
public class MenuProductResolver {
    private final ProductRepository productRepository;
    private final RecipeRepository recipeRepository;

    public Map<Long, Product> loadByIds(Collection<Long> productIds, String missingMessage) {
        List<Long> ids = productIds.stream().filter(java.util.Objects::nonNull).distinct().toList();
        if (ids.isEmpty()) {
            return Map.of();
        }
        List<Product> products = productRepository.findByIds(ids);
        if (products.size() != ids.size()) {
            throw new ResourceNotFoundException(missingMessage);
        }
        Map<Long, Product> productsById = new LinkedHashMap<>();
        products.stream().map(this::attachDerivedProduct).forEach(product -> productsById.put(product.getId(), product));
        return productsById;
    }

    public Map<Long, Product> loadCompositionProducts(Map<Long, Product> productsById, String missingMessage) {
        List<Long> ingredientIds = productsById.values().stream()
                .map(Product::getDerivedProduct)
                .filter(this::usesCompositionStock)
                .flatMap(derivedProduct -> derivedProduct.getIngredients().stream())
                .map(RecipeIngredient::getProductId)
                .filter(java.util.Objects::nonNull)
                .filter(productId -> !productsById.containsKey(productId))
                .distinct()
                .toList();
        return loadByIds(ingredientIds, missingMessage);
    }

    public boolean usesCompositionStock(Product product) {
        return usesCompositionStock(product == null ? null : product.getDerivedProduct());
    }

    public boolean usesCompositionStock(RecipeDerivedProduct derivedProduct) {
        return derivedProduct != null
                && derivedProduct.isStockFromComposition()
                && derivedProduct.getIngredients() != null
                && !derivedProduct.getIngredients().isEmpty();
    }

    private Product attachDerivedProduct(Product product) {
        return product.toBuilder()
                .derivedProduct(recipeRepository.findDerivedProductByProductId(product.getId()).orElse(null))
                .build();
    }
}
