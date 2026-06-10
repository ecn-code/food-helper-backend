package com.eliascanalesnieto.foodhelper.presentation;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Contact;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.info.License;
import io.swagger.v3.oas.annotations.servers.Server;

@OpenAPIDefinition(
        info = @Info(
                title = "FoodHelper API",
                version = "v1",
                description = "API para gestionar productos y sus valores nutricionales.",
                contact = @Contact(name = "FoodHelper"),
                license = @License(name = "Proprietary")
        ),
        servers = {
                @Server(url = "/", description = "Servidor actual")
        }
)
public class OpenApiConfig {
}
