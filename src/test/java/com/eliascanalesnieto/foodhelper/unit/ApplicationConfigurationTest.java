package com.eliascanalesnieto.foodhelper.unit;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import com.eliascanalesnieto.foodhelper.application.AuthService;
import com.eliascanalesnieto.foodhelper.application.JwtService;
import com.eliascanalesnieto.foodhelper.application.PasswordHasher;
import com.eliascanalesnieto.foodhelper.domain.AppUserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.boot.SpringBootConfiguration;
import org.springframework.boot.WebApplicationType;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Import;

class ApplicationConfigurationTest {

    @Test
    void shouldStartAuthServiceWithDefaultLocalRegistrationCode() {
        try (ConfigurableApplicationContext context = new SpringApplicationBuilder(TestApplication.class)
                .web(WebApplicationType.NONE)
                .properties("spring.main.banner-mode=off")
                .run()) {

            assertThat(context.getBean(AuthService.class)).isNotNull();
            assertThat(context.getEnvironment().getProperty("app.auth.registration-code"))
                    .isEqualTo("foodhelper-invite");
        }
    }

    @SpringBootConfiguration
    @Import(AuthService.class)
    static class TestApplication {

        @Bean
        AppUserRepository appUserRepository() {
            return mock(AppUserRepository.class);
        }

        @Bean
        PasswordHasher passwordHasher() {
            return mock(PasswordHasher.class);
        }

        @Bean
        JwtService jwtService() {
            return mock(JwtService.class);
        }
    }
}
