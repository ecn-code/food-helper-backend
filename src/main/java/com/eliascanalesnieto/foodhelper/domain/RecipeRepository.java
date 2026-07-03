package com.eliascanalesnieto.foodhelper.domain;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

public interface RecipeRepository {
    List<Recipe> findAll();

    List<Recipe> findPage(int offset, int limit);

    List<Recipe> findPage(int offset, int limit, RecipeSearchCriteria criteria);

    long count();

    long count(RecipeSearchCriteria criteria);

    Recipe create(Recipe recipe);

    Recipe update(Long id, Recipe recipe);

    Recipe findById(Long id);

    void delete(Long id);

    Optional<RecipeDerivedProduct> findDerivedProductByRecipeId(Long recipeId);

    Optional<RecipeDerivedProduct> findDerivedProductByProductId(Long productId);

    RecipeDerivedProduct saveDerivedProduct(Long recipeId, Long productId, String name, BigDecimal unitsProduced, boolean stockFromComposition, List<RecipeIngredient> ingredients);
}
