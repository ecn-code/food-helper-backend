package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import org.springdoc.core.models.GroupedOpenApi;
import org.springdoc.core.customizers.OpenApiCustomizer;
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
        },
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        scheme = "bearer"
)
public class OpenApiConfig {

    @Bean
    OpenApiCustomizer publicOperationsCustomizer() {
        return openApi -> {
            if (openApi.getPaths() == null) {
                return;
            }
            markPublic(openApi, "/api/v1/auth/register");
            markPublic(openApi, "/api/v1/auth/login");
            markPublic(openApi, "/api/v1/health");
            markPublic(openApi, "/api/v1/media/{id}");
        };
    }

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
                .pathsToExclude("/api/v1/products/*/stock", "/api/v1/products/*/stock/**")
                .build();
    }

    @Bean
    GroupedOpenApi supermarketsApi() {
        return groupedApi("supermarkets", "Supermarkets", "/api/v1/supermarkets", "/api/v1/supermarkets/**").build();
    }

    @Bean
    GroupedOpenApi recipesApi() {
        return groupedApi("recipes", "Recipes", "/api/v1/recipes", "/api/v1/recipes/**").build();
    }

    @Bean
    GroupedOpenApi stockApi() {
        return groupedApi("stock", "Stock", "/api/v1/stock", "/api/v1/stock/**", "/api/v1/products/*/stock", "/api/v1/products/*/stock/**").build();
    }

    @Bean
    GroupedOpenApi usersApi() {
        return groupedApi("users", "Users", "/api/v1/users", "/api/v1/users/**").build();
    }

    @Bean
    GroupedOpenApi userWeightsApi() {
        return groupedApi("user-weights", "User weights", "/api/v1/users/*/weights", "/api/v1/users/*/weights/**").build();
    }

    @Bean
    GroupedOpenApi moneyBoxesApi() {
        return groupedApi("money-boxes", "Money boxes", "/api/v1/money-boxes", "/api/v1/money-boxes/**").build();
    }

    @Bean
    GroupedOpenApi couponsApi() {
        return groupedApi("coupons", "Coupons", "/api/v1/coupons", "/api/v1/coupons/**").build();
    }

    @Bean
    GroupedOpenApi challengesApi() {
        return groupedApi("challenges", "Challenges", "/api/v1/challenges", "/api/v1/challenges/**").build();
    }

    @Bean
    GroupedOpenApi planningApi() {
        return groupedApi(
                "planning",
                "Planning",
                "/api/v1/planning",
                "/api/v1/planning/**"
        ).build();
    }

    @Bean
    GroupedOpenApi menusApi() {
        return groupedApi(
                "menus",
                "Menus",
                "/api/v1/menus",
                "/api/v1/menus/**"
        ).build();
    }

    @Bean
    GroupedOpenApi nutritionalRulesApi() {
        return groupedApi("nutritional-rules", "Nutritional rules", "/api/v1/nutritional-rules").build();
    }

    private GroupedOpenApi.Builder groupedApi(String group, String displayName, String... pathsToMatch) {
        return GroupedOpenApi.builder()
                .group(group)
                .displayName(displayName)
                .pathsToMatch(pathsToMatch);
    }

    private void markPublic(io.swagger.v3.oas.models.OpenAPI openApi, String path) {
        io.swagger.v3.oas.models.PathItem pathItem = openApi.getPaths().get(path);
        if (pathItem != null) {
            pathItem.readOperations().forEach(operation -> operation.setSecurity(java.util.List.of()));
        }
    }
}
