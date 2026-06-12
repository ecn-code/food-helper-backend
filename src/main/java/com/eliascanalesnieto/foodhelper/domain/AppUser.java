package com.eliascanalesnieto.foodhelper.domain;

import java.time.Instant;
import lombok.Builder;
import lombok.Value;

@Value
@Builder
public class AppUser {
    Long id;
    String username;
    String passwordHash;
    Instant createdAt;
}
