package com.eliascanalesnieto.foodhelper.unit;

import static org.assertj.core.api.Assertions.assertThat;

import com.eliascanalesnieto.foodhelper.application.AuthService;
import com.eliascanalesnieto.foodhelper.application.JwtService;
import com.eliascanalesnieto.foodhelper.application.PasswordHasher;
import com.eliascanalesnieto.foodhelper.domain.AppUserRepository;
import java.io.ByteArrayOutputStream;
import java.io.PrintStream;
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
    void shouldPrintRegistrationCodeDuringConstruction() {
        PrintStream originalOut = System.out;
        ByteArrayOutputStream captured = new ByteArrayOutputStream();
        System.setOut(new PrintStream(captured));

        try {
            new AuthService(userRepository, passwordHasher, jwtService, "12345");
        } finally {
            System.setOut(originalOut);
        }

        assertThat(captured.toString()).contains("[DEBUG] APP_AUTH_REGISTRATION_CODE=12345");
    }
}
