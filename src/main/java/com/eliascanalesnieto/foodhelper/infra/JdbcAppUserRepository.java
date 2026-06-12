package com.eliascanalesnieto.foodhelper.infra;

import com.eliascanalesnieto.foodhelper.domain.AppUser;
import com.eliascanalesnieto.foodhelper.domain.AppUserRepository;
import com.eliascanalesnieto.foodhelper.presentation.error.DuplicateResourceException;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class JdbcAppUserRepository implements AppUserRepository {
    private final AppUserCrudRepository userRepository;

    @Override
    public AppUser create(AppUser user) {
        try {
            return toDomain(userRepository.save(new AppUserEntity(
                    null,
                    user.getUsername(),
                    user.getPasswordHash(),
                    user.getCreatedAt()
            )));
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateResourceException("Username already exists");
        }
    }

    @Override
    public Optional<AppUser> findByUsername(String username) {
        return userRepository.findByUsername(username).map(this::toDomain);
    }

    private AppUser toDomain(AppUserEntity entity) {
        return AppUser.builder()
                .id(entity.id())
                .username(entity.username())
                .passwordHash(entity.passwordHash())
                .createdAt(entity.createdAt())
                .build();
    }
}
