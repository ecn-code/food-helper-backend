package com.eliascanalesnieto.foodhelper.infra;

import com.eliascanalesnieto.foodhelper.domain.Recipe;
import com.eliascanalesnieto.foodhelper.domain.RecipeDerivedProduct;
import com.eliascanalesnieto.foodhelper.domain.RecipeIngredient;
import com.eliascanalesnieto.foodhelper.domain.RecipeRepository;
import com.eliascanalesnieto.foodhelper.presentation.error.DuplicateResourceException;
import com.eliascanalesnieto.foodhelper.presentation.error.ResourceNotFoundException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class JdbcRecipeRepository implements RecipeRepository {
    private final RecipeCrudRepository recipeRepository;
    private final RecipeIngredientCrudRepository recipeIngredientRepository;
    private final RecipeProductOriginCrudRepository recipeProductOriginRepository;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public Recipe create(Recipe recipe) {
        try {
            RecipeEntity savedRecipe = recipeRepository.save(toEntity(recipe.getId(), recipe));
            saveIngredients(savedRecipe.id(), recipe.getIngredients());
            return recipe.toBuilder().id(savedRecipe.id()).build();
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateResourceException("Recipe name already exists");
        }
    }

    @Override
    @Transactional
    public Recipe update(Long id, Recipe recipe) {
        if (!recipeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Recipe not found");
        }
        try {
            RecipeEntity savedRecipe = recipeRepository.save(toEntity(id, recipe));
            recipeIngredientRepository.deleteAllByRecipeId(id);
            saveIngredients(id, recipe.getIngredients());
            return recipe.toBuilder().id(savedRecipe.id()).build();
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateResourceException("Recipe name already exists");
        }
    }

    @Override
    public Recipe findById(Long id) {
        RecipeEntity recipe = recipeRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Recipe not found"));
        List<RecipeIngredient> ingredients = recipeIngredientRepository.findAllByRecipeId(id).stream()
                .map(this::toDomain)
                .toList();
        return Recipe.builder()
                .id(recipe.id())
                .name(recipe.name())
                .description(recipe.description())
                .instructions(recipe.instructions())
                .ingredients(ingredients)
                .build();
    }

    @Override
    @Transactional
    public void delete(Long id) {
        if (!recipeRepository.existsById(id)) {
            throw new ResourceNotFoundException("Recipe not found");
        }
        recipeRepository.deleteById(id);
    }

    @Override
    public Optional<RecipeDerivedProduct> findDerivedProductByRecipeId(Long recipeId) {
        return recipeProductOriginRepository.findById(recipeId)
                .map(this::toDomain);
    }

    @Override
    @Transactional
    public RecipeDerivedProduct linkDerivedProduct(Long recipeId, Long productId, BigDecimal producedGrams, BigDecimal gramsPerUnit) {
        if (!recipeRepository.existsById(recipeId)) {
            throw new ResourceNotFoundException("Recipe not found");
        }
        try {
            RecipeProductOriginEntity savedOrigin = new RecipeProductOriginEntity(recipeId, productId, producedGrams, gramsPerUnit);
            jdbcTemplate.update("""
                            INSERT INTO recipe_product_origins (recipe_id, product_id, produced_grams, grams_per_unit)
                            VALUES (:recipeId, :productId, :producedGrams, :gramsPerUnit)
                            """,
                    new MapSqlParameterSource()
                            .addValue("recipeId", recipeId)
                            .addValue("productId", productId)
                            .addValue("producedGrams", producedGrams)
                            .addValue("gramsPerUnit", gramsPerUnit));
            return toDomain(savedOrigin);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateResourceException("Recipe already has a derived product");
        }
    }

    private RecipeEntity toEntity(Long id, Recipe recipe) {
        return new RecipeEntity(id, recipe.getName(), recipe.getDescription(), recipe.getInstructions());
    }

    private void saveIngredients(Long recipeId, List<RecipeIngredient> ingredients) {
        recipeIngredientRepository.saveAll(ingredients.stream()
                .map(ingredient -> new RecipeIngredientEntity(null, recipeId, ingredient.getProductId(), ingredient.getGrams()))
                .toList());
    }

    private RecipeIngredient toDomain(RecipeIngredientEntity entity) {
        return RecipeIngredient.builder()
                .productId(entity.productId())
                .grams(entity.grams())
                .build();
    }

    private RecipeDerivedProduct toDomain(RecipeProductOriginEntity entity) {
        return RecipeDerivedProduct.builder()
                .productId(entity.productId())
                .producedGrams(entity.producedGrams())
                .gramsPerUnit(entity.gramsPerUnit())
                .build();
    }
}
