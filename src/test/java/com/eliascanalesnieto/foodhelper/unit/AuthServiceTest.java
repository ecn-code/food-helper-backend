package com.eliascanalesnieto.foodhelper.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.eliascanalesnieto.foodhelper.application.AuthService;
import com.eliascanalesnieto.foodhelper.application.JwtService;
import com.eliascanalesnieto.foodhelper.application.PasswordHasher;
import com.eliascanalesnieto.foodhelper.domain.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private AppUserRepository userRepository;

    @Mock
    private PasswordHasher passwordHasher;

    @Mock
    private JwtService jwtService;

    @Test
    void shouldCreateServiceWithoutExposingRegistrationCode() {
        AuthService service = new AuthService(userRepository, passwordHasher, jwtService, "12345");

        assertThat(service).isNotNull();
    }

    @Test
    void shouldRejectInvalidRegistrationCode() {
        AuthService service = new AuthService(userRepository, passwordHasher, jwtService, "12345");

        assertThatThrownBy(() -> service.register("elias", "secret-password", "wrong"))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("Invalid registration code");
    }
}
