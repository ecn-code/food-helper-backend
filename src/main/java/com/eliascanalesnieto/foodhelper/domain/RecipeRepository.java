package com.eliascanalesnieto.foodhelper.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface RecipeRepository {
    List<Recipe> findAll();

    Recipe create(Recipe recipe);

    Recipe update(Long id, Recipe recipe);

    Recipe findById(Long id);

    void delete(Long id);

    Optional<RecipeDerivedProduct> findDerivedProductByRecipeId(Long recipeId);

    RecipeDerivedProduct linkDerivedProduct(Long recipeId, Long productId, BigDecimal producedGrams, BigDecimal gramsPerUnit);
}
