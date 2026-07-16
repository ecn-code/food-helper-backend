package com.eliascanalesnieto.foodhelper.infra;

import com.eliascanalesnieto.foodhelper.domain.Media;
import com.eliascanalesnieto.foodhelper.domain.NutritionBasis;
import com.eliascanalesnieto.foodhelper.domain.NutritionalValues;
import com.eliascanalesnieto.foodhelper.domain.Product;
import com.eliascanalesnieto.foodhelper.domain.ProductRepository;
import com.eliascanalesnieto.foodhelper.domain.ProductSearchCriteria;
import com.eliascanalesnieto.foodhelper.domain.Supermarket;
import com.eliascanalesnieto.foodhelper.presentation.error.DuplicateResourceException;
import com.eliascanalesnieto.foodhelper.presentation.error.ResourceNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
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
public class JdbcProductRepository implements ProductRepository {
    private static final String SELECT_PRODUCTS_WITH_VALUES = """
            SELECT p.id,
                   p.name,
                   p.description,
                   p.grams_per_unit,
                   p.is_stock_in_units,
                   p.nutrition_basis,
                   p.default_price,
                   p.created_at,
                   m.id AS media_id,
                   m.file_name,
                   m.content_type,
                   m.size_bytes,
                   m.width,
                   m.height,
                   nv.product_id,
                   nv.calories,
                   nv.carbohydrates,
                   nv.proteins,
                   nv.fats
            FROM products p
            JOIN nutritional_values nv ON nv.product_id = p.id
            LEFT JOIN media m ON m.id = p.media_id
            """;

    private final ProductCrudRepository productRepository;
    private final NutritionalValuesCrudRepository nutritionalValuesRepository;
    private final NamedParameterJdbcTemplate jdbcTemplate;

    @Override
    @Transactional
    public Product create(Product product) {
        try {
            Instant createdAt = product.getCreatedAt() == null ? Instant.now() : product.getCreatedAt();
            ProductEntity savedProduct = productRepository.save(new ProductEntity(
                    null,
                    product.getName(),
                    product.getDescription(),
                    product.getGramsPerUnit(),
                    product.isStockInUnits(),
                    product.getNutritionBasis() == null ? NutritionBasis.PER_100_GRAMS.name() : product.getNutritionBasis().name(),
                    product.getDefaultPrice(),
                    mediaId(product),
                    createdAt
            ));
            NutritionalValuesEntity savedValues = upsertNutritionalValues(savedProduct.id(), product.getNutritionalValues());
            replaceSupermarkets(savedProduct.id(), product.getSupermarkets());
            return toDomain(savedProduct, savedValues, product.getSupermarkets());
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
            Instant createdAt = product.getCreatedAt() == null ? findById(id).getCreatedAt() : product.getCreatedAt();
            ProductEntity savedProduct = productRepository.save(new ProductEntity(
                    id,
                    product.getName(),
                    product.getDescription(),
                    product.getGramsPerUnit(),
                    product.isStockInUnits(),
                    product.getNutritionBasis() == null ? NutritionBasis.PER_100_GRAMS.name() : product.getNutritionBasis().name(),
                    product.getDefaultPrice(),
                    mediaId(product),
                    createdAt
            ));
            NutritionalValuesEntity savedValues = upsertNutritionalValues(id, product.getNutritionalValues());
            replaceSupermarkets(id, product.getSupermarkets());
            return toDomain(savedProduct, savedValues, product.getSupermarkets());
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateResourceException("Product name already exists");
        }
    }

    @Override
    public Product findById(Long id) {
        List<Product> products = jdbcTemplate.query(
                SELECT_PRODUCTS_WITH_VALUES + " WHERE p.id = :id",
                new MapSqlParameterSource("id", id),
                productRowMapper()
        );
        if (products.isEmpty()) {
            throw new ResourceNotFoundException("Product not found");
        }
        return attachSupermarkets(products.getFirst());
    }

    @Override
    public Optional<Product> findByName(String name) {
        return jdbcTemplate.query(
                SELECT_PRODUCTS_WITH_VALUES + " WHERE p.name = :name",
                new MapSqlParameterSource("name", name),
                productRowMapper()
        ).stream().findFirst().map(this::attachSupermarkets);
    }

    @Override
    public List<Product> findAll() {
        return jdbcTemplate.query(SELECT_PRODUCTS_WITH_VALUES + " ORDER BY LOWER(p.name), p.id", productRowMapper()).stream()
                .map(this::attachSupermarkets)
                .toList();
    }

    @Override
    public List<Product> findPage(int offset, int limit, ProductSearchCriteria searchCriteria) {
        MapSqlParameterSource params = new MapSqlParameterSource()
                .addValue("limit", limit)
                .addValue("offset", offset);
        StringBuilder sql = new StringBuilder(SELECT_PRODUCTS_WITH_VALUES);
        appendFilters(sql, params, searchCriteria);
        sql.append(" ORDER BY LOWER(p.name), p.id LIMIT :limit OFFSET :offset");
        return jdbcTemplate.query(
                sql.toString(),
                params,
                productRowMapper()
        ).stream().map(this::attachSupermarkets).toList();
    }

    @Override
    public long count(ProductSearchCriteria searchCriteria) {
        MapSqlParameterSource params = new MapSqlParameterSource();
        StringBuilder sql = new StringBuilder("""
                SELECT COUNT(*)
                FROM products p
                JOIN nutritional_values nv ON nv.product_id = p.id
                """);
        appendFilters(sql, params, searchCriteria);
        return jdbcTemplate.queryForObject(sql.toString(), params, Long.class);
    }

    @Override
    public List<Product> findByIds(Collection<Long> ids) {
        if (ids.isEmpty()) {
            return List.of();
        }
        return new LinkedHashSet<>(ids).stream()
                .map(this::findById)
                .toList();
    }

    @Override
    public Collection<Long> findProductIdsBySupermarket(Long supermarketId, Collection<Long> productIds) {
        if (productIds == null || productIds.isEmpty()) {
            return List.of();
        }
        return jdbcTemplate.queryForList("""
                        SELECT p.id
                        FROM products p
                        WHERE p.id IN (:productIds)
                          AND (
                                NOT EXISTS (
                                    SELECT 1
                                    FROM product_supermarkets ps
                                    WHERE ps.product_id = p.id
                                )
                                OR EXISTS (
                                    SELECT 1
                                    FROM product_supermarkets ps
                                    WHERE ps.product_id = p.id
                                      AND ps.supermarket_id = :supermarketId
                                )
                          )
                        ORDER BY p.id
                        """,
                new MapSqlParameterSource()
                        .addValue("supermarketId", supermarketId)
                        .addValue("productIds", productIds),
                Long.class
        );
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

    private Product toDomain(ProductEntity product, NutritionalValuesEntity values, List<Supermarket> supermarkets) {
        return Product.builder()
                .id(product.id())
                .name(product.name())
                .description(product.description())
                .gramsPerUnit(product.gramsPerUnit())
                .stockInUnits(product.stockInUnits())
                .createdAt(product.createdAt())
                .nutritionBasis(product.nutritionBasis() == null ? NutritionBasis.PER_100_GRAMS : NutritionBasis.valueOf(product.nutritionBasis()))
                .defaultPrice(product.defaultPrice())
                .photo(product.mediaId() == null ? null : Media.builder()
                        .id(product.mediaId())
                        .build())
                .supermarkets(supermarkets == null ? List.of() : supermarkets)
                .nutritionalValues(NutritionalValues.builder()
                        .productId(values.productId())
                        .calories(values.calories())
                        .carbohydrates(values.carbohydrates())
                        .proteins(values.proteins())
                        .fats(values.fats())
                        .build())
                .build();
    }

    private NutritionalValuesEntity upsertNutritionalValues(Long productId, NutritionalValues values) {
        NutritionalValuesEntity entity = toEntity(productId, values);
        jdbcTemplate.update("""
                        INSERT INTO nutritional_values (product_id, calories, carbohydrates, proteins, fats)
                        VALUES (:productId, :calories, :carbohydrates, :proteins, :fats)
                        ON CONFLICT (product_id) DO UPDATE SET
                            calories = EXCLUDED.calories,
                            carbohydrates = EXCLUDED.carbohydrates,
                            proteins = EXCLUDED.proteins,
                            fats = EXCLUDED.fats
                        """,
                new MapSqlParameterSource()
                        .addValue("productId", entity.productId())
                        .addValue("calories", entity.calories())
                        .addValue("carbohydrates", entity.carbohydrates())
                        .addValue("proteins", entity.proteins())
                        .addValue("fats", entity.fats()));
        return entity;
    }

    private RowMapper<Product> productRowMapper() {
        return (rs, rowNum) -> Product.builder()
                .id(rs.getLong("id"))
                .name(rs.getString("name"))
                .description(rs.getString("description"))
                .gramsPerUnit(rs.getBigDecimal("grams_per_unit"))
                .stockInUnits(rs.getBoolean("is_stock_in_units"))
                .createdAt(rs.getTimestamp("created_at").toInstant())
                .nutritionBasis(NutritionBasis.valueOf(rs.getString("nutrition_basis")))
                .defaultPrice(rs.getBigDecimal("default_price"))
                .photo(mapMedia(rs))
                .nutritionalValues(NutritionalValues.builder()
                        .productId(rs.getLong("product_id"))
                        .calories(rs.getBigDecimal("calories"))
                        .carbohydrates(rs.getBigDecimal("carbohydrates"))
                        .proteins(rs.getBigDecimal("proteins"))
                        .fats(rs.getBigDecimal("fats"))
                        .build())
                .build();
    }

    private Product attachSupermarkets(Product product) {
        return product.toBuilder()
                .supermarkets(findSupermarkets(product.getId()))
                .build();
    }

    private List<Supermarket> findSupermarkets(Long productId) {
        return jdbcTemplate.query("""
                        SELECT s.id, s.name
                        FROM supermarkets s
                        INNER JOIN product_supermarkets ps ON ps.supermarket_id = s.id
                        WHERE ps.product_id = :productId
                        ORDER BY LOWER(s.name), s.id
                        """,
                new MapSqlParameterSource("productId", productId),
                (rs, rowNum) -> Supermarket.builder()
                        .id(rs.getLong("id"))
                        .name(rs.getString("name"))
                        .build()
        );
    }

    private void replaceSupermarkets(Long productId, List<Supermarket> supermarkets) {
        jdbcTemplate.update(
                "DELETE FROM product_supermarkets WHERE product_id = :productId",
                new MapSqlParameterSource("productId", productId)
        );
        if (supermarkets == null) {
            return;
        }
        supermarkets.stream()
                .map(Supermarket::getId)
                .distinct()
                .forEach(supermarketId -> jdbcTemplate.update("""
                                INSERT INTO product_supermarkets (product_id, supermarket_id)
                                VALUES (:productId, :supermarketId)
                                """,
                        new MapSqlParameterSource()
                                .addValue("productId", productId)
                                .addValue("supermarketId", supermarketId)
                ));
    }

    private Long mediaId(Product product) {
        return product.getPhoto() == null ? null : product.getPhoto().getId();
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

    private void appendFilters(StringBuilder sql, MapSqlParameterSource params, ProductSearchCriteria searchCriteria) {
        List<String> conditions = new ArrayList<>();
        if (searchCriteria != null && searchCriteria.hasFilters()) {
            if (searchCriteria.search() != null) {
                conditions.add("(LOWER(p.name) LIKE :search OR LOWER(p.description) LIKE :search)");
                params.addValue("search", "%" + searchCriteria.search() + "%");
            }
            addRangeCondition(conditions, params, "calories", "nv.calories", searchCriteria.caloriesMin(), searchCriteria.caloriesMax());
            addRangeCondition(conditions, params, "carbohydrates", "nv.carbohydrates", searchCriteria.carbohydratesMin(), searchCriteria.carbohydratesMax());
            addRangeCondition(conditions, params, "proteins", "nv.proteins", searchCriteria.proteinsMin(), searchCriteria.proteinsMax());
            addRangeCondition(conditions, params, "fats", "nv.fats", searchCriteria.fatsMin(), searchCriteria.fatsMax());
        }
        if (!conditions.isEmpty()) {
            sql.append(" WHERE ").append(String.join(" AND ", conditions));
        }
    }

    private void addRangeCondition(
            List<String> conditions,
            MapSqlParameterSource params,
            String filterName,
            String column,
            java.math.BigDecimal min,
            java.math.BigDecimal max
    ) {
        if (min != null) {
            conditions.add(column + " >= :" + filterName + "Min");
            params.addValue(filterName + "Min", min);
        }
        if (max != null) {
            conditions.add(column + " <= :" + filterName + "Max");
            params.addValue(filterName + "Max", max);
        }
    }
}
