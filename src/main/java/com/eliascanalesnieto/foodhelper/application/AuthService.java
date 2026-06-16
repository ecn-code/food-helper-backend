package com.eliascanalesnieto.foodhelper.application;

import com.eliascanalesnieto.foodhelper.domain.AppUser;
import com.eliascanalesnieto.foodhelper.domain.AppUserRepository;
import com.eliascanalesnieto.foodhelper.presentation.AuthResponse;
import java.time.Instant;
import java.util.Locale;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Service
public class AuthService {
    private final AppUserRepository userRepository;
    private final PasswordHasher passwordHasher;
    private final JwtService jwtService;
    private final String registrationCode;

    public AuthService(
            AppUserRepository userRepository,
            PasswordHasher passwordHasher,
            JwtService jwtService,
            @Value("${app.auth.registration-code}") String registrationCode
    ) {
        this.userRepository = userRepository;
        this.passwordHasher = passwordHasher;
        this.jwtService = jwtService;
        this.registrationCode = registrationCode;
        System.out.println("[DEBUG] APP_AUTH_REGISTRATION_CODE=" + registrationCode);
    }

    @Transactional
    public AuthResponse register(String username, String password, String registrationCode) {
        validateRegistrationCode(registrationCode);
        validatePassword(password);
        AppUser created = userRepository.create(AppUser.builder()
                .username(normalizeUsername(username))
                .passwordHash(passwordHasher.hash(password))
                .createdAt(Instant.now())
                .build());
        return toResponse(created);
    }

    public AuthResponse login(String username, String password) {
        if (!StringUtils.hasText(password)) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        AppUser user = userRepository.findByUsername(normalizeUsername(username))
                .orElseThrow(() -> new IllegalArgumentException("Invalid username or password"));
        if (!passwordHasher.matches(password, user.getPasswordHash())) {
            throw new IllegalArgumentException("Invalid username or password");
        }
        return toResponse(user);
    }

    private AuthResponse toResponse(AppUser user) {
        JwtService.TokenIssue token = jwtService.issue(user);
        return new AuthResponse(user.getId(), user.getUsername(), token.token(), "Bearer", token.expiresAt());
    }

    private String normalizeUsername(String username) {
        if (!StringUtils.hasText(username)) {
            throw new IllegalArgumentException("Username is required");
        }
        return username.trim().toLowerCase(Locale.ROOT);
    }

    private void validatePassword(String password) {
        if (!StringUtils.hasText(password) || password.length() < 8 || password.length() > 128) {
            throw new IllegalArgumentException("Password must contain between 8 and 128 characters");
        }
    }

    private void validateRegistrationCode(String providedRegistrationCode) {
        if (!StringUtils.hasText(registrationCode)) {
            throw new IllegalStateException("Registration code is not configured");
        }
        if (!StringUtils.hasText(providedRegistrationCode)
                || !registrationCode.equals(providedRegistrationCode.trim())) {
            throw new IllegalArgumentException("Invalid registration code");
        }
    }
}
