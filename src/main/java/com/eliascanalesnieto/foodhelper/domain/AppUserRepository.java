package com.eliascanalesnieto.foodhelper.domain;

import java.util.Optional;
import java.util.List;

public interface AppUserRepository {
    AppUser create(AppUser user);

    AppUser findById(Long id);

    Optional<AppUser> findByUsername(String username);

    List<AppUser> findAll();
}
