package com.eliascanalesnieto.foodhelper.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import java.util.function.Function;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.NONE)
@ActiveProfiles({"lambda", "test-integration"})
class ProductLambdaRouterIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("foodhelper")
            .withUsername("foodhelper")
            .withPassword("foodhelper")
            .withInitScript("db/migration/V1__create_products.sql");

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "true");
        registry.add("spring.flyway.url", postgres::getJdbcUrl);
        registry.add("spring.flyway.user", postgres::getUsername);
        registry.add("spring.flyway.password", postgres::getPassword);
    }

    @Autowired
    private Function<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> productHttpHandler;

    @Test
    void shouldHandleApiGatewayStyleRequests() {
        APIGatewayProxyRequestEvent create = new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/api/v1/products")
                .withBody("{\"name\":\"Apple\",\"description\":\"Fresh apple\",\"calories\":52,\"carbohydrates\":14,\"proteins\":0.3,\"fats\":0.2}");

        APIGatewayProxyResponseEvent createResponse = productHttpHandler.apply(create);
        assertThat(createResponse.getStatusCode()).isEqualTo(201);
        assertThat(createResponse.getBody()).contains("Apple");
        assertThat(createResponse.getBody()).contains("Fresh apple");

        APIGatewayProxyRequestEvent health = new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath("/api/v1/health");

        APIGatewayProxyResponseEvent healthResponse = productHttpHandler.apply(health);
        assertThat(healthResponse.getStatusCode()).isEqualTo(200);
        assertThat(healthResponse.getBody()).contains("UP");
    }
}
