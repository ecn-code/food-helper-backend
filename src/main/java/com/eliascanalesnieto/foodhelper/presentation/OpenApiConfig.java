package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "FoodHelper API",
                version = "v1",
                description = "API for managing products, recipes, and nutritional calculations.",
                contact = @Contact(name = "FoodHelper"),
                license = @License(name = "Proprietary")
        ),
        servers = {
                @Server(url = "/", description = "Current server")
        }
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class OpenApiConfig {

    @Bean
    GroupedOpenApi authApi() {
        return groupedApi("auth", "Authentication", "/api/v1/auth/**").build();
    }

    @Bean
    GroupedOpenApi healthApi() {
        return groupedApi("health", "Health", "/api/v1/health").build();
    }

    @Bean
    GroupedOpenApi mediaApi() {
        return groupedApi("media", "Media", "/api/v1/media/**").build();
    }

    @Bean
    GroupedOpenApi productsApi() {
        return groupedApi("products", "Products", "/api/v1/products", "/api/v1/products/**")
                .pathsToExclude("/api/v1/products/*/stock")
                .build();
    }

    @Bean
    GroupedOpenApi recipesApi() {
        return groupedApi("recipes", "Recipes", "/api/v1/recipes", "/api/v1/recipes/**").build();
    }

    @Bean
    GroupedOpenApi stockApi() {
        return groupedApi("stock", "Stock", "/api/v1/stock", "/api/v1/stock/**", "/api/v1/products/*/stock").build();
    }

    @Bean
    GroupedOpenApi proposedWeekMenusApi() {
        return groupedApi(
                "proposed-week-menus",
                "Proposed week menus",
                "/api/v1/proposed-week-menus",
                "/api/v1/proposed-week-menus/**",
                "/api/v1/proposed-week-menu-day-parts",
                "/api/v1/proposed-week-menu-day-parts/**"
        ).build();
    }

    @Bean
    GroupedOpenApi currentWeekMenusApi() {
        return groupedApi(
                "established-week-menus",
                "Established week menus",
                "/api/v1/established-week-menus",
                "/api/v1/established-week-menus/**"
        ).build();
    }

    private GroupedOpenApi.Builder groupedApi(String group, String displayName, String... pathsToMatch) {
        return GroupedOpenApi.builder()
                .group(group)
                .displayName(displayName)
                .pathsToMatch(pathsToMatch);
    }
}
