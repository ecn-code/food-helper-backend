package com.eliascanalesnieto.foodhelper.infra;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface RecipeCrudRepository extends CrudRepository<RecipeEntity, Long> {
    Optional<RecipeEntity> findByName(String name);
}
