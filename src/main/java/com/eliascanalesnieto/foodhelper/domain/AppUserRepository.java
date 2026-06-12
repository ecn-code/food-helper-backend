package com.eliascanalesnieto.foodhelper.domain;

import java.util.Optional;

public interface AppUserRepository {
    AppUser create(AppUser user);

    Optional<AppUser> findByUsername(String username);
}
