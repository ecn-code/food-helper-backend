package com.eliascanalesnieto.foodhelper.infra;

import java.util.Collection;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jdbc.repository.query.Query;
import org.springframework.data.repository.CrudRepository;

public interface NutritionalValuesCrudRepository extends CrudRepository<NutritionalValuesEntity, Long> {
    @Query("SELECT product_id, calories, carbohydrates, proteins, fats FROM nutritional_values WHERE product_id = :productId")
    Optional<NutritionalValuesEntity> findByProductId(Long productId);

    @Query("SELECT product_id, calories, carbohydrates, proteins, fats FROM nutritional_values WHERE product_id IN (:productIds)")
    List<NutritionalValuesEntity> findAllByProductIdIn(Collection<Long> productIds);
}
