package com.eliascanalesnieto.foodhelper.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.util.Arrays;
import java.util.Base64;
import java.util.Map;
import java.util.function.Function;
import javax.imageio.ImageIO;
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

        APIGatewayProxyRequestEvent listRecipes = new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath("/api/v1/recipes")
                .withHeaders(authHeaders(token));

        APIGatewayProxyResponseEvent listRecipesResponse = productHttpHandler.apply(listRecipes);
        assertThat(listRecipesResponse.getStatusCode()).isEqualTo(200);
        assertThat(listRecipesResponse.getBody()).contains("Curry");

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
    void shouldHandleProductPhotoAndMediaDownloadRequests() {
        String token = registerAndReadToken();
        APIGatewayProxyRequestEvent create = new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/api/v1/products")
                .withHeaders(authHeaders(token))
                .withBody("""
                        {"name":"Photo Apple","description":"Fresh apple","gramsPerUnit":150,"calories":52,"carbohydrates":14,"proteins":0.3,"fats":0.2,"photo":{"fileName":"apple.png","contentType":"image/png","base64Data":"%s"}}
                        """.formatted(samplePhotoBase64()));

        APIGatewayProxyResponseEvent createResponse = productHttpHandler.apply(create);
        assertThat(createResponse.getStatusCode()).isEqualTo(201);
        String photo = readText(createResponse.getBody(), "photo");
        assertThat(photo).startsWith("/api/v1/media/");
        assertThat(photo).contains("signature=");
        URI photoUri = URI.create(photo);

        APIGatewayProxyRequestEvent mediaDownload = new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath(photoUri.getPath())
                .withQueryStringParameters(queryParams(photoUri.getQuery()));

        APIGatewayProxyResponseEvent mediaResponse = productHttpHandler.apply(mediaDownload);
        assertThat(mediaResponse.getStatusCode()).isEqualTo(200);
        assertThat(mediaResponse.getHeaders()).containsEntry("Content-Type", "image/jpeg");
        assertThat(mediaResponse.getIsBase64Encoded()).isTrue();
        assertThat(Base64.getDecoder().decode(mediaResponse.getBody())).isNotEmpty();
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
        assertThat(readText(missingCodeResponse.getBody(), "message")).contains("registrationCode");

        APIGatewayProxyRequestEvent blankCodeRegister = new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/api/v1/auth/register")
                .withBody("{\"username\":\"" + username + "-blank\",\"password\":\"secret-password\",\"registrationCode\":\"\"}");

        APIGatewayProxyResponseEvent blankCodeResponse = productHttpHandler.apply(blankCodeRegister);
        assertThat(blankCodeResponse.getStatusCode()).isEqualTo(400);
        assertThat(readText(blankCodeResponse.getBody(), "message")).contains("registrationCode");
        assertThat(blankCodeResponse.getBody()).doesNotContain("\"type\":\"failure\"");
        assertThat(blankCodeResponse.getBody()).doesNotContain("\"data\"");

        APIGatewayProxyRequestEvent invalidCodeRegister = new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/api/v1/auth/register")
                .withBody("{\"username\":\"" + username + "-invalid\",\"password\":\"secret-password\",\"registrationCode\":\"wrong-code\"}");

        APIGatewayProxyResponseEvent invalidCodeResponse = productHttpHandler.apply(invalidCodeRegister);
        assertThat(invalidCodeResponse.getStatusCode()).isEqualTo(400);
        assertThat(readText(invalidCodeResponse.getBody(), "message")).isEqualTo("Invalid registration code");

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
            JsonNode node = readNode(json, field);
            return node.asLong();
        } catch (Exception ex) {
            throw new AssertionError("Unable to read field '" + field + "' from response: " + json, ex);
        }
    }

    private Map<String, String> queryParams(String query) {
        return Arrays.stream(query.split("&"))
                .map(param -> param.split("=", 2))
                .collect(java.util.stream.Collectors.toMap(parts -> parts[0], parts -> parts[1]));
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
            JsonNode node = readNode(json, field);
            return node.asText();
        } catch (Exception ex) {
            throw new AssertionError("Unable to read field '" + field + "' from response: " + json, ex);
        }
    }

    private JsonNode readNode(String json, String path) throws Exception {
        JsonNode current = OBJECT_MAPPER.readTree(json);
        for (String segment : path.split("\\.")) {
            current = current.get(segment);
            if (current == null) {
                throw new AssertionError("Path segment not found: " + segment);
            }
        }
        return current;
    }

    private Map<String, String> authHeaders(String token) {
        return Map.of("Authorization", "Bearer " + token);
    }

    private String samplePhotoBase64() {
        try {
            BufferedImage image = new BufferedImage(1600, 1000, BufferedImage.TYPE_INT_RGB);
            for (int x = 0; x < image.getWidth(); x++) {
                for (int y = 0; y < image.getHeight(); y++) {
                    int red = (x * 255) / image.getWidth();
                    int green = (y * 255) / image.getHeight();
                    int blue = (red + green) / 2;
                    image.setRGB(x, y, new Color(red, green, blue).getRGB());
                }
            }
            ByteArrayOutputStream output = new ByteArrayOutputStream();
            ImageIO.write(image, "png", output);
            return Base64.getEncoder().encodeToString(output.toByteArray());
        } catch (Exception ex) {
            throw new AssertionError("Unable to build test image", ex);
        }
    }
}
