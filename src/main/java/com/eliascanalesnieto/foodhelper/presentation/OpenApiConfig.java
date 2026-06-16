package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.annotations.servers.Server;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;

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
}
