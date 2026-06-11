package com.eliascanalesnieto.foodhelper.infra;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface RecipeProductOriginCrudRepository extends CrudRepository<RecipeProductOriginEntity, Long> {
    Optional<RecipeProductOriginEntity> findByProductId(Long productId);
}
