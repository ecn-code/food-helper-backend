package com.eliascanalesnieto.foodhelper.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
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

    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    @Container
    static PostgreSQLContainer<?> postgres = TestContainerSupport.postgres("postgres:16-alpine");

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "false");
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

    @Test
    void shouldHandleRecipeRequests() {
        APIGatewayProxyRequestEvent createIngredient = new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/api/v1/products")
                .withBody("{\"name\":\"Chicken breast\",\"description\":\"Chicken\",\"calories\":165,\"carbohydrates\":0,\"proteins\":31,\"fats\":3.6}");

        APIGatewayProxyResponseEvent ingredientResponse = productHttpHandler.apply(createIngredient);
        assertThat(ingredientResponse.getStatusCode()).isEqualTo(201);
        long ingredientId = readLong(ingredientResponse.getBody(), "id");

        APIGatewayProxyRequestEvent createRecipe = new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/api/v1/recipes")
                .withBody("{\"name\":\"Curry\",\"description\":\"Homemade curry\",\"instructions\":\"Cook slowly.\",\"products\":[{\"productId\":" + ingredientId + ",\"grams\":200}]}");

        APIGatewayProxyResponseEvent recipeResponse = productHttpHandler.apply(createRecipe);
        assertThat(recipeResponse.getStatusCode()).isEqualTo(201);
        assertThat(recipeResponse.getBody()).contains("Curry");
        assertThat(recipeResponse.getBody()).contains("330.00");
        long recipeId = readLong(recipeResponse.getBody(), "id");

        APIGatewayProxyRequestEvent createDerivedProduct = new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/api/v1/recipes/" + recipeId + "/derived-product")
                .withBody("{\"producedGrams\":400,\"gramsPerUnit\":100}");

        APIGatewayProxyResponseEvent derivedProductResponse = productHttpHandler.apply(createDerivedProduct);
        assertThat(derivedProductResponse.getStatusCode()).isEqualTo(201);
        assertThat(derivedProductResponse.getBody()).contains("4.00");
    }

    @Test
    void shouldHandleStockRequests() {
        APIGatewayProxyRequestEvent createProduct = new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/api/v1/products")
                .withBody("{\"name\":\"Stock Apple\",\"description\":\"Fresh apple\",\"calories\":52,\"carbohydrates\":14,\"proteins\":0.3,\"fats\":0.2}");

        APIGatewayProxyResponseEvent productResponse = productHttpHandler.apply(createProduct);
        assertThat(productResponse.getStatusCode()).isEqualTo(201);
        long productId = readLong(productResponse.getBody(), "id");

        APIGatewayProxyRequestEvent createStock = new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/api/v1/products/" + productId + "/stock")
                .withBody("{\"quantity\":5,\"expirationDate\":\"2026-06-20\",\"entryDate\":\"2026-06-10\"}");

        APIGatewayProxyResponseEvent createStockResponse = productHttpHandler.apply(createStock);
        assertThat(createStockResponse.getStatusCode()).isEqualTo(201);
        assertThat(createStockResponse.getBody()).contains("\"quantity\":5");
        long stockEntryId = readLong(createStockResponse.getBody(), "id");

        APIGatewayProxyRequestEvent addStock = new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/api/v1/stock/" + stockEntryId + "/add")
                .withBody("{\"quantity\":2}");

        APIGatewayProxyResponseEvent addStockResponse = productHttpHandler.apply(addStock);
        assertThat(addStockResponse.getStatusCode()).isEqualTo(200);
        assertThat(addStockResponse.getBody()).contains("\"quantity\":7");

        APIGatewayProxyRequestEvent listStock = new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath("/api/v1/stock");

        APIGatewayProxyResponseEvent listStockResponse = productHttpHandler.apply(listStock);
        assertThat(listStockResponse.getStatusCode()).isEqualTo(200);
        assertThat(listStockResponse.getBody()).contains("Stock Apple");

        APIGatewayProxyRequestEvent removeStock = new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/api/v1/stock/" + stockEntryId + "/remove")
                .withBody("{\"quantity\":7}");

        APIGatewayProxyResponseEvent removeStockResponse = productHttpHandler.apply(removeStock);
        assertThat(removeStockResponse.getStatusCode()).isEqualTo(200);
    }

    private long readLong(String json, String field) {
        try {
            JsonNode root = OBJECT_MAPPER.readTree(json);
            return root.get(field).asLong();
        } catch (Exception ex) {
            throw new AssertionError("Unable to read field '" + field + "' from response: " + json, ex);
        }
    }
}
