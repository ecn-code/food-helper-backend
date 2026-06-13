package com.eliascanalesnieto.foodhelper.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.eliascanalesnieto.foodhelper.presentation.PhotoUploadRequest;
import com.eliascanalesnieto.foodhelper.infra.NutritionalValuesCrudRepository;
import com.eliascanalesnieto.foodhelper.infra.NutritionalValuesEntity;
import com.eliascanalesnieto.foodhelper.presentation.AuthResponse;
import com.eliascanalesnieto.foodhelper.presentation.CreateProductRequest;
import com.eliascanalesnieto.foodhelper.presentation.CreateProposedWeekMenuRequest;
import com.eliascanalesnieto.foodhelper.presentation.CreateRecipeDerivedProductRequest;
import com.eliascanalesnieto.foodhelper.presentation.CreateRecipeRequest;
import com.eliascanalesnieto.foodhelper.presentation.LoginRequest;
import com.eliascanalesnieto.foodhelper.presentation.ProductResponse;
import com.eliascanalesnieto.foodhelper.presentation.AdjustStockQuantityRequest;
import com.eliascanalesnieto.foodhelper.presentation.ProposedWeekMenuProductRequest;
import com.eliascanalesnieto.foodhelper.presentation.ProposedWeekMenuResponse;
import com.eliascanalesnieto.foodhelper.presentation.ProposedWeekMenuSectionRequest;
import com.eliascanalesnieto.foodhelper.presentation.RecipeIngredientAssignmentRequest;
import com.eliascanalesnieto.foodhelper.presentation.RecipeResponse;
import com.eliascanalesnieto.foodhelper.presentation.RecipeDerivedProductResponse;
import com.eliascanalesnieto.foodhelper.presentation.RegisterRequest;
import com.eliascanalesnieto.foodhelper.presentation.CreateStockEntryRequest;
import com.eliascanalesnieto.foodhelper.presentation.StockEntryResponse;
import com.eliascanalesnieto.foodhelper.presentation.UpsertProposedWeekMenuDayRequest;
import com.eliascanalesnieto.foodhelper.presentation.UpdateRecipeRequest;
import com.eliascanalesnieto.foodhelper.presentation.UpdateProductRequest;
import java.math.BigDecimal;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.util.Base64;
import java.util.List;
import java.util.Map;
import javax.imageio.ImageIO;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test-integration")
class ProductRestIntegrationTest {
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

    @LocalServerPort
    int port;

    @Autowired
    private NutritionalValuesCrudRepository nutritionalValuesRepository;

    RestTemplate restTemplate = new RestTemplate();
    private String accessToken;

    ProductRestIntegrationTest() {
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            protected boolean hasError(org.springframework.http.HttpStatusCode statusCode) {
                return false;
            }
        });
    }

    @Test
    void createUpdateAndDeleteShouldWork() {
        String baseUrl = "http://localhost:" + port + "/api/v1/products";

        ResponseEntity<ProductResponse> created = postAuthorized(
                baseUrl,
                new CreateProductRequest("Apple", "Fresh apple", new BigDecimal("150"), new BigDecimal("52"), new BigDecimal("14"), new BigDecimal("0.3"), new BigDecimal("0.2")),
                ProductResponse.class
        );
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).isNotNull();
        Long id = created.getBody().id();

        ResponseEntity<ProductResponse> updated = restTemplate.exchange(
                baseUrl + "/" + id,
                HttpMethod.PUT,
                authorizedEntity(new UpdateProductRequest("Green Apple", "Green apple", new BigDecimal("140"), new BigDecimal("48"), new BigDecimal("13"), new BigDecimal("0.4"), new BigDecimal("0.1"))),
                ProductResponse.class
        );
        assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updated.getBody().name()).isEqualTo("Green Apple");
        assertThat(updated.getBody().description()).isEqualTo("Green apple");
        assertThat(updated.getBody().gramsPerUnit()).isEqualByComparingTo("140.00");

        restTemplate.exchange(
                baseUrl + "/" + id,
                HttpMethod.DELETE,
                authorizedEntity(null),
                String.class
        );

        ResponseEntity<String> deleted = restTemplate.exchange(
                baseUrl + "/" + id,
                HttpMethod.DELETE,
                authorizedEntity(null),
                String.class
        );
        assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void authShouldRegisterLoginAndRejectMissingToken() {
        String authUrl = "http://localhost:" + port + "/api/v1/auth";
        String username = "auth-user-" + System.nanoTime();

        ResponseEntity<String> missingCode = restTemplate.postForEntity(
                authUrl + "/register",
                Map.of("username", username + "-missing", "password", "secret-password"),
                String.class
        );
        assertThat(missingCode.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        ResponseEntity<String> invalidCode = restTemplate.postForEntity(
                authUrl + "/register",
                new RegisterRequest(username + "-invalid", "secret-password", "wrong-code"),
                String.class
        );
        assertThat(invalidCode.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        ResponseEntity<AuthResponse> registered = restTemplate.postForEntity(
                authUrl + "/register",
                new RegisterRequest(username, "secret-password", REGISTRATION_CODE),
                AuthResponse.class
        );
        assertThat(registered.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(registered.getBody()).isNotNull();
        assertThat(registered.getBody().tokenType()).isEqualTo("Bearer");
        assertThat(registered.getBody().accessToken()).isNotBlank();

        ResponseEntity<AuthResponse> loggedIn = restTemplate.postForEntity(
                authUrl + "/login",
                new LoginRequest(username, "secret-password"),
                AuthResponse.class
        );
        assertThat(loggedIn.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loggedIn.getBody()).isNotNull();
        assertThat(loggedIn.getBody().accessToken()).isNotBlank();

        ResponseEntity<String> withoutToken = restTemplate.getForEntity(
                "http://localhost:" + port + "/api/v1/products",
                String.class
        );
        assertThat(withoutToken.getStatusCode()).isEqualTo(HttpStatus.UNAUTHORIZED);
    }

    @Test
    void listProductsShouldReturnCreatedProducts() {
        String baseUrl = "http://localhost:" + port + "/api/v1/products";
        createProduct(baseUrl, "List Apple", "Fresh apple", "52", "14", "0.3", "0.2");
        createProduct(baseUrl, "List Banana", "Fresh banana", "89", "23", "1.1", "0.3");

        ResponseEntity<ProductResponse[]> listed = getAuthorized(baseUrl, ProductResponse[].class);

        assertThat(listed.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(listed.getBody()).isNotNull();
        assertThat(listed.getBody()).hasSize(2);
        assertThat(java.util.Arrays.stream(listed.getBody()).map(ProductResponse::name))
                .containsExactlyInAnyOrder("List Apple", "List Banana");
    }

    @Test
    void healthShouldBeUp() {
        ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:" + port + "/api/v1/health", String.class);
        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("UP");
    }

    @Test
    void openApiDocsShouldExposeCurrentEndpoints() {
        ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:" + port + "/v3/api-docs", String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).contains("/api/v1/products");
        assertThat(response.getBody()).contains("/api/v1/products/{id}");
        assertThat(response.getBody()).contains("/api/v1/recipes");
        assertThat(response.getBody()).contains("/api/v1/recipes/{id}");
        assertThat(response.getBody()).contains("/api/v1/recipes/{id}/derived-product");
        assertThat(response.getBody()).contains("/api/v1/media/{id}");
        assertThat(response.getBody()).contains("/api/v1/stock");
        assertThat(response.getBody()).contains("/api/v1/products/{productId}/stock");
        assertThat(response.getBody()).contains("/api/v1/stock/{stockEntryId}/add");
        assertThat(response.getBody()).contains("/api/v1/stock/{stockEntryId}/remove");
        assertThat(response.getBody()).contains("/api/v1/proposed-week-menus");
        assertThat(response.getBody()).contains("/api/v1/proposed-week-menus/{id}");
        assertThat(response.getBody()).contains("/api/v1/proposed-week-menus/{id}/days");
        assertThat(response.getBody()).contains("/api/v1/auth/register");
        assertThat(response.getBody()).contains("/api/v1/auth/login");
        assertThat(response.getBody()).contains("registrationCode");
        assertThat(response.getBody()).contains("PhotoUploadRequest");
        assertThat(response.getBody()).contains("bearerAuth");
        assertThat(response.getBody()).contains("/api/v1/health");
    }

    @Test
    void proposedWeekMenuShouldStartEmptyAndCalculateOrderedDayTotals() {
        String productsUrl = "http://localhost:" + port + "/api/v1/products";
        String proposedMenusUrl = "http://localhost:" + port + "/api/v1/proposed-week-menus";
        Long yogurtId = createProduct(productsUrl, "Menu Yogurt", "Greek yogurt", "59", "3.6", "10", "0.4", "125");
        Long almondsId = createProduct(productsUrl, "Menu Almonds", "Raw almonds", "579", "22", "21", "50", "30");

        ResponseEntity<ProposedWeekMenuResponse> createdMenu = postAuthorized(
                proposedMenusUrl,
                new CreateProposedWeekMenuRequest(LocalDate.of(2026, 6, 15), LocalDate.of(2026, 6, 21)),
                ProposedWeekMenuResponse.class
        );
        assertThat(createdMenu.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createdMenu.getBody()).isNotNull();
        assertThat(createdMenu.getBody().days()).isEmpty();
        assertThat(createdMenu.getBody().nutritionalValues().calories()).isEqualByComparingTo("0.00");

        ResponseEntity<ProposedWeekMenuResponse> updatedMenu = restTemplate.exchange(
                proposedMenusUrl + "/" + createdMenu.getBody().id() + "/days",
                HttpMethod.PUT,
                authorizedEntity(new UpsertProposedWeekMenuDayRequest(
                        LocalDate.of(2026, 6, 15),
                        List.of(
                                new ProposedWeekMenuSectionRequest(
                                        "Snack",
                                        20,
                                        List.of(new ProposedWeekMenuProductRequest(almondsId, new BigDecimal("2"), null, 10))
                                ),
                                new ProposedWeekMenuSectionRequest(
                                        "Lunch",
                                        10,
                                        List.of(new ProposedWeekMenuProductRequest(yogurtId, new BigDecimal("1"), new BigDecimal("200"), 10))
                                )
                        )
                )),
                ProposedWeekMenuResponse.class
        );

        assertThat(updatedMenu.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updatedMenu.getBody()).isNotNull();
        assertThat(updatedMenu.getBody().days()).hasSize(1);
        assertThat(updatedMenu.getBody().days().getFirst().sections())
                .extracting(section -> section.name())
                .containsExactly("Lunch", "Snack");
        assertThat(updatedMenu.getBody().days().getFirst().sections().get(1).products().getFirst().grams())
                .isEqualByComparingTo("60.00");
        assertThat(updatedMenu.getBody().nutritionalValues().calories()).isEqualByComparingTo("465.40");
        assertThat(updatedMenu.getBody().nutritionalValues().proteins()).isEqualByComparingTo("32.60");

        ResponseEntity<ProposedWeekMenuResponse> loadedMenu = getAuthorized(
                proposedMenusUrl + "/" + createdMenu.getBody().id(),
                ProposedWeekMenuResponse.class
        );
        assertThat(loadedMenu.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loadedMenu.getBody()).isNotNull();
        assertThat(loadedMenu.getBody().nutritionalValues().fats()).isEqualByComparingTo("30.80");
    }

    @Test
    void stockEndpointsShouldCreateAdjustAndFilterStock() {
        String productsUrl = "http://localhost:" + port + "/api/v1/products";
        String stockUrl = "http://localhost:" + port + "/api/v1/stock";
        Long appleId = createProduct(productsUrl, "Stock Apple", "Fresh apple", "52", "14", "0.3", "0.2");
        Long bananaId = createProduct(productsUrl, "Stock Banana", "Fresh banana", "89", "23", "1.1", "0.3");

        ResponseEntity<StockEntryResponse> createdAppleStock = postAuthorized(
                productsUrl + "/" + appleId + "/stock",
                new CreateStockEntryRequest(new BigDecimal("5.5"), LocalDate.of(2026, 6, 14), LocalDate.of(2026, 6, 10)),
                StockEntryResponse.class
        );
        assertThat(createdAppleStock.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createdAppleStock.getBody()).isNotNull();
        assertThat(createdAppleStock.getBody().productId()).isEqualTo(appleId);

        ResponseEntity<StockEntryResponse> createdBananaStock = postAuthorized(
                productsUrl + "/" + bananaId + "/stock",
                new CreateStockEntryRequest(new BigDecimal("4"), LocalDate.of(2026, 6, 12), LocalDate.of(2026, 6, 9)),
                StockEntryResponse.class
        );
        assertThat(createdBananaStock.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createdBananaStock.getBody()).isNotNull();

        ResponseEntity<StockEntryResponse> addedQuantity = postAuthorized(
                stockUrl + "/" + createdAppleStock.getBody().id() + "/add",
                new AdjustStockQuantityRequest(new BigDecimal("1.5")),
                StockEntryResponse.class
        );
        assertThat(addedQuantity.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(addedQuantity.getBody()).isNotNull();
        assertThat(addedQuantity.getBody().quantity()).isEqualByComparingTo("7.0");

        ResponseEntity<String> removeSome = postAuthorized(
                stockUrl + "/" + createdAppleStock.getBody().id() + "/remove",
                new AdjustStockQuantityRequest(new BigDecimal("2")),
                String.class
        );
        assertThat(removeSome.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<StockEntryResponse[]> allStock = getAuthorized(stockUrl, StockEntryResponse[].class);
        assertThat(allStock.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(allStock.getBody()).isNotNull();
        assertThat(allStock.getBody()).hasSize(2);
        assertThat(allStock.getBody()[0].productId()).isEqualTo(bananaId);
        assertThat(allStock.getBody()[1].quantity()).isEqualByComparingTo("5.0");

        ResponseEntity<StockEntryResponse[]> expiringSoon = getAuthorized(
                stockUrl + "?expiresBefore=2026-06-13",
                StockEntryResponse[].class
        );
        assertThat(expiringSoon.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(expiringSoon.getBody()).isNotNull();
        assertThat(expiringSoon.getBody()).hasSize(1);
        assertThat(expiringSoon.getBody()[0].productId()).isEqualTo(bananaId);

        ResponseEntity<StockEntryResponse[]> appleStock = getAuthorized(
                productsUrl + "/" + appleId + "/stock",
                StockEntryResponse[].class
        );
        assertThat(appleStock.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(appleStock.getBody()).isNotNull();
        assertThat(appleStock.getBody()).hasSize(1);
        assertThat(appleStock.getBody()[0].quantity()).isEqualByComparingTo("5.0");

        ResponseEntity<String> removeAll = postAuthorized(
                stockUrl + "/" + createdAppleStock.getBody().id() + "/remove",
                new AdjustStockQuantityRequest(new BigDecimal("5")),
                String.class
        );
        assertThat(removeAll.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<StockEntryResponse[]> appleStockAfterDelete = getAuthorized(
                productsUrl + "/" + appleId + "/stock",
                StockEntryResponse[].class
        );
        assertThat(appleStockAfterDelete.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(appleStockAfterDelete.getBody()).isNotNull();
        assertThat(appleStockAfterDelete.getBody()).isEmpty();
    }

    @Test
    void recipeDerivedProductShouldBeCalculatedAndSynchronized() {
        String productsUrl = "http://localhost:" + port + "/api/v1/products";
        String recipesUrl = "http://localhost:" + port + "/api/v1/recipes";

        Long chickenId = createProduct(productsUrl, "Chicken breast", "Chicken", "165", "0", "31", "3.6");
        Long coconutMilkId = createProduct(productsUrl, "Coconut milk", "Coconut milk", "230", "6", "2", "24");

        ResponseEntity<RecipeResponse> createdRecipe = postAuthorized(
                recipesUrl,
                new CreateRecipeRequest(
                        "Curry",
                        "Creamy curry",
                        "Cook everything together.",
                        List.of(
                                new RecipeIngredientAssignmentRequest(chickenId, new BigDecimal("200")),
                                new RecipeIngredientAssignmentRequest(coconutMilkId, new BigDecimal("100"))
                        )
                ),
                RecipeResponse.class
        );

        assertThat(createdRecipe.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createdRecipe.getBody()).isNotNull();
        assertThat(createdRecipe.getBody().nutritionalValues().calories()).isEqualByComparingTo("560.00");
        assertThat(createdRecipe.getBody().products()).hasSize(2);

        Long recipeId = createdRecipe.getBody().id();
        ResponseEntity<RecipeDerivedProductResponse> derivedProduct = postAuthorized(
                recipesUrl + "/" + recipeId + "/derived-product",
                new CreateRecipeDerivedProductRequest(new BigDecimal("400"), new BigDecimal("100")),
                RecipeDerivedProductResponse.class
        );

        assertThat(derivedProduct.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(derivedProduct.getBody()).isNotNull();
        assertThat(derivedProduct.getBody().unitsProduced()).isEqualByComparingTo("4.00");

        ResponseEntity<RecipeResponse> updatedRecipe = restTemplate.exchange(
                recipesUrl + "/" + recipeId,
                HttpMethod.PUT,
                authorizedEntity(new UpdateRecipeRequest(
                        "Curry",
                        "Creamy curry updated",
                        "Cook slowly and reduce.",
                        List.of(
                                new RecipeIngredientAssignmentRequest(chickenId, new BigDecimal("250")),
                                new RecipeIngredientAssignmentRequest(coconutMilkId, new BigDecimal("100"))
                        )
                )),
                RecipeResponse.class
        );

        assertThat(updatedRecipe.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updatedRecipe.getBody()).isNotNull();
        assertThat(updatedRecipe.getBody().nutritionalValues().calories()).isEqualByComparingTo("642.50");
        assertThat(updatedRecipe.getBody().derivedProduct()).isNotNull();

        NutritionalValuesEntity linkedProductValues = nutritionalValuesRepository.findById(derivedProduct.getBody().productId()).orElseThrow();
        assertThat(linkedProductValues.calories()).isEqualByComparingTo("642.50");

        ResponseEntity<String> secondDerivedProductAttempt = postAuthorized(
                recipesUrl + "/" + recipeId + "/derived-product",
                new CreateRecipeDerivedProductRequest(new BigDecimal("400"), new BigDecimal("100")),
                String.class
        );
        assertThat(secondDerivedProductAttempt.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void productPhotoShouldBeCompressedStoredAndDownloadable() {
        String productsUrl = "http://localhost:" + port + "/api/v1/products";

        ResponseEntity<ProductResponse> created = postAuthorized(
                productsUrl,
                new CreateProductRequest(
                        "Photo Apple",
                        "Apple with photo",
                        new BigDecimal("150"),
                        new BigDecimal("52"),
                        new BigDecimal("14"),
                        new BigDecimal("0.3"),
                        new BigDecimal("0.2"),
                        samplePhoto("product-photo")
                ),
                ProductResponse.class
        );

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).isNotNull();
        assertThat(created.getBody().photo()).isNotNull();
        assertThat(created.getBody().photo().contentType()).isEqualTo("image/jpeg");
        assertThat(created.getBody().photo().sizeBytes()).isLessThanOrEqualTo(153600);

        ResponseEntity<byte[]> mediaResponse = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/media/" + created.getBody().photo().id(),
                HttpMethod.GET,
                authorizedEntity(null),
                byte[].class
        );

        assertThat(mediaResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(mediaResponse.getHeaders().getContentType()).isEqualTo(org.springframework.http.MediaType.IMAGE_JPEG);
        assertThat(mediaResponse.getBody()).isNotNull();
        assertThat(mediaResponse.getBody().length).isEqualTo(created.getBody().photo().sizeBytes());
    }

    @Test
    void recipePhotoShouldBeCompressedAndReturnedInResponse() {
        String productsUrl = "http://localhost:" + port + "/api/v1/products";
        String recipesUrl = "http://localhost:" + port + "/api/v1/recipes";
        Long ingredientId = createProduct(productsUrl, "Photo Rice", "Rice", "130", "28", "2.7", "0.3");

        ResponseEntity<RecipeResponse> created = postAuthorized(
                recipesUrl,
                new CreateRecipeRequest(
                        "Photo Rice Bowl",
                        "Recipe with photo",
                        "Cook and serve.",
                        List.of(new RecipeIngredientAssignmentRequest(ingredientId, new BigDecimal("180"))),
                        samplePhoto("recipe-photo")
                ),
                RecipeResponse.class
        );

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).isNotNull();
        assertThat(created.getBody().photo()).isNotNull();
        assertThat(created.getBody().photo().contentType()).isEqualTo("image/jpeg");
        assertThat(created.getBody().photo().sizeBytes()).isLessThanOrEqualTo(153600);
    }

    private Long createProduct(String productsUrl, String name, String description, String calories, String carbohydrates, String proteins, String fats) {
        return createProduct(productsUrl, name, description, calories, carbohydrates, proteins, fats, "100");
    }

    private Long createProduct(String productsUrl, String name, String description, String calories, String carbohydrates, String proteins, String fats, String gramsPerUnit) {
        ResponseEntity<ProductResponse> created = postAuthorized(
                productsUrl,
                new CreateProductRequest(
                        name,
                        description,
                        new BigDecimal(gramsPerUnit),
                        new BigDecimal(calories),
                        new BigDecimal(carbohydrates),
                        new BigDecimal(proteins),
                        new BigDecimal(fats)
                ),
                ProductResponse.class
        );
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).isNotNull();
        return created.getBody().id();
    }

    private <T> ResponseEntity<T> postAuthorized(String url, Object body, Class<T> responseType) {
        return restTemplate.exchange(url, HttpMethod.POST, authorizedEntity(body), responseType);
    }

    private <T> ResponseEntity<T> getAuthorized(String url, Class<T> responseType) {
        return restTemplate.exchange(url, HttpMethod.GET, authorizedEntity(null), responseType);
    }

    private HttpEntity<Object> authorizedEntity(Object body) {
        return new HttpEntity<>(body, authHeaders());
    }

    private HttpHeaders authHeaders() {
        if (accessToken == null) {
            ResponseEntity<AuthResponse> registered = restTemplate.postForEntity(
                    "http://localhost:" + port + "/api/v1/auth/register",
                    new RegisterRequest("test-user-" + System.nanoTime(), "secret-password", REGISTRATION_CODE),
                    AuthResponse.class
            );
            assertThat(registered.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            assertThat(registered.getBody()).isNotNull();
            accessToken = registered.getBody().accessToken();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        return headers;
    }

    private PhotoUploadRequest samplePhoto(String fileNameBase) {
        try {
            BufferedImage image = new BufferedImage(1800, 1200, BufferedImage.TYPE_INT_RGB);
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
            return new PhotoUploadRequest(fileNameBase + ".png", "image/png", Base64.getEncoder().encodeToString(output.toByteArray()));
        } catch (Exception ex) {
            throw new AssertionError("Unable to build test image", ex);
        }
    }
}
