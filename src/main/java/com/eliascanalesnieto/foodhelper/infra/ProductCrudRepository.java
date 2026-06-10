package com.eliascanalesnieto.foodhelper.infra;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface ProductCrudRepository extends CrudRepository<ProductEntity, Long> {
    Optional<ProductEntity> findByName(String name);
}
