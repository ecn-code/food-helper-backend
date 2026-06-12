package com.eliascanalesnieto.foodhelper.infra;

import java.util.Optional;
import org.springframework.data.repository.CrudRepository;

public interface AppUserCrudRepository extends CrudRepository<AppUserEntity, Long> {
    Optional<AppUserEntity> findByUsername(String username);
}
