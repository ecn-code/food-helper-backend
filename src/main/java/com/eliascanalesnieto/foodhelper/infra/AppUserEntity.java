package com.eliascanalesnieto.foodhelper.infra;

import java.time.Instant;
import org.springframework.data.annotation.Id;
import org.springframework.data.relational.core.mapping.Column;
import org.springframework.data.relational.core.mapping.Table;

@Table("app_users")
public record AppUserEntity(
        @Id Long id,
        @Column("username") String username,
        @Column("password_hash") String passwordHash,
        @Column("created_at") Instant createdAt
) {
}
