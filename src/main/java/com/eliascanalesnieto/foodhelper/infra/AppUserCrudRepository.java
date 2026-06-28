package com.eliascanalesnieto.foodhelper.infra;

import java.util.Optional;
import java.util.List;
import org.springframework.data.repository.CrudRepository;

public interface AppUserCrudRepository extends CrudRepository<AppUserEntity, Long> {
    Optional<AppUserEntity> findByUsername(String username);

    List<AppUserEntity> findAllByOrderByUsernameAsc();
}
