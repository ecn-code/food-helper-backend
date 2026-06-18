package com.eliascanalesnieto.foodhelper.infra;

import com.eliascanalesnieto.foodhelper.domain.Media;
import com.eliascanalesnieto.foodhelper.domain.NutritionalValues;
import com.eliascanalesnieto.foodhelper.domain.Product;
import com.eliascanalesnieto.foodhelper.domain.ProductRepository;
import com.eliascanalesnieto.foodhelper.presentation.error.DuplicateResourceException;
import com.eliascanalesnieto.foodhelper.presentation.error.ResourceNotFoundException;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Collection;
import java.util.LinkedHashSet;
import java.util.List;
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
                   p.default_price,
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
            ProductEntity savedProduct = productRepository.save(new ProductEntity(
                    null,
                    product.getName(),
                    product.getDescription(),
                    product.getGramsPerUnit(),
                    product.getDefaultPrice(),
                    mediaId(product)
            ));
            NutritionalValuesEntity savedValues = upsertNutritionalValues(savedProduct.id(), product.getNutritionalValues());
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
            ProductEntity savedProduct = productRepository.save(new ProductEntity(
                    id,
                    product.getName(),
                    product.getDescription(),
                    product.getGramsPerUnit(),
                    product.getDefaultPrice(),
                    mediaId(product)
            ));
            NutritionalValuesEntity savedValues = upsertNutritionalValues(id, product.getNutritionalValues());
            return toDomain(savedProduct, savedValues);
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
        return products.getFirst();
    }

    @Override
    public List<Product> findAll() {
        return jdbcTemplate.query(SELECT_PRODUCTS_WITH_VALUES + " ORDER BY p.id", productRowMapper());
    }

    @Override
    public List<Product> findPage(int offset, int limit) {
        return jdbcTemplate.query(
                SELECT_PRODUCTS_WITH_VALUES + " ORDER BY p.id LIMIT :limit OFFSET :offset",
                new MapSqlParameterSource()
                        .addValue("limit", limit)
                        .addValue("offset", offset),
                productRowMapper()
        );
    }

    @Override
    public long count() {
        return productRepository.count();
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
                .gramsPerUnit(product.gramsPerUnit())
                .defaultPrice(product.defaultPrice())
                .photo(product.mediaId() == null ? null : Media.builder()
                        .id(product.mediaId())
                        .build())
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
}
