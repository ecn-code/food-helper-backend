package com.eliascanalesnieto.foodhelper.infra;

import com.eliascanalesnieto.foodhelper.domain.Media;
import com.eliascanalesnieto.foodhelper.domain.QuantityType;
import com.eliascanalesnieto.foodhelper.domain.Recipe;
import com.eliascanalesnieto.foodhelper.domain.RecipeDerivedProduct;
import com.eliascanalesnieto.foodhelper.domain.RecipeIngredient;
import com.eliascanalesnieto.foodhelper.domain.RecipeRepository;
import com.eliascanalesnieto.foodhelper.domain.ProductRepository;
import com.eliascanalesnieto.foodhelper.domain.RecipeSearchCriteria;
import com.eliascanalesnieto.foodhelper.presentation.error.DuplicateResourceException;
import com.eliascanalesnieto.foodhelper.presentation.error.ResourceNotFoundException;
import java.math.BigDecimal;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

@Repository
@RequiredArgsConstructor
public class JdbcRecipeRepository implements RecipeRepository {
    private static final String RECIPE_TOTALS = """
            WITH recipe_totals AS (
                SELECT r.id,
                       r.name,
                       r.description,
                       r.instructions,
                       COALESCE(string_agg(CONCAT_WS(' ', p.name, p.description), ' '), '') AS ingredient_text,
                       COALESCE(SUM(ROUND(CASE
                           WHEN rp.quantity_type = 'UNITS' THEN nv.calories * rp.quantity
                           ELSE nv.calories * rp.quantity / 100
                       END, 2)), 0) AS calories,
                       COALESCE(SUM(ROUND(CASE
                           WHEN rp.quantity_type = 'UNITS' THEN nv.carbohydrates * rp.quantity
                           ELSE nv.carbohydrates * rp.quantity / 100
                       END, 2)), 0) AS carbohydrates,
                       COALESCE(SUM(ROUND(CASE
                           WHEN rp.quantity_type = 'UNITS' THEN nv.proteins * rp.quantity
                           ELSE nv.proteins * rp.quantity / 100
                       END, 2)), 0) AS proteins,
                       COALESCE(SUM(ROUND(CASE
                           WHEN rp.quantity_type = 'UNITS' THEN nv.fats * rp.quantity
                           ELSE nv.fats * rp.quantity / 100
                       END, 2)), 0) AS fats,
                       EXISTS (SELECT 1 FROM recipe_product_origins origin WHERE origin.recipe_id = r.id) AS has_derived_product
                FROM recipes r
                LEFT JOIN recipe_products rp ON rp.recipe_id = r.id
                LEFT JOIN products p ON p.id = rp.product_id
                LEFT JOIN nutritional_values nv ON nv.product_id = p.id
                GROUP BY r.id, r.name, r.description, r.instructions
            )
            """;
    private static final String NORMALIZED_SEARCH = """
            translate(lower(CONCAT_WS(' ', name, description, instructions, ingredient_text)),
                      'áàäâéèëêíìïîóòöôúùüûñç',
                      'aaaaeeeeiiiioooouuuunc') LIKE :search
            """;
    private static final String SELECT_RECIPES_WITH_MEDIA = """
            SELECT r.id,
                   r.name,
                   r.description,
                   r.instructions,
                   r.default_units_produced,
                   r.media_id,
                   m.file_name,
                   m.content_type,
                   m.size_bytes,
                   m.width,
                   m.height
            FROM recipes r
            LEFT JOIN media m ON m.id = r.media_id
            """;

    private final RecipeCrudRepository recipeRepository;
    private final RecipeIngredientCrudRepository recipeIngredientRepository;
    private final RecipeProductOriginCrudRepository recipeProductOriginRepository;
    private final ProductRepository productRepository;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    public List<Recipe> findAll() {
        return jdbcTemplate.query("""
                        SELECT id
                        FROM recipes
                        ORDER BY LOWER(name), id
                        """,
                (rs, rowNum) -> rs.getLong("id")
        ).stream()
                .map(this::findById)
                .toList();
    }

    @Override
    public List<Recipe> findPage(int offset, int limit) {
        return findPage(offset, limit, RecipeSearchCriteria.empty());
    }

    @Override
    public List<Recipe> findPage(int offset, int limit, RecipeSearchCriteria criteria) {
        MapSqlParameterSource parameters = filterParameters(criteria)
                .addValue("limit", limit)
                .addValue("offset", offset);
        String sql = RECIPE_TOTALS + " SELECT id FROM recipe_totals " + filterClause(criteria)
                + " ORDER BY LOWER(name), id LIMIT :limit OFFSET :offset";
        return jdbcTemplate.query(sql, parameters, (rs, rowNum) -> rs.getLong("id")).stream()
                .map(this::findById)
                .toList();
    }

    @Override
    public long count() {
        return recipeRepository.count();
    }

    @Override
    public long count(RecipeSearchCriteria criteria) {
        Long count = jdbcTemplate.queryForObject(
                RECIPE_TOTALS + " SELECT COUNT(*) FROM recipe_totals " + filterClause(criteria),
                filterParameters(criteria),
                Long.class
        );
        return count == null ? 0 : count;
    }

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
        Recipe recipe = jdbcTemplate.query(SELECT_RECIPES_WITH_MEDIA + " WHERE r.id = :id",
                        new MapSqlParameterSource("id", id),
                        recipeRowMapper())
                .stream()
                .findFirst()
                .orElseThrow(() -> new ResourceNotFoundException("Recipe not found"));
        return recipe.toBuilder()
                .ingredients(recipeIngredientRepository.findAllByRecipeId(id).stream()
                        .map(this::toDomain)
                        .toList())
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
                .map(origin -> toDomain(origin, findDerivedIngredients(recipeId)));
    }

    @Override
    public Optional<RecipeDerivedProduct> findDerivedProductByProductId(Long productId) {
        return recipeProductOriginRepository.findByProductId(productId)
                .map(origin -> toDomain(origin, findDerivedIngredients(origin.recipeId())));
    }

    @Override
    @Transactional
    public RecipeDerivedProduct saveDerivedProduct(Long recipeId, Long productId, String name, BigDecimal unitsProduced, boolean stockFromComposition, List<RecipeIngredient> ingredients) {
        if (!recipeRepository.existsById(recipeId)) {
            throw new ResourceNotFoundException("Recipe not found");
        }
        RecipeProductOriginEntity origin = new RecipeProductOriginEntity(recipeId, productId, unitsProduced, stockFromComposition);
        MapSqlParameterSource originParameters = new MapSqlParameterSource()
                .addValue("recipeId", recipeId)
                .addValue("productId", productId)
                .addValue("unitsProduced", unitsProduced)
                .addValue("stockFromComposition", stockFromComposition);
        if (hasDerivedProductNameColumn()) {
            jdbcTemplate.update("""
                            INSERT INTO recipe_product_origins (recipe_id, product_id, derived_product_name, units_produced, stock_from_composition)
                            VALUES (:recipeId, :productId, :derivedProductName, :unitsProduced, :stockFromComposition)
                            ON CONFLICT (recipe_id) DO UPDATE SET
                                product_id = EXCLUDED.product_id,
                                derived_product_name = EXCLUDED.derived_product_name,
                                units_produced = EXCLUDED.units_produced,
                                stock_from_composition = EXCLUDED.stock_from_composition
                            """,
                    originParameters.addValue("derivedProductName", name));
        } else {
            jdbcTemplate.update("""
                            INSERT INTO recipe_product_origins (recipe_id, product_id, units_produced, stock_from_composition)
                            VALUES (:recipeId, :productId, :unitsProduced, :stockFromComposition)
                            ON CONFLICT (recipe_id) DO UPDATE SET
                                product_id = EXCLUDED.product_id,
                                units_produced = EXCLUDED.units_produced,
                                stock_from_composition = EXCLUDED.stock_from_composition
                            """,
                    originParameters);
        }
        jdbcTemplate.update("DELETE FROM recipe_derived_product_ingredients WHERE recipe_id = :recipeId",
                new MapSqlParameterSource("recipeId", recipeId));
        jdbcTemplate.batchUpdate("""
                        INSERT INTO recipe_derived_product_ingredients (recipe_id, product_id, quantity, quantity_type)
                        VALUES (:recipeId, :productId, :quantity, :quantityType)
                        """,
                ingredients.stream()
                        .map(ingredient -> new MapSqlParameterSource()
                                .addValue("recipeId", recipeId)
                                .addValue("productId", ingredient.getProductId())
                                .addValue("quantity", ingredient.getQuantity())
                                .addValue("quantityType", ingredient.getQuantityType().name()))
                        .toArray(MapSqlParameterSource[]::new));
        return toDomain(origin, ingredients);
    }

    private boolean hasDerivedProductNameColumn() {
        Boolean hasColumn = jdbcTemplate.queryForObject("""
                        SELECT EXISTS (
                            SELECT 1
                            FROM information_schema.columns
                            WHERE table_schema = current_schema()
                              AND table_name = 'recipe_product_origins'
                              AND column_name = 'derived_product_name'
                        )
                        """,
                new MapSqlParameterSource(),
                Boolean.class);
        return Boolean.TRUE.equals(hasColumn);
    }

    private String filterClause(RecipeSearchCriteria criteria) {
        List<String> filters = new java.util.ArrayList<>();
        if (criteria.search() != null) filters.add(NORMALIZED_SEARCH);
        if (criteria.minCalories() != null) filters.add("calories >= :minCalories");
        if (criteria.maxCalories() != null) filters.add("calories <= :maxCalories");
        if (criteria.minCarbohydrates() != null) filters.add("carbohydrates >= :minCarbohydrates");
        if (criteria.maxCarbohydrates() != null) filters.add("carbohydrates <= :maxCarbohydrates");
        if (criteria.minProteins() != null) filters.add("proteins >= :minProteins");
        if (criteria.maxProteins() != null) filters.add("proteins <= :maxProteins");
        if (criteria.minFats() != null) filters.add("fats >= :minFats");
        if (criteria.maxFats() != null) filters.add("fats <= :maxFats");
        if (criteria.hasDerivedProduct() != null) filters.add("has_derived_product = :hasDerivedProduct");
        return filters.isEmpty() ? "" : "WHERE " + String.join(" AND ", filters);
    }

    private MapSqlParameterSource filterParameters(RecipeSearchCriteria criteria) {
        MapSqlParameterSource parameters = new MapSqlParameterSource();
        if (criteria.search() != null) {
            String normalized = java.text.Normalizer.normalize(criteria.search().toLowerCase(java.util.Locale.ROOT), java.text.Normalizer.Form.NFD)
                    .replaceAll("\\p{M}", "");
            parameters.addValue("search", "%" + normalized + "%");
        }
        parameters.addValue("minCalories", criteria.minCalories());
        parameters.addValue("maxCalories", criteria.maxCalories());
        parameters.addValue("minCarbohydrates", criteria.minCarbohydrates());
        parameters.addValue("maxCarbohydrates", criteria.maxCarbohydrates());
        parameters.addValue("minProteins", criteria.minProteins());
        parameters.addValue("maxProteins", criteria.maxProteins());
        parameters.addValue("minFats", criteria.minFats());
        parameters.addValue("maxFats", criteria.maxFats());
        parameters.addValue("hasDerivedProduct", criteria.hasDerivedProduct());
        return parameters;
    }

    private RecipeEntity toEntity(Long id, Recipe recipe) {
        return new RecipeEntity(
                id,
                recipe.getName(),
                recipe.getDescription(),
                recipe.getInstructions(),
                recipe.getDefaultUnitsProduced(),
                recipe.getPhoto() == null ? null : recipe.getPhoto().getId()
        );
    }

    private void saveIngredients(Long recipeId, List<RecipeIngredient> ingredients) {
        recipeIngredientRepository.saveAll(ingredients.stream()
                .map(ingredient -> new RecipeIngredientEntity(
                        null,
                        recipeId,
                        ingredient.getProductId(),
                        ingredient.getQuantity(),
                        ingredient.getQuantityType().name()))
                .toList());
    }

    private List<RecipeIngredient> findDerivedIngredients(Long recipeId) {
        return jdbcTemplate.query("""
                        SELECT recipe_id, product_id, quantity, quantity_type
                        FROM recipe_derived_product_ingredients
                        WHERE recipe_id = :recipeId
                        ORDER BY product_id
                        """,
                new MapSqlParameterSource("recipeId", recipeId),
                (rs, rowNum) -> toDomainDerivedIngredient(rs))
                ;
    }

    private RecipeIngredient toDomain(RecipeIngredientEntity entity) {
        return RecipeIngredient.builder()
                .productId(entity.productId())
                .quantity(entity.quantity())
                .quantityType(QuantityType.valueOf(entity.quantityType()))
                .build();
    }

    private RecipeIngredient toDomainDerivedIngredient(ResultSet rs) throws SQLException {
        return RecipeIngredient.builder()
                .productId(rs.getLong("product_id"))
                .quantity(rs.getBigDecimal("quantity"))
                .quantityType(QuantityType.valueOf(rs.getString("quantity_type")))
                .build();
    }

    private RecipeDerivedProduct toDomain(RecipeProductOriginEntity origin, List<RecipeIngredient> ingredients) {
        return RecipeDerivedProduct.builder()
                .productId(origin.productId())
                .name(productRepository.findById(origin.productId()).getName())
                .unitsProduced(origin.unitsProduced())
                .stockFromComposition(origin.stockFromComposition())
                .ingredients(ingredients)
                .build();
    }

    private RowMapper<Recipe> recipeRowMapper() {
        return (rs, rowNum) -> Recipe.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .instructions(rs.getString("instructions"))
                .defaultUnitsProduced(rs.getBigDecimal("default_units_produced"))
                .photo(mapMedia(rs))
                .build();
    }

    private Media mapMedia(ResultSet rs) throws SQLException {
        long mediaId = rs.getLong("media_id");
        if (rs.wasNull()) {
            return null;
        }
        return Media.builder()
                .id(mediaId)
                .fileName(rs.getString("file_name"))
                .contentType(rs.getString("content_type"))
                .sizeBytes(rs.getInt("size_bytes"))
                .width(rs.getInt("width"))
                .height(rs.getInt("height"))
                .build();
    }
}
