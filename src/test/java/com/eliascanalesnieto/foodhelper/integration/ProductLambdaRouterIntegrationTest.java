package com.eliascanalesnieto.foodhelper.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuDayPart;
import com.eliascanalesnieto.foodhelper.domain.ProposedWeekMenuDayPartRepository;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.net.URI;
import java.time.LocalDate;
import java.math.BigDecimal;
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
    static PostgreSQLContainer<?> postgres = postgres("postgres:16-alpine");

    @DynamicPropertySource
    static void configureDataSource(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.flyway.enabled", () -> "false");
    }

    @Autowired
    private Function<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> productHttpHandler;

    @Autowired
    private ProposedWeekMenuDayPartRepository dayPartRepository;

    @Test
    void shouldHandleApiGatewayStyleRequests() {
        AuthSession auth = registerAndReadAuth();
        String token = auth.token();
        APIGatewayProxyRequestEvent create = new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/api/v1/products")
                .withHeaders(authHeaders(token))
                .withBody("{\"name\":\"Apple\",\"description\":\"Fresh apple\",\"gramsPerUnit\":150,\"calories\":52,\"carbohydrates\":14,\"proteins\":0.3,\"fats\":0.2,\"defaultPrice\":2.49}");

        APIGatewayProxyResponseEvent createResponse = productHttpHandler.apply(create);
        assertThat(createResponse.getStatusCode()).isEqualTo(201);
        assertThat(createResponse.getBody()).contains("Apple");
        assertThat(createResponse.getBody()).contains("Fresh apple");
        assertThat(createResponse.getBody()).contains("\"defaultPrice\":2.49");

        APIGatewayProxyRequestEvent createSecond = new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/api/v1/products")
                .withHeaders(authHeaders(token))
                .withBody("{\"name\":\"Banana\",\"description\":\"Fresh banana\",\"gramsPerUnit\":120,\"calories\":89,\"carbohydrates\":23,\"proteins\":1.1,\"fats\":0.3}");

        APIGatewayProxyResponseEvent secondCreateResponse = productHttpHandler.apply(createSecond);
        assertThat(secondCreateResponse.getStatusCode()).isEqualTo(201);

        APIGatewayProxyRequestEvent listProducts = new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath("/api/v1/products")
                .withHeaders(authHeaders(token))
                .withQueryStringParameters(Map.of("page", "0", "size", "1"));

        APIGatewayProxyResponseEvent listProductsResponse = productHttpHandler.apply(listProducts);
        assertThat(listProductsResponse.getStatusCode()).isEqualTo(200);
        JsonNode productsPage = readNode(listProductsResponse.getBody());
        assertThat(productsPage.get("items").size()).isEqualTo(1);
        assertThat(productsPage.get("page").asInt()).isEqualTo(0);
        assertThat(productsPage.get("size").asInt()).isEqualTo(1);
        assertThat(productsPage.get("totalElements").asInt()).isGreaterThanOrEqualTo(2);
        assertThat(productsPage.get("totalPages").asInt()).isGreaterThanOrEqualTo(2);

        int totalElements = productsPage.get("totalElements").asInt();
        APIGatewayProxyResponseEvent previousProductPage = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath("/api/v1/products")
                .withHeaders(authHeaders(token))
                .withQueryStringParameters(Map.of("page", Integer.toString(totalElements - 2), "size", "1")));
        APIGatewayProxyResponseEvent lastProductPage = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath("/api/v1/products")
                .withHeaders(authHeaders(token))
                .withQueryStringParameters(Map.of("page", Integer.toString(totalElements - 1), "size", "1")));

        assertThat(readNode(previousProductPage.getBody()).get("items").get(0).get("name").asText()).isEqualTo("Apple");
        assertThat(readNode(lastProductPage.getBody()).get("items").get(0).get("name").asText()).isEqualTo("Banana");

        APIGatewayProxyRequestEvent health = new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath("/api/v1/health");

        APIGatewayProxyResponseEvent healthResponse = productHttpHandler.apply(health);
        assertThat(healthResponse.getStatusCode()).isEqualTo(200);
        assertThat(healthResponse.getBody()).contains("UP");
    }

    @Test
    void shouldHandleDailyAndWeeklyNutritionalRulesThroughLambda() {
        AuthSession auth = registerAndReadAuth();
        String token = auth.token();

        APIGatewayProxyResponseEvent saved = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("PUT")
                .withPath("/api/v1/nutritional-rules")
                .withHeaders(authHeaders(token))
                .withBody("""
                        {"daily":{"calories":{"minimum":100,"maximum":200},"carbohydrates":{"minimum":null,"maximum":20},"proteins":{"minimum":10,"maximum":null},"fats":{"minimum":null,"maximum":null}},"weekly":{"calories":{"minimum":120,"maximum":180},"carbohydrates":{"minimum":null,"maximum":20},"proteins":{"minimum":0,"maximum":20},"fats":{"minimum":null,"maximum":null}}}
                        """));
        assertThat(saved.getStatusCode()).isEqualTo(200);
        assertThat(readDecimal(saved.getBody(), "daily.calories.minimum")).isEqualByComparingTo("100");
        assertThat(readDecimal(saved.getBody(), "weekly.calories.minimum")).isEqualByComparingTo("120");

        APIGatewayProxyResponseEvent loaded = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath("/api/v1/nutritional-rules")
                .withHeaders(authHeaders(token)));
        assertThat(loaded.getStatusCode()).isEqualTo(200);
        assertThat(readDecimal(loaded.getBody(), "daily.calories.maximum")).isEqualByComparingTo("200");
        assertThat(readDecimal(loaded.getBody(), "weekly.proteins.maximum")).isEqualByComparingTo("20");
    }

    @Test
    void shouldManageSupermarketsAndProductAssignmentsThroughLambda() {
        AuthSession auth = registerAndReadAuth();
        String token = auth.token();
        String suffix = Long.toString(System.nanoTime());

        APIGatewayProxyResponseEvent supermarket = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/api/v1/supermarkets")
                .withHeaders(authHeaders(token))
                .withBody("{\"name\":\"Lambda Market " + suffix + "\"}"));
        assertThat(supermarket.getStatusCode()).isEqualTo(201);
        long supermarketId = readLong(supermarket.getBody(), "id");

        APIGatewayProxyResponseEvent product = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/api/v1/products")
                .withHeaders(authHeaders(token))
                .withBody("{\"name\":\"Lambda Assigned " + suffix + "\",\"description\":\"Assigned\",\"gramsPerUnit\":100,\"calories\":10,\"carbohydrates\":20,\"proteins\":30,\"fats\":4,\"supermarketIds\":[" + supermarketId + "]}"));
        assertThat(product.getStatusCode()).isEqualTo(201);
        assertThat(readLong(product.getBody(), "supermarkets.0.id")).isEqualTo(supermarketId);

        APIGatewayProxyResponseEvent listed = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath("/api/v1/supermarkets")
                .withHeaders(authHeaders(token)));
        assertThat(listed.getStatusCode()).isEqualTo(200);
        assertThat(listed.getBody()).contains("Lambda Market " + suffix);

        APIGatewayProxyResponseEvent renamed = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("PUT")
                .withPath("/api/v1/supermarkets/" + supermarketId)
                .withHeaders(authHeaders(token))
                .withBody("{\"name\":\"Lambda Market Renamed " + suffix + "\"}"));
        assertThat(renamed.getStatusCode()).isEqualTo(200);

        APIGatewayProxyResponseEvent rejectedDelete = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("DELETE")
                .withPath("/api/v1/supermarkets/" + supermarketId)
                .withHeaders(authHeaders(token)));
        assertThat(rejectedDelete.getStatusCode()).isEqualTo(409);
    }

    @Test
    void shouldFilterProductsInApiGatewayRequests() {
        AuthSession auth = registerAndReadAuth();
        String token = auth.token();
        String suffix = Long.toString(System.nanoTime());

        productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/api/v1/products")
                .withHeaders(authHeaders(token))
                .withBody("{\"name\":\"Lambda Filter Apple " + suffix + "\",\"description\":\"Searchable apple " + suffix + "\",\"gramsPerUnit\":150,\"calories\":52,\"carbohydrates\":14,\"proteins\":0.3,\"fats\":0.2}"));
        productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/api/v1/products")
                .withHeaders(authHeaders(token))
                .withBody("{\"name\":\"Lambda Filter Banana " + suffix + "\",\"description\":\"Searchable banana " + suffix + "\",\"gramsPerUnit\":120,\"calories\":89,\"carbohydrates\":23,\"proteins\":1.1,\"fats\":0.3}"));
        productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/api/v1/products")
                .withHeaders(authHeaders(token))
                .withBody("{\"name\":\"Lambda Filter Chicken " + suffix + "\",\"description\":\"Searchable chicken " + suffix + "\",\"gramsPerUnit\":100,\"calories\":165,\"carbohydrates\":0,\"proteins\":31,\"fats\":3.6}"));

        APIGatewayProxyResponseEvent searchOnly = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath("/api/v1/products")
                .withHeaders(authHeaders(token))
                .withQueryStringParameters(Map.of("search", suffix)));
        assertThat(searchOnly.getStatusCode()).isEqualTo(200);
        JsonNode searchOnlyPage = readNode(searchOnly.getBody());
        assertThat(searchOnlyPage.get("items").size()).isEqualTo(3);
        assertThat(searchOnlyPage.get("items").get(0).get("name").asText()).isEqualTo("Lambda Filter Apple " + suffix);
        assertThat(searchOnlyPage.get("items").get(1).get("name").asText()).isEqualTo("Lambda Filter Banana " + suffix);
        assertThat(searchOnlyPage.get("items").get(2).get("name").asText()).isEqualTo("Lambda Filter Chicken " + suffix);

        APIGatewayProxyResponseEvent caloriesFiltered = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath("/api/v1/products")
                .withHeaders(authHeaders(token))
                .withQueryStringParameters(Map.of("search", suffix, "caloriesMin", "80", "caloriesMax", "90")));
        assertThat(caloriesFiltered.getStatusCode()).isEqualTo(200);
        JsonNode caloriesFilteredPage = readNode(caloriesFiltered.getBody());
        assertThat(caloriesFilteredPage.get("items").size()).isEqualTo(1);
        assertThat(caloriesFilteredPage.get("items").get(0).get("name").asText()).isEqualTo("Lambda Filter Banana " + suffix);

        APIGatewayProxyResponseEvent combinedFilters = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath("/api/v1/products")
                .withHeaders(authHeaders(token))
                .withQueryStringParameters(Map.of(
                        "search", suffix,
                        "caloriesMin", "80",
                        "caloriesMax", "90",
                        "carbohydratesMin", "20",
                        "carbohydratesMax", "25"
                )));
        assertThat(combinedFilters.getStatusCode()).isEqualTo(200);
        JsonNode combinedPage = readNode(combinedFilters.getBody());
        assertThat(combinedPage.get("items").size()).isEqualTo(1);
        assertThat(combinedPage.get("items").get(0).get("name").asText()).isEqualTo("Lambda Filter Banana " + suffix);

        APIGatewayProxyResponseEvent paged = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath("/api/v1/products")
                .withHeaders(authHeaders(token))
                .withQueryStringParameters(Map.of("search", suffix, "page", "0", "size", "1")));
        APIGatewayProxyResponseEvent secondPage = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath("/api/v1/products")
                .withHeaders(authHeaders(token))
                .withQueryStringParameters(Map.of("search", suffix, "page", "1", "size", "1")));
        assertThat(readNode(paged.getBody()).get("items").get(0).get("name").asText()).isEqualTo("Lambda Filter Apple " + suffix);
        assertThat(readNode(secondPage.getBody()).get("items").get(0).get("name").asText()).isEqualTo("Lambda Filter Banana " + suffix);

        APIGatewayProxyResponseEvent empty = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath("/api/v1/products")
                .withHeaders(authHeaders(token))
                .withQueryStringParameters(Map.of("search", "no-match-" + suffix)));
        JsonNode emptyPage = readNode(empty.getBody());
        assertThat(emptyPage.get("items").size()).isZero();
        assertThat(emptyPage.get("totalElements").asInt()).isZero();
    }

    @Test
    void shouldHandleRecipeRequests() {
        AuthSession auth = registerAndReadAuth();
        String token = auth.token();
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

        APIGatewayProxyRequestEvent createSecondRecipe = new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/api/v1/recipes")
                .withHeaders(authHeaders(token))
                .withBody("{\"name\":\"Soup\",\"description\":\"Homemade soup\",\"instructions\":\"Cook gently.\",\"products\":[{\"productId\":" + ingredientId + ",\"grams\":100}]}");

        APIGatewayProxyResponseEvent secondRecipeResponse = productHttpHandler.apply(createSecondRecipe);
        assertThat(secondRecipeResponse.getStatusCode()).isEqualTo(201);

        APIGatewayProxyRequestEvent listRecipes = new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath("/api/v1/recipes")
                .withHeaders(authHeaders(token))
                .withQueryStringParameters(Map.of("page", "0", "size", "1"));

        APIGatewayProxyResponseEvent listRecipesResponse = productHttpHandler.apply(listRecipes);
        assertThat(listRecipesResponse.getStatusCode()).isEqualTo(200);
        JsonNode recipesPage = readNode(listRecipesResponse.getBody());
        assertThat(recipesPage.get("items").size()).isEqualTo(1);
        assertThat(recipesPage.get("page").asInt()).isEqualTo(0);
        assertThat(recipesPage.get("size").asInt()).isEqualTo(1);
        assertThat(recipesPage.get("totalElements").asInt()).isGreaterThanOrEqualTo(2);
        assertThat(recipesPage.get("totalPages").asInt()).isGreaterThanOrEqualTo(2);

        int totalElements = recipesPage.get("totalElements").asInt();
        APIGatewayProxyResponseEvent previousRecipePage = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath("/api/v1/recipes")
                .withHeaders(authHeaders(token))
                .withQueryStringParameters(Map.of("page", Integer.toString(totalElements - 2), "size", "1")));
        APIGatewayProxyResponseEvent lastRecipePage = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath("/api/v1/recipes")
                .withHeaders(authHeaders(token))
                .withQueryStringParameters(Map.of("page", Integer.toString(totalElements - 1), "size", "1")));

        assertThat(readNode(previousRecipePage.getBody()).get("items").get(0).get("name").asText()).isEqualTo("Curry");
        assertThat(readNode(lastRecipePage.getBody()).get("items").get(0).get("name").asText()).isEqualTo("Soup");

        APIGatewayProxyRequestEvent createDerivedProduct = new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/api/v1/recipes/" + recipeId + "/derived-product")
                .withHeaders(authHeaders(token))
                .withBody("{\"producedGrams\":400,\"gramsPerUnit\":100}");

        APIGatewayProxyResponseEvent derivedProductResponse = productHttpHandler.apply(createDerivedProduct);
        assertThat(derivedProductResponse.getStatusCode()).isEqualTo(201);
        assertThat(derivedProductResponse.getBody()).contains("4.00");

        APIGatewayProxyResponseEvent filteredRecipes = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath("/api/v1/recipes")
                .withHeaders(authHeaders(token))
                .withQueryStringParameters(Map.of(
                        "search", "chicken",
                        "caloriesMin", "300",
                        "caloriesMax", "350",
                        "hasDerivedProduct", "true"
                )));
        assertThat(filteredRecipes.getStatusCode()).isEqualTo(200);
        JsonNode filteredItems = readNode(filteredRecipes.getBody()).get("items");
        assertThat(filteredItems.size()).isOne();
        assertThat(filteredItems.get(0).get("id").asLong()).isEqualTo(recipeId);

        APIGatewayProxyResponseEvent invalidRange = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath("/api/v1/recipes")
                .withHeaders(authHeaders(token))
                .withQueryStringParameters(Map.of("fatsMin", "5", "fatsMax", "1")));
        assertThat(invalidRange.getStatusCode()).isEqualTo(400);
    }

    @Test
    void shouldHandleProductPhotoAndMediaDownloadRequests() {
        AuthSession auth = registerAndReadAuth();
        String token = auth.token();
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
        AuthSession auth = registerAndReadAuth();
        String token = auth.token();
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
                .withBody("{\"quantity\":5,\"price\":4.99,\"expirationDate\":\"2026-06-20\",\"entryDate\":\"2026-06-10\"}");

        APIGatewayProxyResponseEvent createStockResponse = productHttpHandler.apply(createStock);
        assertThat(createStockResponse.getStatusCode()).isEqualTo(201);
        assertThat(createStockResponse.getBody()).contains("\"quantity\":5");
        assertThat(createStockResponse.getBody()).contains("\"price\":4.99");
        long stockEntryId = readLong(createStockResponse.getBody(), "id");

        APIGatewayProxyResponseEvent updateStockResponse = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("PUT")
                .withPath("/api/v1/stock/" + stockEntryId)
                .withHeaders(authHeaders(token))
                .withBody("{\"quantity\":7.25,\"price\":5.49,\"expirationDate\":\"2026-06-25\",\"entryDate\":\"2026-06-11\"}"));
        assertThat(updateStockResponse.getStatusCode()).isEqualTo(200);
        assertThat(updateStockResponse.getBody()).contains("\"quantity\":7.25");
        assertThat(updateStockResponse.getBody()).contains("\"price\":5.49");

        APIGatewayProxyRequestEvent addStock = new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/api/v1/stock/" + stockEntryId + "/add")
                .withHeaders(authHeaders(token))
                .withBody("{\"quantity\":2}");

        APIGatewayProxyResponseEvent addStockResponse = productHttpHandler.apply(addStock);
        assertThat(addStockResponse.getStatusCode()).isEqualTo(200);
        assertThat(addStockResponse.getBody()).contains("\"quantity\":9.25");

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
    void shouldHandleStatsRequests() {
        AuthSession auth = registerAndReadAuth();
        String token = auth.token();

        APIGatewayProxyResponseEvent chickenResponse = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/api/v1/products")
                .withHeaders(authHeaders(token))
                .withBody("{\"name\":\"Lambda Chicken\",\"description\":\"Chicken\",\"gramsPerUnit\":100,\"calories\":165,\"carbohydrates\":0,\"proteins\":31,\"fats\":3.6}"));
        assertThat(chickenResponse.getStatusCode()).isEqualTo(201);
        long chickenId = readLong(chickenResponse.getBody(), "id");

        APIGatewayProxyResponseEvent riceResponse = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/api/v1/products")
                .withHeaders(authHeaders(token))
                .withBody("{\"name\":\"Lambda Rice\",\"description\":\"Rice\",\"gramsPerUnit\":100,\"calories\":130,\"carbohydrates\":28,\"proteins\":2.7,\"fats\":0.3}"));
        assertThat(riceResponse.getStatusCode()).isEqualTo(201);
        long riceId = readLong(riceResponse.getBody(), "id");

        APIGatewayProxyResponseEvent stockResponse = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/api/v1/products/" + chickenId + "/stock")
                .withHeaders(authHeaders(token))
                .withBody("{\"quantity\":2,\"price\":4.99,\"expirationDate\":\"2026-06-20\",\"entryDate\":\"2026-06-10\"}"));
        assertThat(stockResponse.getStatusCode()).isEqualTo(201);

        APIGatewayProxyResponseEvent recipeResponse = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/api/v1/recipes")
                .withHeaders(authHeaders(token))
                .withBody("{\"name\":\"Lambda Curry\",\"description\":\"Desc\",\"instructions\":\"Cook\",\"products\":[{\"productId\":" + chickenId + ",\"grams\":200},{\"productId\":" + riceId + ",\"grams\":150}]}"));
        assertThat(recipeResponse.getStatusCode()).isEqualTo(201);

        APIGatewayProxyResponseEvent productStatsResponse = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath("/api/v1/products/stats")
                .withHeaders(authHeaders(token)));
        assertThat(productStatsResponse.getStatusCode()).isEqualTo(200);
        assertThat(readText(productStatsResponse.getBody(), "caloriesTop.productName")).isEqualTo("Lambda Chicken");
        assertThat(readDecimal(productStatsResponse.getBody(), "stock.totalQuantity")).isEqualByComparingTo("2.00");

        APIGatewayProxyResponseEvent recipeStatsResponse = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath("/api/v1/recipes/stats")
                .withHeaders(authHeaders(token)));
        assertThat(recipeStatsResponse.getStatusCode()).isEqualTo(200);
        assertThat(readText(recipeStatsResponse.getBody(), "activeRecipes")).isEqualTo("1");
        assertThat(readDecimal(recipeStatsResponse.getBody(), "averageCalories")).isEqualByComparingTo("525.00");
    }

    @Test
    void shouldCloseAWeekAndExposeLambdaStats() {
        AuthSession auth = registerAndReadAuth();
        String token = auth.token();
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(8);
        LocalDate endDate = today.minusDays(1);

        APIGatewayProxyResponseEvent supermarketResponse = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/api/v1/supermarkets")
                .withHeaders(authHeaders(token))
                .withBody("{\"name\":\"Lambda Week Market\"}"));
        assertThat(supermarketResponse.getStatusCode()).isEqualTo(201);
        long supermarketId = readLong(supermarketResponse.getBody(), "id");

        APIGatewayProxyResponseEvent chickenResponse = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/api/v1/products")
                .withHeaders(authHeaders(token))
                .withBody("{\"name\":\"Lambda Week Chicken\",\"description\":\"Chicken\",\"gramsPerUnit\":100,\"calories\":165,\"carbohydrates\":0,\"proteins\":31,\"fats\":3.6,\"defaultPrice\":2.00}"));
        assertThat(chickenResponse.getStatusCode()).isEqualTo(201);
        long chickenId = readLong(chickenResponse.getBody(), "id");

        APIGatewayProxyResponseEvent riceResponse = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/api/v1/products")
                .withHeaders(authHeaders(token))
                .withBody("{\"name\":\"Lambda Week Rice\",\"description\":\"Rice\",\"gramsPerUnit\":100,\"calories\":130,\"carbohydrates\":28,\"proteins\":2.7,\"fats\":0.3,\"defaultPrice\":1.20,\"supermarketIds\":[" + supermarketId + "]}"));
        assertThat(riceResponse.getStatusCode()).isEqualTo(201);
        long riceId = readLong(riceResponse.getBody(), "id");

        long dayPartId = dayPartRepository.create(ProposedWeekMenuDayPart.builder()
                .name("Lunch Lambda")
                .description("Main meal")
                .sortOrder(10)
                .build()).getId();

        APIGatewayProxyResponseEvent stockResponse = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/api/v1/products/" + chickenId + "/stock")
                .withHeaders(authHeaders(token))
                .withBody("{\"quantity\":1.5,\"price\":2.00,\"expirationDate\":\"2026-06-20\",\"entryDate\":\"2026-06-10\"}"));
        assertThat(stockResponse.getStatusCode()).isEqualTo(201);

        APIGatewayProxyResponseEvent beansStock = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/api/v1/products/" + riceId + "/stock")
                .withHeaders(authHeaders(token))
                .withBody("{\"quantity\":2,\"price\":1.20,\"entryDate\":\"2026-06-11\"}"));
        assertThat(beansStock.getStatusCode()).isEqualTo(201);

        APIGatewayProxyResponseEvent createdMenu = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/api/v1/planning")
                .withHeaders(authHeaders(token))
                .withBody("{\"startDate\":\"" + startDate + "\",\"endDate\":\"" + endDate + "\"}"));
        assertThat(createdMenu.getStatusCode()).isEqualTo(201);
        long menuId = readLong(createdMenu.getBody(), "id");

        APIGatewayProxyResponseEvent draftCatalog = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath("/api/v1/planning")
                .withHeaders(authHeaders(token)));
        JsonNode draftSummary = findById(readNode(draftCatalog.getBody()), menuId);
        assertThat(draftSummary.get("state").asText()).isEqualTo("DRAFT");
        assertThat(draftSummary.get("plannedDays").asInt()).isZero();

        APIGatewayProxyResponseEvent plannedMenu = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("PUT")
                .withPath("/api/v1/planning/" + menuId + "/days")
                .withHeaders(authHeaders(token))
                .withBody("{\"date\":\"" + startDate + "\",\"sections\":[{\"dayPartId\":" + dayPartId + ",\"products\":[{\"productId\":" + chickenId + ",\"units\":1.5,\"sortOrder\":10},{\"productId\":" + riceId + ",\"units\":3,\"sortOrder\":20}]}]}"));
        assertThat(plannedMenu.getStatusCode()).isEqualTo(200);

        APIGatewayProxyResponseEvent establish = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/api/v1/planning/" + menuId + "/menu")
                .withHeaders(authHeaders(token))
                .withBody("{\"payerUserId\":" + auth.userId() + "}"));
        assertThat(establish.getStatusCode()).isEqualTo(201);
        long currentWeekMenuId = readLong(establish.getBody(), "id");
        assertThat(readNode(establish.getBody()).get("personIds").size()).isZero();

        APIGatewayProxyResponseEvent establishedCatalog = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath("/api/v1/planning")
                .withHeaders(authHeaders(token)));
        JsonNode establishedSummary = findById(readNode(establishedCatalog.getBody()), menuId);
        assertThat(establishedSummary.get("state").asText()).isEqualTo("ESTABLISHED");
        assertThat(establishedSummary.get("menuId").asLong()).isEqualTo(currentWeekMenuId);
        assertThat(establishedSummary.get("plannedDays").asInt()).isOne();

        APIGatewayProxyResponseEvent completeList = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath("/api/v1/menus/" + currentWeekMenuId + "/shopping-list")
                .withHeaders(authHeaders(token)));
        assertThat(completeList.getStatusCode()).isEqualTo(200);
        assertThat(readLong(completeList.getBody(), "0.productId")).isEqualTo(riceId);
        assertThat(readDecimal(completeList.getBody(), "0.missingUnits")).isEqualByComparingTo("1.00");

        APIGatewayProxyResponseEvent filteredList = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath("/api/v1/menus/" + currentWeekMenuId + "/shopping-list")
                .withQueryStringParameters(Map.of("supermarketId", Long.toString(supermarketId)))
                .withHeaders(authHeaders(token)));
        assertThat(filteredList.getBody()).isEqualTo(completeList.getBody());

        APIGatewayProxyResponseEvent missingSupermarket = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath("/api/v1/menus/" + currentWeekMenuId + "/shopping-list")
                .withQueryStringParameters(Map.of("supermarketId", "999999999"))
                .withHeaders(authHeaders(token)));
        assertThat(missingSupermarket.getStatusCode()).isEqualTo(404);

        APIGatewayProxyResponseEvent close = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/api/v1/menus/" + currentWeekMenuId + "/close")
                .withHeaders(authHeaders(token))
                .withBody("{\"personIds\":[" + auth.userId() + "]}"));
        assertThat(close.getStatusCode()).isEqualTo(200);
        assertThat(readDecimal(close.getBody(), "period.moneySpent")).isEqualByComparingTo("5.40");
        assertThat(readDecimal(close.getBody(), "month.moneySpent")).isEqualByComparingTo("5.40");
        assertThat(readText(close.getBody(), "period.maxDay.date")).isEqualTo(startDate.toString());
        BigDecimal closedCalories = readDecimal(close.getBody(), "period.averageCalories");

        APIGatewayProxyResponseEvent currentMenu = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath("/api/v1/menus/" + currentWeekMenuId)
                .withHeaders(authHeaders(token)));
        assertThat(currentMenu.getStatusCode()).isEqualTo(200);
        assertThat(readLong(currentMenu.getBody(), "personIds.0")).isEqualTo(auth.userId());

        APIGatewayProxyResponseEvent closedCatalog = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath("/api/v1/planning")
                .withHeaders(authHeaders(token)));
        assertThat(findById(readNode(closedCatalog.getBody()), menuId).get("state").asText()).isEqualTo("CLOSED");

        APIGatewayProxyResponseEvent updateProduct = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("PUT")
                .withPath("/api/v1/products/" + chickenId)
                .withHeaders(authHeaders(token))
                .withBody("{\"name\":\"Changed chicken\",\"description\":\"Changed\",\"gramsPerUnit\":100,\"calories\":9999,\"carbohydrates\":9999,\"proteins\":9999,\"fats\":9999,\"defaultPrice\":9999}"));
        assertThat(updateProduct.getStatusCode()).isEqualTo(200);

        APIGatewayProxyResponseEvent people = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath("/api/v1/users")
                .withHeaders(authHeaders(token)));
        assertThat(people.getStatusCode()).isEqualTo(200);
        assertThat(readNode(people.getBody()).findValuesAsText("id"))
                .contains(Long.toString(auth.userId()));

        APIGatewayProxyResponseEvent monthlyHistory = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath("/api/v1/users/" + auth.userId() + "/menu-history/monthly")
                .withQueryStringParameters(Map.of("year", Integer.toString(endDate.getYear()), "month", Integer.toString(endDate.getMonthValue())))
                .withHeaders(authHeaders(token)));
        assertThat(monthlyHistory.getStatusCode()).isEqualTo(200);
        assertThat(readLong(monthlyHistory.getBody(), "menus.0.menuId")).isEqualTo(currentWeekMenuId);
        assertThat(readDecimal(monthlyHistory.getBody(), "totals.averageCalories")).isEqualByComparingTo(closedCalories);

        APIGatewayProxyResponseEvent annualHistory = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath("/api/v1/users/" + auth.userId() + "/menu-history/annual")
                .withQueryStringParameters(Map.of("year", Integer.toString(endDate.getYear())))
                .withHeaders(authHeaders(token)));
        assertThat(annualHistory.getStatusCode()).isEqualTo(200);
        assertThat(readDecimal(annualHistory.getBody(), "totals.averageCalories")).isEqualByComparingTo(closedCalories);

        APIGatewayProxyResponseEvent rangeHistory = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath("/api/v1/users/" + auth.userId() + "/menu-history")
                .withQueryStringParameters(Map.of(
                        "from", "2018-12-31T00:00:00Z",
                        "to", "2019-01-31T23:59:59Z"
                ))
                .withHeaders(authHeaders(token)));
        assertThat(rangeHistory.getStatusCode()).isEqualTo(200);
        assertThat(readLong(rangeHistory.getBody(), "personId")).isEqualTo(auth.userId());
        assertThat(readText(rangeHistory.getBody(), "personName")).isNotBlank();
        assertThat(readText(rangeHistory.getBody(), "from")).isEqualTo("2018-12-31T00:00:00Z");
        assertThat(readText(rangeHistory.getBody(), "to")).isEqualTo("2019-01-31T23:59:59Z");
        assertThat(readLong(rangeHistory.getBody(), "menus.0.menuId")).isEqualTo(currentWeekMenuId);

        APIGatewayProxyResponseEvent invalidRange = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath("/api/v1/users/" + auth.userId() + "/menu-history")
                .withQueryStringParameters(Map.of(
                        "from", "2019-01-31T00:00:00Z",
                        "to", "2018-12-31T23:59:59Z"
                ))
                .withHeaders(authHeaders(token)));
        assertThat(invalidRange.getStatusCode()).isEqualTo(400);

        APIGatewayProxyResponseEvent missingHistoryFrom = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath("/api/v1/users/" + auth.userId() + "/menu-history")
                .withQueryStringParameters(Map.of("to", "2019-01-31T23:59:59Z"))
                .withHeaders(authHeaders(token)));
        assertThat(missingHistoryFrom.getStatusCode()).isEqualTo(400);

        APIGatewayProxyResponseEvent stats = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath("/api/v1/menus/" + currentWeekMenuId + "/stats")
                .withHeaders(authHeaders(token)));
        assertThat(stats.getStatusCode()).isEqualTo(200);
        assertThat(readDecimal(stats.getBody(), "period.moneySpent")).isEqualByComparingTo("5.40");
    }

    @Test
    void shouldDeleteMoneyBoxesAndMovementsThroughLambda() {
        AuthSession auth = registerAndReadAuth();
        String token = auth.token();
        String moneyBoxesPath = "/api/v1/money-boxes";

        APIGatewayProxyResponseEvent createdBox = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath(moneyBoxesPath)
                .withHeaders(authHeaders(token))
                .withBody("{\"name\":\"Lambda delete box " + System.nanoTime() + "\"}"));
        assertThat(createdBox.getStatusCode()).isEqualTo(201);
        long moneyBoxId = readLong(createdBox.getBody(), "id");

        APIGatewayProxyResponseEvent createdMovement = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath(moneyBoxesPath + "/" + moneyBoxId + "/movements")
                .withHeaders(authHeaders(token))
                .withBody("{\"amount\":5.25,\"description\":\"Lambda removable\"}"));
        assertThat(createdMovement.getStatusCode()).isEqualTo(201);
        long movementId = readLong(createdMovement.getBody(), "id");

        APIGatewayProxyResponseEvent deletedMovement = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("DELETE")
                .withPath(moneyBoxesPath + "/" + moneyBoxId + "/movements/" + movementId)
                .withHeaders(authHeaders(token)));
        assertThat(deletedMovement.getStatusCode()).isEqualTo(204);

        APIGatewayProxyResponseEvent missingMovement = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("DELETE")
                .withPath(moneyBoxesPath + "/" + moneyBoxId + "/movements/" + movementId)
                .withHeaders(authHeaders(token)));
        assertThat(missingMovement.getStatusCode()).isEqualTo(404);

        APIGatewayProxyResponseEvent allBoxes = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath(moneyBoxesPath)
                .withHeaders(authHeaders(token)));
        long userMoneyBoxId = 0;
        for (JsonNode box : readNode(allBoxes.getBody())) {
            if (!box.get("userId").isNull() && box.get("userId").asLong() == auth.userId()) {
                userMoneyBoxId = box.get("id").asLong();
                break;
            }
        }
        assertThat(userMoneyBoxId).isPositive();

        APIGatewayProxyResponseEvent rejectedUserBoxDelete = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("DELETE")
                .withPath(moneyBoxesPath + "/" + userMoneyBoxId)
                .withHeaders(authHeaders(token)));
        assertThat(rejectedUserBoxDelete.getStatusCode()).isEqualTo(409);

        APIGatewayProxyResponseEvent deletedBox = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("DELETE")
                .withPath(moneyBoxesPath + "/" + moneyBoxId)
                .withHeaders(authHeaders(token)));
        assertThat(deletedBox.getStatusCode()).isEqualTo(204);

        APIGatewayProxyResponseEvent missingBox = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("DELETE")
                .withPath(moneyBoxesPath + "/" + moneyBoxId)
                .withHeaders(authHeaders(token)));
        assertThat(missingBox.getStatusCode()).isEqualTo(404);
    }

    @Test
    void shouldUpdateAndDeleteUserWeightsThroughLambda() {
        AuthSession auth = registerAndReadAuth();
        AuthSession otherUser = registerAndReadAuth();
        String weightsPath = "/api/v1/users/" + auth.userId() + "/weights";

        APIGatewayProxyResponseEvent editable = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath(weightsPath)
                .withHeaders(authHeaders(auth.token()))
                .withBody("{\"weight\":90.00,\"recordedAt\":\"2026-06-05T08:00:00Z\",\"notes\":\"before period\"}"));
        assertThat(editable.getStatusCode()).isEqualTo(201);
        long weightId = readLong(editable.getBody(), "id");
        assertThat(readText(editable.getBody(), "notes")).isEqualTo("before period");

        productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath(weightsPath)
                .withHeaders(authHeaders(auth.token()))
                .withBody("{\"weight\":75.00,\"recordedAt\":\"2026-06-10T08:00:00Z\"}"));

        String weightPath = weightsPath + "/" + weightId;
        APIGatewayProxyResponseEvent updated = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("PUT")
                .withPath(weightPath)
                .withHeaders(authHeaders(auth.token()))
                .withBody("{\"weight\":70.25,\"recordedAt\":\"2026-06-25T19:30:00Z\",\"notes\":\"edited note\"}"));
        assertThat(updated.getStatusCode()).isEqualTo(200);
        assertThat(readDecimal(updated.getBody(), "weight")).isEqualByComparingTo("70.25");
        assertThat(readText(updated.getBody(), "recordedAt")).isEqualTo("2026-06-25T19:30:00Z");
        assertThat(readText(updated.getBody(), "notes")).isEqualTo("edited note");
        assertThat(readText(updated.getBody(), "createdAt")).isNotBlank();
        assertThat(readText(updated.getBody(), "updatedAt")).isNotBlank();

        APIGatewayProxyResponseEvent stats = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath(weightsPath + "/stats")
                .withQueryStringParameters(Map.of(
                        "from", "2026-06-01T00:00:00Z",
                        "to", "2026-06-30T23:59:59Z"
                ))
                .withHeaders(authHeaders(auth.token())));
        assertThat(stats.getStatusCode()).isEqualTo(200);
        assertThat(readDecimal(stats.getBody(), "highest.weight")).isEqualByComparingTo("75.00");
        assertThat(readDecimal(stats.getBody(), "lowest.weight")).isEqualByComparingTo("70.25");

        APIGatewayProxyResponseEvent invalid = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("PUT")
                .withPath(weightPath)
                .withHeaders(authHeaders(auth.token()))
                .withBody("{\"weight\":0,\"recordedAt\":\"2026-06-25T19:30:00Z\"}"));
        assertThat(invalid.getStatusCode()).isEqualTo(400);

        APIGatewayProxyResponseEvent missingTo = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath(weightsPath)
                .withQueryStringParameters(Map.of("from", "2026-06-01T00:00:00Z"))
                .withHeaders(authHeaders(auth.token())));
        assertThat(missingTo.getStatusCode()).isEqualTo(400);

        APIGatewayProxyResponseEvent missingFrom = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath(weightsPath)
                .withQueryStringParameters(Map.of("to", "2026-06-30T23:59:59Z"))
                .withHeaders(authHeaders(auth.token())));
        assertThat(missingFrom.getStatusCode()).isEqualTo(400);

        APIGatewayProxyResponseEvent legacyWeightRoute = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath("/api/v1/weights/" + weightId)
                .withHeaders(authHeaders(auth.token())));
        assertThat(legacyWeightRoute.getStatusCode()).isEqualTo(404);

        String wrongUserPath = "/api/v1/users/" + otherUser.userId() + "/weights/" + weightId;
        APIGatewayProxyResponseEvent wrongUserUpdate = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("PUT")
                .withPath(wrongUserPath)
                .withHeaders(authHeaders(auth.token()))
                .withBody("{\"weight\":65.00,\"recordedAt\":\"2026-06-25T19:30:00Z\"}"));
        assertThat(wrongUserUpdate.getStatusCode()).isEqualTo(404);

        APIGatewayProxyResponseEvent deleted = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("DELETE")
                .withPath(weightPath)
                .withHeaders(authHeaders(auth.token())));
        assertThat(deleted.getStatusCode()).isEqualTo(204);

        APIGatewayProxyResponseEvent statsAfterDelete = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("GET")
                .withPath(weightsPath + "/stats")
                .withQueryStringParameters(Map.of(
                        "from", "2026-06-01T00:00:00Z",
                        "to", "2026-06-30T23:59:59Z"
                ))
                .withHeaders(authHeaders(auth.token())));
        assertThat(readDecimal(statsAfterDelete.getBody(), "highest.weight")).isEqualByComparingTo("75.00");
        assertThat(readDecimal(statsAfterDelete.getBody(), "lowest.weight")).isEqualByComparingTo("75.00");

        APIGatewayProxyResponseEvent missing = productHttpHandler.apply(new APIGatewayProxyRequestEvent()
                .withHttpMethod("DELETE")
                .withPath(weightPath)
                .withHeaders(authHeaders(auth.token())));
        assertThat(missing.getStatusCode()).isEqualTo(404);
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

    private JsonNode findById(JsonNode array, long id) {
        for (JsonNode item : array) {
            if (item.get("id").asLong() == id) {
                return item;
            }
        }
        throw new AssertionError("Entry with id " + id + " not found in response: " + array);
    }

    private Map<String, String> queryParams(String query) {
        return Arrays.stream(query.split("&"))
                .map(param -> param.split("=", 2))
                .collect(java.util.stream.Collectors.toMap(parts -> parts[0], parts -> parts[1]));
    }

    private AuthSession registerAndReadAuth() {
        APIGatewayProxyRequestEvent register = new APIGatewayProxyRequestEvent()
                .withHttpMethod("POST")
                .withPath("/api/v1/auth/register")
                .withBody("{\"username\":\"lambda-user-" + System.nanoTime() + "\",\"password\":\"secret-password\",\"registrationCode\":\"" + REGISTRATION_CODE + "\"}");
        APIGatewayProxyResponseEvent response = productHttpHandler.apply(register);
        assertThat(response.getStatusCode()).isEqualTo(201);
        return new AuthSession(readText(response.getBody(), "accessToken"), readLong(response.getBody(), "userId"));
    }

    private record AuthSession(String token, Long userId) {
    }

    private String readText(String json, String field) {
        try {
            JsonNode node = readNode(json, field);
            return node.asText();
        } catch (Exception ex) {
            throw new AssertionError("Unable to read field '" + field + "' from response: " + json, ex);
        }
    }

    private java.math.BigDecimal readDecimal(String json, String field) {
        try {
            return readNode(json, field).decimalValue();
        } catch (Exception ex) {
            throw new AssertionError("Unable to read numeric field '" + field + "' from response: " + json, ex);
        }
    }

    private JsonNode readNode(String json, String path) throws Exception {
        JsonNode current = OBJECT_MAPPER.readTree(json);
        for (String segment : path.split("\\.")) {
            current = current.isArray() ? current.get(Integer.parseInt(segment)) : current.get(segment);
            if (current == null) {
                throw new AssertionError("Path segment not found: " + segment);
            }
        }
        return current;
    }

    private JsonNode readNode(String json) {
        try {
            return OBJECT_MAPPER.readTree(json);
        } catch (Exception ex) {
            throw new AssertionError("Unable to parse response: " + json, ex);
        }
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

    private static PostgreSQLContainer<?> postgres(String imageName) {
        PostgreSQLContainer<?> container = new PostgreSQLContainer<>(imageName)
                .withDatabaseName("foodhelper")
                .withUsername("foodhelper")
                .withPassword("foodhelper")
                .withInitScript("db/test-init.sql");

        String fixedPort = System.getenv("TESTCONTAINERS_POSTGRES_HOST_PORT");
        if (fixedPort != null && !fixedPort.isBlank()) {
            container.setPortBindings(java.util.List.of(fixedPort + ":5432"));
        }

        return container;
    }
}
