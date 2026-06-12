package com.eliascanalesnieto.foodhelper.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Map;
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
    private static final String REGISTRATION_CODE = "test-registration-code";

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
        String token = registerAndReadToken();
        APIGatewayProxyRequestEvent create = new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/api/v1/products")
                .withHeaders(authHeaders(token))
                .withBody("{\"name\":\"Apple\",\"description\":\"Fresh apple\",\"gramsPerUnit\":150,\"calories\":52,\"carbohydrates\":14,\"proteins\":0.3,\"fats\":0.2}");

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
        String token = registerAndReadToken();
        APIGatewayProxyRequestEvent createIngredient = new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/api/v1/products")
                .withHeaders(authHeaders(token))
                .withBody("{\"name\":\"Chicken breast\",\"description\":\"Chicken\",\"gramsPerUnit\":100,\"calories\":165,\"carbohydrates\":0,\"proteins\":31,\"fats\":3.6}");

        APIGatewayProxyResponseEvent ingredientResponse = productHttpHandler.apply(createIngredient);
        assertThat(ingredientResponse.getStatusCode()).isEqualTo(201);
        long ingredientId = readLong(ingredientResponse.getBody(), "id");

        APIGatewayProxyRequestEvent createRecipe = new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/api/v1/recipes")
                .withHeaders(authHeaders(token))
                .withBody("{\"name\":\"Curry\",\"description\":\"Homemade curry\",\"instructions\":\"Cook slowly.\",\"products\":[{\"productId\":" + ingredientId + ",\"grams\":200}]}");

        APIGatewayProxyResponseEvent recipeResponse = productHttpHandler.apply(createRecipe);
        assertThat(recipeResponse.getStatusCode()).isEqualTo(201);
        assertThat(recipeResponse.getBody()).contains("Curry");
        assertThat(recipeResponse.getBody()).contains("330.00");
        long recipeId = readLong(recipeResponse.getBody(), "id");

        APIGatewayProxyRequestEvent createDerivedProduct = new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/api/v1/recipes/" + recipeId + "/derived-product")
                .withHeaders(authHeaders(token))
                .withBody("{\"producedGrams\":400,\"gramsPerUnit\":100}");

        APIGatewayProxyResponseEvent derivedProductResponse = productHttpHandler.apply(createDerivedProduct);
        assertThat(derivedProductResponse.getStatusCode()).isEqualTo(201);
        assertThat(derivedProductResponse.getBody()).contains("4.00");
    }

    @Test
    void shouldHandleStockRequests() {
        String token = registerAndReadToken();
        APIGatewayProxyRequestEvent createProduct = new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/api/v1/products")
                .withHeaders(authHeaders(token))
                .withBody("{\"name\":\"Stock Apple\",\"description\":\"Fresh apple\",\"gramsPerUnit\":150,\"calories\":52,\"carbohydrates\":14,\"proteins\":0.3,\"fats\":0.2}");

        APIGatewayProxyResponseEvent productResponse = productHttpHandler.apply(createProduct);
        assertThat(productResponse.getStatusCode()).isEqualTo(201);
        long productId = readLong(productResponse.getBody(), "id");

        APIGatewayProxyRequestEvent createStock = new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/api/v1/products/" + productId + "/stock")
                .withHeaders(authHeaders(token))
                .withBody("{\"quantity\":5,\"expirationDate\":\"2026-06-20\",\"entryDate\":\"2026-06-10\"}");

        APIGatewayProxyResponseEvent createStockResponse = productHttpHandler.apply(createStock);
        assertThat(createStockResponse.getStatusCode()).isEqualTo(201);
        assertThat(createStockResponse.getBody()).contains("\"quantity\":5");
        long stockEntryId = readLong(createStockResponse.getBody(), "id");

        APIGatewayProxyRequestEvent addStock = new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/api/v1/stock/" + stockEntryId + "/add")
                .withHeaders(authHeaders(token))
                .withBody("{\"quantity\":2}");

        APIGatewayProxyResponseEvent addStockResponse = productHttpHandler.apply(addStock);
        assertThat(addStockResponse.getStatusCode()).isEqualTo(200);
        assertThat(addStockResponse.getBody()).contains("\"quantity\":7");

        APIGatewayProxyRequestEvent listStock = new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath("/api/v1/stock")
                .withHeaders(authHeaders(token));

        APIGatewayProxyResponseEvent listStockResponse = productHttpHandler.apply(listStock);
        assertThat(listStockResponse.getStatusCode()).isEqualTo(200);
        assertThat(listStockResponse.getBody()).contains("Stock Apple");

        APIGatewayProxyRequestEvent removeStock = new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/api/v1/stock/" + stockEntryId + "/remove")
                .withHeaders(authHeaders(token))
                .withBody("{\"quantity\":7}");

        APIGatewayProxyResponseEvent removeStockResponse = productHttpHandler.apply(removeStock);
        assertThat(removeStockResponse.getStatusCode()).isEqualTo(200);
    }

    @Test
    void shouldRegisterLoginAndRejectMissingToken() {
        String username = "lambda-user-" + System.nanoTime();
        APIGatewayProxyRequestEvent missingCodeRegister = new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/api/v1/auth/register")
                .withBody("{\"username\":\"" + username + "-missing\",\"password\":\"secret-password\"}");

        APIGatewayProxyResponseEvent missingCodeResponse = productHttpHandler.apply(missingCodeRegister);
        assertThat(missingCodeResponse.getStatusCode()).isEqualTo(400);

        APIGatewayProxyRequestEvent invalidCodeRegister = new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/api/v1/auth/register")
                .withBody("{\"username\":\"" + username + "-invalid\",\"password\":\"secret-password\",\"registrationCode\":\"wrong-code\"}");

        APIGatewayProxyResponseEvent invalidCodeResponse = productHttpHandler.apply(invalidCodeRegister);
        assertThat(invalidCodeResponse.getStatusCode()).isEqualTo(400);

        APIGatewayProxyRequestEvent register = new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/api/v1/auth/register")
                .withBody("{\"username\":\"" + username + "\",\"password\":\"secret-password\",\"registrationCode\":\"" + REGISTRATION_CODE + "\"}");

        APIGatewayProxyResponseEvent registerResponse = productHttpHandler.apply(register);
        assertThat(registerResponse.getStatusCode()).isEqualTo(201);
        assertThat(registerResponse.getBody()).contains("\"tokenType\":\"Bearer\"");
        assertThat(readText(registerResponse.getBody(), "accessToken")).isNotBlank();

        APIGatewayProxyRequestEvent login = new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/api/v1/auth/login")
                .withBody("{\"username\":\"" + username + "\",\"password\":\"secret-password\"}");

        APIGatewayProxyResponseEvent loginResponse = productHttpHandler.apply(login);
        assertThat(loginResponse.getStatusCode()).isEqualTo(200);
        assertThat(readText(loginResponse.getBody(), "accessToken")).isNotBlank();

        APIGatewayProxyRequestEvent unauthorized = new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath("/api/v1/stock");

        APIGatewayProxyResponseEvent unauthorizedResponse = productHttpHandler.apply(unauthorized);
        assertThat(unauthorizedResponse.getStatusCode()).isEqualTo(401);
    }

    private long readLong(String json, String field) {
        try {
            JsonNode root = OBJECT_MAPPER.readTree(json);
            return root.get(field).asLong();
        } catch (Exception ex) {
            throw new AssertionError("Unable to read field '" + field + "' from response: " + json, ex);
        }
    }

    private String registerAndReadToken() {
        APIGatewayProxyRequestEvent register = new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/api/v1/auth/register")
                .withBody("{\"username\":\"lambda-user-" + System.nanoTime() + "\",\"password\":\"secret-password\",\"registrationCode\":\"" + REGISTRATION_CODE + "\"}");
        APIGatewayProxyResponseEvent response = productHttpHandler.apply(register);
        assertThat(response.getStatusCode()).isEqualTo(201);
        return readText(response.getBody(), "accessToken");
    }

    private String readText(String json, String field) {
        try {
            JsonNode root = OBJECT_MAPPER.readTree(json);
            return root.get(field).asText();
        } catch (Exception ex) {
            throw new AssertionError("Unable to read field '" + field + "' from response: " + json, ex);
        }
    }

    private Map<String, String> authHeaders(String token) {
        return Map.of("Authorization", "Bearer " + token);
    }
}
