package com.eliascanalesnieto.foodhelper.infra;

import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface RecipeIngredientCrudRepository extends CrudRepository<RecipeIngredientEntity, Long> {
    List<RecipeIngredientEntity> findAllByRecipeId(Long recipeId);

    void deleteAllByRecipeId(Long recipeId);
}
