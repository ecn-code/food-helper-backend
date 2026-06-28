package com.eliascanalesnieto.foodhelper.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.eliascanalesnieto.foodhelper.presentation.PhotoUploadRequest;
import com.eliascanalesnieto.foodhelper.infra.NutritionalValuesCrudRepository;
import com.eliascanalesnieto.foodhelper.infra.NutritionalValuesEntity;
import com.eliascanalesnieto.foodhelper.presentation.AuthResponse;
import com.eliascanalesnieto.foodhelper.presentation.CreateProductRequest;
import com.eliascanalesnieto.foodhelper.presentation.CreateMoneyBoxRequest;
import com.eliascanalesnieto.foodhelper.presentation.CloseCurrentWeekMenuRequest;
import com.eliascanalesnieto.foodhelper.presentation.CreateProposedWeekMenuRequest;
import com.eliascanalesnieto.foodhelper.presentation.CreateRecipeDerivedProductRequest;
import com.eliascanalesnieto.foodhelper.presentation.CreateRecipeRequest;
import com.eliascanalesnieto.foodhelper.presentation.LoginRequest;
import com.eliascanalesnieto.foodhelper.presentation.MoneyBoxMovementResponse;
import com.eliascanalesnieto.foodhelper.presentation.MoneyBoxResponse;
import com.eliascanalesnieto.foodhelper.presentation.NutrientRuleRequest;
import com.eliascanalesnieto.foodhelper.presentation.NutritionalRuleStatus;
import com.eliascanalesnieto.foodhelper.presentation.NutritionalRulesResponse;
import com.eliascanalesnieto.foodhelper.presentation.SaveNutritionalRulesRequest;
import com.eliascanalesnieto.foodhelper.presentation.CreateUserMoneyMovementRequest;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuResponse;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuShoppingListItemResponse;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuStatsResponse;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuUsedStockResponse;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuPeriodStatsResponse;
import com.eliascanalesnieto.foodhelper.presentation.ProductStatsResponse;
import com.eliascanalesnieto.foodhelper.presentation.ProductResponse;
import com.eliascanalesnieto.foodhelper.presentation.ProductPageResponse;
import com.eliascanalesnieto.foodhelper.presentation.AdjustStockQuantityRequest;
import com.eliascanalesnieto.foodhelper.presentation.ProposedWeekMenuDayPartRequest;
import com.eliascanalesnieto.foodhelper.presentation.ProposedWeekMenuDayPartResponse;
import com.eliascanalesnieto.foodhelper.presentation.ProposedWeekMenuProductRequest;
import com.eliascanalesnieto.foodhelper.presentation.ProposedWeekMenuProductResponse;
import com.eliascanalesnieto.foodhelper.presentation.ProposedWeekMenuResponse;
import com.eliascanalesnieto.foodhelper.presentation.ProposedWeekMenuStockSummaryResponse;
import com.eliascanalesnieto.foodhelper.presentation.ProposedWeekMenuSectionRequest;
import com.eliascanalesnieto.foodhelper.presentation.RecipeIngredientAssignmentRequest;
import com.eliascanalesnieto.foodhelper.presentation.RecipeStatsResponse;
import com.eliascanalesnieto.foodhelper.presentation.RecipeResponse;
import com.eliascanalesnieto.foodhelper.presentation.RecipePageResponse;
import com.eliascanalesnieto.foodhelper.presentation.RecipeDerivedProductResponse;
import com.eliascanalesnieto.foodhelper.presentation.RegisterRequest;
import com.eliascanalesnieto.foodhelper.presentation.CreateStockEntryRequest;
import com.eliascanalesnieto.foodhelper.presentation.StockEntryResponse;
import com.eliascanalesnieto.foodhelper.presentation.SupermarketRequest;
import com.eliascanalesnieto.foodhelper.presentation.SupermarketResponse;
import com.eliascanalesnieto.foodhelper.presentation.ProductStatsSummaryResponse;
import com.eliascanalesnieto.foodhelper.presentation.UpdateStockEntryRequest;
import com.eliascanalesnieto.foodhelper.presentation.EstablishProposedWeekMenuRequest;
import com.eliascanalesnieto.foodhelper.presentation.MenuStockAllocationRequest;
import com.eliascanalesnieto.foodhelper.presentation.PlanningSummaryResponse;
import com.eliascanalesnieto.foodhelper.presentation.UpsertProposedWeekMenuDayRequest;
import com.eliascanalesnieto.foodhelper.presentation.UpdateRecipeRequest;
import com.eliascanalesnieto.foodhelper.presentation.UpdateProductRequest;
import com.eliascanalesnieto.foodhelper.presentation.UserMoneyBoxResponse;
import com.eliascanalesnieto.foodhelper.presentation.UserMoneyMovementResponse;
import com.eliascanalesnieto.foodhelper.presentation.CreateUserWeightRequest;
import com.eliascanalesnieto.foodhelper.presentation.UserWeightResponse;
import com.eliascanalesnieto.foodhelper.presentation.UserWeightStatsResponse;
import com.eliascanalesnieto.foodhelper.presentation.UpdateUserWeightRequest;
import java.math.BigDecimal;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.Instant;
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
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.jdbc.core.JdbcTemplate;
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
    static PostgreSQLContainer<?> postgres = postgres("postgres:16-alpine");

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

    @Autowired
    private JdbcTemplate jdbcTemplate;

    RestTemplate restTemplate = new RestTemplate();
    private String accessToken;
    private Long authenticatedUserId;

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
                new CreateProductRequest("Apple", "Fresh apple", new BigDecimal("150"), new BigDecimal("52"), new BigDecimal("14"), new BigDecimal("0.3"), new BigDecimal("0.2"), new BigDecimal("2.49")),
                ProductResponse.class
        );
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).isNotNull();
        assertThat(created.getBody().defaultPrice()).isEqualByComparingTo("2.49");
        Long id = created.getBody().id();

        ResponseEntity<ProductResponse> updated = restTemplate.exchange(
                baseUrl + "/" + id,
                HttpMethod.PUT,
                authorizedEntity(new UpdateProductRequest("Green Apple", "Green apple", new BigDecimal("140"), new BigDecimal("48"), new BigDecimal("13"), new BigDecimal("0.4"), new BigDecimal("0.1"), new BigDecimal("2.79"))),
                ProductResponse.class
        );
        assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updated.getBody().name()).isEqualTo("Green Apple");
        assertThat(updated.getBody().description()).isEqualTo("Green apple");
        assertThat(updated.getBody().gramsPerUnit()).isEqualByComparingTo("140.00");
        assertThat(updated.getBody().defaultPrice()).isEqualByComparingTo("2.79");

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
    void supermarketCrudAndProductAssignmentsShouldWork() {
        String supermarketsUrl = "http://localhost:" + port + "/api/v1/supermarkets";
        String productsUrl = "http://localhost:" + port + "/api/v1/products";
        String suffix = Long.toString(System.nanoTime());

        ResponseEntity<SupermarketResponse> first = postAuthorized(
                supermarketsUrl,
                new SupermarketRequest("Market A " + suffix),
                SupermarketResponse.class
        );
        ResponseEntity<SupermarketResponse> second = postAuthorized(
                supermarketsUrl,
                new SupermarketRequest("Market B " + suffix),
                SupermarketResponse.class
        );
        assertThat(first.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(second.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<String> duplicate = postAuthorized(
                supermarketsUrl,
                new SupermarketRequest(("Market A " + suffix).toLowerCase()),
                String.class
        );
        assertThat(duplicate.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        ResponseEntity<ProductResponse> product = postAuthorized(
                productsUrl,
                new CreateProductRequest(
                        "Assigned Product " + suffix, "Assigned", new BigDecimal("100"),
                        new BigDecimal("10"), new BigDecimal("20"), new BigDecimal("30"), new BigDecimal("4"),
                        null, List.of(first.getBody().id(), second.getBody().id())
                ),
                ProductResponse.class
        );
        assertThat(product.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(product.getBody().supermarkets()).extracting(SupermarketResponse::id)
                .containsExactlyInAnyOrder(first.getBody().id(), second.getBody().id());

        ResponseEntity<ProductResponse> updatedProduct = restTemplate.exchange(
                productsUrl + "/" + product.getBody().id(),
                HttpMethod.PUT,
                authorizedEntity(new UpdateProductRequest(
                        "Assigned Product " + suffix, "Assigned", new BigDecimal("100"),
                        new BigDecimal("10"), new BigDecimal("20"), new BigDecimal("30"), new BigDecimal("4"),
                        null, List.of(second.getBody().id())
                )),
                ProductResponse.class
        );
        assertThat(updatedProduct.getBody().supermarkets()).extracting(SupermarketResponse::id)
                .containsExactly(second.getBody().id());

        ResponseEntity<String> assignedDelete = restTemplate.exchange(
                supermarketsUrl + "/" + second.getBody().id(), HttpMethod.DELETE, authorizedEntity(null), String.class
        );
        assertThat(assignedDelete.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);

        ResponseEntity<SupermarketResponse[]> listed = getAuthorized(supermarketsUrl, SupermarketResponse[].class);
        assertThat(listed.getBody()).extracting(SupermarketResponse::id)
                .contains(first.getBody().id(), second.getBody().id());

        ResponseEntity<SupermarketResponse> renamed = restTemplate.exchange(
                supermarketsUrl + "/" + first.getBody().id(), HttpMethod.PUT,
                authorizedEntity(new SupermarketRequest("Market C " + suffix)), SupermarketResponse.class
        );
        assertThat(renamed.getBody().name()).isEqualTo("Market C " + suffix);
        ResponseEntity<String> deleted = restTemplate.exchange(
                supermarketsUrl + "/" + first.getBody().id(), HttpMethod.DELETE, authorizedEntity(null), String.class
        );
        assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
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
        ResponseEntity<ProductPageResponse> before = getAuthorized(baseUrl + "?page=0&size=1", ProductPageResponse.class);
        createProduct(baseUrl, "List Apple", "Fresh apple", "52", "14", "0.3", "0.2");
        createProduct(baseUrl, "List Banana", "Fresh banana", "89", "23", "1.1", "0.3");

        ResponseEntity<ProductPageResponse> after = getAuthorized(baseUrl + "?page=0&size=1", ProductPageResponse.class);
        long afterTotal = after.getBody().totalElements();

        assertThat(after.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(after.getBody()).isNotNull();
        assertThat(afterTotal).isGreaterThanOrEqualTo(before.getBody().totalElements() + 2);
        assertThat(after.getBody().page()).isEqualTo(0);
        assertThat(after.getBody().size()).isEqualTo(1);

        ResponseEntity<ProductPageResponse> applePage = getAuthorized(baseUrl + "?page=" + (afterTotal - 2) + "&size=1", ProductPageResponse.class);
        ResponseEntity<ProductPageResponse> bananaPage = getAuthorized(baseUrl + "?page=" + (afterTotal - 1) + "&size=1", ProductPageResponse.class);

        assertThat(applePage.getBody()).isNotNull();
        assertThat(applePage.getBody().items()).hasSize(1);
        assertThat(applePage.getBody().items().getFirst().name()).isEqualTo("List Apple");
        assertThat(bananaPage.getBody()).isNotNull();
        assertThat(bananaPage.getBody().items()).hasSize(1);
        assertThat(bananaPage.getBody().items().getFirst().name()).isEqualTo("List Banana");
    }

    @Test
    void listProductsShouldSupportSearchAndNutritionalFilters() {
        String baseUrl = "http://localhost:" + port + "/api/v1/products";
        String suffix = Long.toString(System.nanoTime());

        createProduct(baseUrl, "Filter Apple " + suffix, "Searchable apple " + suffix, "52", "14", "0.3", "0.2");
        createProduct(baseUrl, "Filter Banana " + suffix, "Searchable banana " + suffix, "89", "23", "1.1", "0.3");
        createProduct(baseUrl, "Filter Chicken " + suffix, "Searchable chicken " + suffix, "165", "0", "31", "3.6");

        ResponseEntity<ProductPageResponse> searchOnly = getAuthorized(baseUrl + "?search=" + suffix, ProductPageResponse.class);
        assertThat(searchOnly.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(searchOnly.getBody()).isNotNull();
        assertThat(searchOnly.getBody().items()).extracting(ProductResponse::name)
                .containsExactly(
                        "Filter Apple " + suffix,
                        "Filter Banana " + suffix,
                        "Filter Chicken " + suffix
                );

        ResponseEntity<ProductPageResponse> caloriesMin = getAuthorized(baseUrl + "?search=" + suffix + "&caloriesMin=80", ProductPageResponse.class);
        assertThat(caloriesMin.getBody()).isNotNull();
        assertThat(caloriesMin.getBody().items()).extracting(ProductResponse::name)
                .containsExactly("Filter Banana " + suffix, "Filter Chicken " + suffix);

        ResponseEntity<ProductPageResponse> caloriesMax = getAuthorized(baseUrl + "?search=" + suffix + "&caloriesMax=60", ProductPageResponse.class);
        assertThat(caloriesMax.getBody()).isNotNull();
        assertThat(caloriesMax.getBody().items()).extracting(ProductResponse::name)
                .containsExactly("Filter Apple " + suffix);

        ResponseEntity<ProductPageResponse> combinedNutrients = getAuthorized(
                baseUrl + "?search=" + suffix + "&caloriesMin=80&caloriesMax=90&carbohydratesMin=20&carbohydratesMax=25",
                ProductPageResponse.class
        );
        assertThat(combinedNutrients.getBody()).isNotNull();
        assertThat(combinedNutrients.getBody().items()).extracting(ProductResponse::name)
                .containsExactly("Filter Banana " + suffix);

        ResponseEntity<ProductPageResponse> searchAndFilters = getAuthorized(
                baseUrl + "?search=banana&caloriesMin=80&caloriesMax=90",
                ProductPageResponse.class
        );
        assertThat(searchAndFilters.getBody()).isNotNull();
        assertThat(searchAndFilters.getBody().items()).extracting(ProductResponse::name)
                .containsExactly("Filter Banana " + suffix);

        ResponseEntity<ProductPageResponse> firstPage = getAuthorized(baseUrl + "?search=" + suffix + "&page=0&size=1", ProductPageResponse.class);
        ResponseEntity<ProductPageResponse> secondPage = getAuthorized(baseUrl + "?search=" + suffix + "&page=1&size=1", ProductPageResponse.class);
        assertThat(firstPage.getBody()).isNotNull();
        assertThat(firstPage.getBody().items()).hasSize(1);
        assertThat(firstPage.getBody().totalElements()).isEqualTo(3);
        assertThat(firstPage.getBody().totalPages()).isEqualTo(3);
        assertThat(firstPage.getBody().items().getFirst().name()).isEqualTo("Filter Apple " + suffix);
        assertThat(secondPage.getBody()).isNotNull();
        assertThat(secondPage.getBody().items()).hasSize(1);
        assertThat(secondPage.getBody().items().getFirst().name()).isEqualTo("Filter Banana " + suffix);

        ResponseEntity<ProductPageResponse> empty = getAuthorized(baseUrl + "?search=no-match-" + suffix, ProductPageResponse.class);
        assertThat(empty.getBody()).isNotNull();
        assertThat(empty.getBody().items()).isEmpty();
        assertThat(empty.getBody().totalElements()).isZero();
        assertThat(empty.getBody().totalPages()).isZero();
    }

    @Test
    void listRecipesShouldReturnCreatedRecipesAndExposeCorsForLocalFrontend() {
        String suffix = Long.toString(System.nanoTime());
        String productsUrl = "http://localhost:" + port + "/api/v1/products";
        String recipesUrl = "http://localhost:" + port + "/api/v1/recipes";
        String recipeName = "Curry " + suffix;
        ResponseEntity<RecipePageResponse> before = getAuthorized(recipesUrl + "?page=0&size=1", RecipePageResponse.class);
        Long chickenId = createProduct(productsUrl, "Recipe Chicken " + suffix, "Chicken breast", "165", "0", "31", "3.6");

        ResponseEntity<RecipeResponse> created = postAuthorized(
                recipesUrl,
                new CreateRecipeRequest(
                        recipeName,
                        "Creamy curry " + suffix,
                        "Cook everything together.",
                        List.of(new RecipeIngredientAssignmentRequest(chickenId, new BigDecimal("200")))
                ),
                RecipeResponse.class
        );

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<RecipeResponse> soup = postAuthorized(
                recipesUrl,
                new CreateRecipeRequest(
                        "Soup " + suffix,
                        "Simple soup " + suffix,
                        "Cook gently.",
                        List.of(new RecipeIngredientAssignmentRequest(chickenId, new BigDecimal("100")))
                ),
                RecipeResponse.class
        );

        assertThat(soup.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<RecipePageResponse> paged = restTemplate.exchange(
                recipesUrl + "?page=0&size=1",
                HttpMethod.GET,
                new HttpEntity<>(null, authHeaders()),
                RecipePageResponse.class
        );

        assertThat(paged.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(paged.getBody()).isNotNull();
        assertThat(paged.getBody().items()).hasSize(1);
        assertThat(paged.getBody().page()).isEqualTo(0);
        assertThat(paged.getBody().size()).isEqualTo(1);
        assertThat(paged.getBody().totalElements()).isGreaterThanOrEqualTo(before.getBody().totalElements() + 2);
        assertThat(paged.getBody().totalPages()).isEqualTo((int) paged.getBody().totalElements());
        ResponseEntity<RecipePageResponse> lastPage = getAuthorized(
                recipesUrl + "?page=" + (paged.getBody().totalElements() - 1) + "&size=1",
                RecipePageResponse.class
        );
        ResponseEntity<RecipePageResponse> previousPage = getAuthorized(
                recipesUrl + "?page=" + (paged.getBody().totalElements() - 2) + "&size=1",
                RecipePageResponse.class
        );
        assertThat(previousPage.getBody()).isNotNull();
        assertThat(previousPage.getBody().items().getFirst().name()).isEqualTo(recipeName);
        assertThat(lastPage.getBody()).isNotNull();
        assertThat(lastPage.getBody().items().getFirst().name()).isEqualTo("Soup " + suffix);
    }

    @Test
    void listRecipesShouldApplyAccentInsensitiveTextNutritionalAndDerivedProductFilters() {
        String suffix = Long.toString(System.nanoTime());
        String productsUrl = "http://localhost:" + port + "/api/v1/products";
        String recipesUrl = "http://localhost:" + port + "/api/v1/recipes";
        Long saffronId = createProduct(
                productsUrl, "Azafrán " + suffix, "Especia aromática", "120", "20", "8", "4"
        );
        RecipeResponse matching = postAuthorized(
                recipesUrl,
                new CreateRecipeRequest(
                        "Paella " + suffix,
                        "Descripción mediterránea",
                        "Cocción lenta con caldo.",
                        List.of(new RecipeIngredientAssignmentRequest(saffronId, new BigDecimal("100")))
                ),
                RecipeResponse.class
        ).getBody();
        postAuthorized(
                recipesUrl,
                new CreateRecipeRequest(
                        "Other recipe " + suffix,
                        "Unrelated description",
                        "Bake briefly.",
                        List.of(new RecipeIngredientAssignmentRequest(saffronId, new BigDecimal("10")))
                ),
                RecipeResponse.class
        );
        postAuthorized(
                recipesUrl + "/" + matching.id() + "/derived-product",
                new CreateRecipeDerivedProductRequest(new BigDecimal("500"), new BigDecimal("100")),
                RecipeDerivedProductResponse.class
        );

        ResponseEntity<RecipePageResponse> filtered = getAuthorized(
                recipesUrl + "?search=azafran&caloriesMin=100&caloriesMax=130"
                        + "&carbohydratesMin=20&proteinsMax=8&fatsMin=4&hasDerivedProduct=true",
                RecipePageResponse.class
        );
        assertThat(filtered.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(filtered.getBody().items()).extracting(RecipeResponse::id).containsExactly(matching.id());
        assertThat(filtered.getBody().totalElements()).isOne();

        assertThat(getAuthorized(recipesUrl + "?caloriesMin=-1", String.class).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(getAuthorized(recipesUrl + "?proteinsMin=20&proteinsMax=10", String.class).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
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
        assertThat(response.getBody()).contains("/api/v1/products/stats");
        assertThat(response.getBody()).contains("/api/v1/products/{id}");
        assertThat(response.getBody()).contains("ProductPageResponse");
        assertThat(response.getBody()).contains("search");
        assertThat(response.getBody()).contains("caloriesMin");
        assertThat(response.getBody()).contains("caloriesMax");
        assertThat(response.getBody()).contains("carbohydratesMin");
        assertThat(response.getBody()).contains("proteinsMax");
        assertThat(response.getBody()).contains("fatsMin");
        assertThat(response.getBody()).contains("/api/v1/recipes");
        assertThat(response.getBody()).contains("/api/v1/recipes/stats");
        assertThat(response.getBody()).contains("/api/v1/recipes/{id}");
        assertThat(response.getBody()).contains("RecipePageResponse");
        assertThat(response.getBody()).contains("/api/v1/recipes/{id}/derived-product");
        assertThat(response.getBody()).contains("/api/v1/media/{id}");
        assertThat(response.getBody()).contains("/api/v1/stock");
        assertThat(response.getBody()).contains("/api/v1/stock/{stockEntryId}");
        assertThat(response.getBody()).contains("/api/v1/products/{productId}/stock");
        assertThat(response.getBody()).contains("/api/v1/stock/{stockEntryId}/add");
        assertThat(response.getBody()).contains("/api/v1/stock/{stockEntryId}/remove");
        assertThat(response.getBody()).contains("defaultPrice");
        assertThat(response.getBody()).contains("\"price\"");
        assertThat(response.getBody()).contains("/api/v1/planning");
        assertThat(response.getBody()).contains("/api/v1/planning/{id}");
        assertThat(response.getBody()).contains("/api/v1/planning/{id}/days");
        assertThat(response.getBody()).contains("/api/v1/planning/day-parts");
        assertThat(response.getBody()).contains("unique within each section");
        assertThat(response.getBody()).contains("stockSummary");
        assertThat(response.getBody()).contains("PlanningStockSummaryResponse");
        assertThat(response.getBody()).contains("PlanningStockRequirementResponse");
        assertThat(response.getBody()).contains("/api/v1/nutritional-rules");
        assertThat(response.getBody()).contains("NutritionalRulesEvaluationResponse");
        assertThat(response.getBody()).contains("inclusive date range cannot span more than 16 calendar days");
        assertThat(response.getBody()).contains("/api/v1/users/{userId}/money-box");
        assertThat(response.getBody()).contains("/api/v1/users/{userId}/money-box/movements");
        assertThat(response.getBody()).contains("/api/v1/users/{userId}/weights");
        assertThat(response.getBody()).contains("/api/v1/users/{userId}/weights/stats");
        assertThat(response.getBody()).contains("/api/v1/users/{userId}/weights/{weightId}");
        assertThat(response.getBody()).contains("UserWeightStatsResponse");
        assertThat(response.getBody()).contains("/api/v1/money-boxes");
        assertThat(response.getBody()).contains("/api/v1/money-boxes/{moneyBoxId}");
        assertThat(response.getBody()).contains("/api/v1/money-boxes/{moneyBoxId}/movements");
        assertThat(response.getBody()).contains("/api/v1/money-boxes/{moneyBoxId}/movements/{movementId}");
        assertThat(response.getBody()).contains("deleteManualMoneyBox");
        assertThat(response.getBody()).contains("deleteMoneyBoxMovement");
        assertThat(response.getBody()).contains("MoneyBoxResponse");
        assertThat(response.getBody()).contains("payerUserId");
        assertThat(response.getBody()).contains("stockAllocations");
        assertThat(response.getBody()).contains("undoMenuCreation");
        assertThat(response.getBody()).contains("/api/v1/auth/register");
        assertThat(response.getBody()).contains("/api/v1/auth/login");
        assertThat(response.getBody()).contains("registrationCode");
        assertThat(response.getBody()).contains("PhotoUploadRequest");
        assertThat(response.getBody()).contains("bearerAuth");
        assertThat(response.getBody()).contains("/api/v1/health");
        assertThat(response.getBody()).contains("ProductStatsResponse");
        assertThat(response.getBody()).contains("RecipeStatsResponse");
    }

    @Test
    void openApiGroupedDocsShouldExposeEachDomainSeparately() {
        assertOpenApiGroup("auth", "/api/v1/auth/register", "/api/v1/auth/login");
        assertOpenApiGroup("health", "/api/v1/health");
        assertOpenApiGroup("media", "/api/v1/media/{id}");
        assertOpenApiGroup("products", "/api/v1/products", "/api/v1/products/stats", "/api/v1/products/{id}");
        assertOpenApiGroup("supermarkets", "/api/v1/supermarkets", "/api/v1/supermarkets/{id}");
        assertOpenApiGroup("recipes", "/api/v1/recipes", "/api/v1/recipes/stats", "/api/v1/recipes/{id}", "/api/v1/recipes/{id}/derived-product");
        assertOpenApiGroup("stock", "/api/v1/stock", "/api/v1/stock/{stockEntryId}", "/api/v1/stock/{stockEntryId}/add", "/api/v1/stock/{stockEntryId}/remove", "/api/v1/products/{productId}/stock");
        assertOpenApiGroup("users", "/api/v1/users/{userId}/money-box", "/api/v1/users/{userId}/money-box/movements");
        assertOpenApiGroup(
                "user-weights",
                "/api/v1/users/{userId}/weights",
                "/api/v1/users/{userId}/weights/stats",
                "/api/v1/users/{userId}/weights/{weightId}"
        );
        assertOpenApiGroup("money-boxes", "/api/v1/money-boxes", "/api/v1/money-boxes/{moneyBoxId}", "/api/v1/money-boxes/{moneyBoxId}/movements", "/api/v1/money-boxes/{moneyBoxId}/movements/{movementId}");
        assertOpenApiGroup("planning", "/api/v1/planning", "/api/v1/planning/{id}", "/api/v1/planning/{id}/days", "/api/v1/planning/day-parts");
        assertOpenApiGroup("menus", "/api/v1/menus", "/api/v1/menus/{id}", "/api/v1/menus/{id}/used-stock", "/api/v1/menus/{id}/shopping-list", "/api/v1/menus/{id}/close", "/api/v1/menus/{id}/stats");
        assertOpenApiGroup("nutritional-rules", "/api/v1/nutritional-rules");
    }

    @Test
    void moneyBoxesShouldMixUserAndManualBoxesAndAcceptManualMovements() {
        String moneyBoxesUrl = "http://localhost:" + port + "/api/v1/money-boxes";
        Long userId = authenticatedUserId();
        String manualName = "Household cash " + System.nanoTime();

        ResponseEntity<MoneyBoxResponse> created = postAuthorized(
                moneyBoxesUrl,
                new CreateMoneyBoxRequest(manualName),
                MoneyBoxResponse.class
        );

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).isNotNull();
        assertThat(created.getBody().type()).isEqualTo(com.eliascanalesnieto.foodhelper.domain.MoneyBoxType.MANUAL);
        assertThat(created.getBody().name()).isEqualTo(manualName);
        assertThat(created.getBody().userId()).isNull();
        assertThat(created.getBody().balance()).isEqualByComparingTo("0.00");

        ResponseEntity<MoneyBoxMovementResponse> movement = postAuthorized(
                moneyBoxesUrl + "/" + created.getBody().id() + "/movements",
                new CreateUserMoneyMovementRequest(new BigDecimal("35.50"), "Cash reserve"),
                MoneyBoxMovementResponse.class
        );

        assertThat(movement.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(movement.getBody()).isNotNull();
        assertThat(movement.getBody().moneyBoxId()).isEqualTo(created.getBody().id());
        assertThat(movement.getBody().userId()).isNull();

        ResponseEntity<MoneyBoxResponse> selectedManual = getAuthorized(
                moneyBoxesUrl + "/" + created.getBody().id(),
                MoneyBoxResponse.class
        );
        assertThat(selectedManual.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(selectedManual.getBody()).isNotNull();
        assertThat(selectedManual.getBody().balance()).isEqualByComparingTo("35.50");
        assertThat(selectedManual.getBody().movements()).hasSize(1);

        ResponseEntity<MoneyBoxResponse[]> all = getAuthorized(moneyBoxesUrl, MoneyBoxResponse[].class);
        assertThat(all.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(all.getBody()).isNotNull();
        assertThat(all.getBody())
                .anySatisfy(box -> {
                    assertThat(box.type()).isEqualTo(com.eliascanalesnieto.foodhelper.domain.MoneyBoxType.USER);
                    assertThat(box.userId()).isEqualTo(userId);
                })
                .anySatisfy(box -> {
                    assertThat(box.type()).isEqualTo(com.eliascanalesnieto.foodhelper.domain.MoneyBoxType.MANUAL);
                    assertThat(box.name()).isEqualTo(manualName);
                    assertThat(box.balance()).isEqualByComparingTo("35.50");
                    assertThat(box.movements()).hasSize(1);
                });

        ResponseEntity<UserMoneyBoxResponse> selectedUser = getAuthorized(
                "http://localhost:" + port + "/api/v1/users/" + userId + "/money-box",
                UserMoneyBoxResponse.class
        );
        assertThat(selectedUser.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(selectedUser.getBody()).isNotNull();
        assertThat(selectedUser.getBody().userId()).isEqualTo(userId);
    }

    @Test
    void moneyBoxDeletesShouldEnforceTypeOwnershipAndMenuLinks() {
        String moneyBoxesUrl = "http://localhost:" + port + "/api/v1/money-boxes";
        Long userId = authenticatedUserId();
        MoneyBoxResponse userMoneyBox = java.util.Arrays.stream(
                        getAuthorized(moneyBoxesUrl, MoneyBoxResponse[].class).getBody()
                )
                .filter(box -> userId.equals(box.userId()))
                .findFirst()
                .orElseThrow();
        MoneyBoxResponse firstBox = postAuthorized(
                moneyBoxesUrl,
                new CreateMoneyBoxRequest("Delete box " + System.nanoTime()),
                MoneyBoxResponse.class
        ).getBody();
        MoneyBoxResponse otherBox = postAuthorized(
                moneyBoxesUrl,
                new CreateMoneyBoxRequest("Other delete box " + System.nanoTime()),
                MoneyBoxResponse.class
        ).getBody();
        assertThat(firstBox).isNotNull();
        assertThat(otherBox).isNotNull();

        MoneyBoxMovementResponse removable = postAuthorized(
                moneyBoxesUrl + "/" + firstBox.id() + "/movements",
                new CreateUserMoneyMovementRequest(new BigDecimal("12.50"), "Removable"),
                MoneyBoxMovementResponse.class
        ).getBody();
        assertThat(removable).isNotNull();

        ResponseEntity<Void> wrongBox = deleteAuthorized(
                moneyBoxesUrl + "/" + otherBox.id() + "/movements/" + removable.id(),
                Void.class
        );
        assertThat(wrongBox.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<Void> deletedMovement = deleteAuthorized(
                moneyBoxesUrl + "/" + firstBox.id() + "/movements/" + removable.id(),
                Void.class
        );
        assertThat(deletedMovement.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(getAuthorized(moneyBoxesUrl + "/" + firstBox.id(), MoneyBoxResponse.class).getBody().movements())
                .isEmpty();
        assertThat(deleteAuthorized(
                moneyBoxesUrl + "/" + firstBox.id() + "/movements/" + removable.id(),
                String.class
        ).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        Long planningId = jdbcTemplate.queryForObject(
                "INSERT INTO proposed_week_menus (start_date, end_date) VALUES (CURRENT_DATE, CURRENT_DATE) RETURNING id",
                Long.class
        );
        Long menuId = jdbcTemplate.queryForObject(
                "INSERT INTO current_week_menus (proposed_week_menu_id, snapshot_json) VALUES (?, '{}') RETURNING id",
                Long.class,
                planningId
        );
        Long linkedMovementId = jdbcTemplate.queryForObject(
                """
                INSERT INTO user_money_movements (money_box_id, amount, description, current_week_menu_id)
                VALUES (?, 8.00, 'Menu cost', ?)
                RETURNING id
                """,
                Long.class,
                firstBox.id(),
                menuId
        );

        ResponseEntity<String> linkedDelete = deleteAuthorized(
                moneyBoxesUrl + "/" + firstBox.id() + "/movements/" + linkedMovementId,
                String.class
        );
        assertThat(linkedDelete.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(linkedDelete.getBody()).contains("linked");

        assertThat(deleteAuthorized(moneyBoxesUrl + "/" + userMoneyBox.id(), String.class).getStatusCode())
                .isEqualTo(HttpStatus.CONFLICT);
        assertThat(deleteAuthorized(moneyBoxesUrl + "/999999999", String.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        assertThat(deleteAuthorized(moneyBoxesUrl + "/" + firstBox.id(), Void.class).getStatusCode())
                .isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(getAuthorized(moneyBoxesUrl + "/" + firstBox.id(), String.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(jdbcTemplate.queryForObject(
                "SELECT COUNT(*) FROM user_money_movements WHERE money_box_id = ?",
                Integer.class,
                firstBox.id()
        )).isZero();
    }

    @Test
    void userWeightsShouldStoreDateTimeAndReturnPeriodAndStats() {
        Long userId = authenticatedUserId();
        String weightsUrl = "http://localhost:" + port + "/api/v1/users/" + userId + "/weights";
        Instant beforePeriod = Instant.parse("2026-05-31T23:59:59Z");
        Instant firstHighest = Instant.parse("2026-06-10T07:30:00Z");
        Instant lowest = Instant.parse("2026-06-15T18:45:00Z");
        Instant tiedHighest = Instant.parse("2026-06-20T09:15:00Z");

        postAuthorized(weightsUrl, new CreateUserWeightRequest(new BigDecimal("80.00"), beforePeriod), UserWeightResponse.class);
        ResponseEntity<UserWeightResponse> created = postAuthorized(
                weightsUrl,
                new CreateUserWeightRequest(new BigDecimal("75.40"), firstHighest),
                UserWeightResponse.class
        );
        postAuthorized(weightsUrl, new CreateUserWeightRequest(new BigDecimal("72.10"), lowest), UserWeightResponse.class);
        postAuthorized(weightsUrl, new CreateUserWeightRequest(new BigDecimal("75.40"), tiedHighest), UserWeightResponse.class);

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).isNotNull();
        assertThat(created.getBody().weight()).isEqualByComparingTo("75.40");
        assertThat(created.getBody().recordedAt()).isEqualTo(firstHighest);

        String periodQuery = "?from=2026-06-01T00:00:00Z&to=2026-06-30T23:59:59Z";
        ResponseEntity<UserWeightResponse[]> period = getAuthorized(weightsUrl + periodQuery, UserWeightResponse[].class);

        assertThat(period.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(period.getBody()).isNotNull();
        assertThat(period.getBody()).extracting(UserWeightResponse::recordedAt)
                .containsExactly(firstHighest, lowest, tiedHighest);

        ResponseEntity<UserWeightStatsResponse> stats = getAuthorized(
                weightsUrl + "/stats" + periodQuery,
                UserWeightStatsResponse.class
        );

        assertThat(stats.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(stats.getBody()).isNotNull();
        assertThat(stats.getBody().highest().weight()).isEqualByComparingTo("75.40");
        assertThat(stats.getBody().highest().recordedAt()).isEqualTo(firstHighest);
        assertThat(stats.getBody().lowest().weight()).isEqualByComparingTo("72.10");
        assertThat(stats.getBody().lowest().recordedAt()).isEqualTo(lowest);

        ResponseEntity<String> invalidPeriod = getAuthorized(
                weightsUrl + "?from=2026-07-01T00:00:00Z&to=2026-06-01T00:00:00Z",
                String.class
        );
        assertThat(invalidPeriod.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void userWeightsShouldUpdateRecalculateStatsDeleteAndRejectWrongUser() {
        Long userId = authenticatedUserId();
        String weightsUrl = "http://localhost:" + port + "/api/v1/users/" + userId + "/weights";
        String periodQuery = "?from=2026-06-01T00:00:00Z&to=2026-06-30T23:59:59Z";
        Instant originalAt = Instant.parse("2026-06-05T08:00:00Z");
        Instant editedAt = Instant.parse("2026-06-25T19:30:00Z");

        ResponseEntity<UserWeightResponse> editable = postAuthorized(
                weightsUrl,
                new CreateUserWeightRequest(new BigDecimal("90.00"), originalAt),
                UserWeightResponse.class
        );
        postAuthorized(
                weightsUrl,
                new CreateUserWeightRequest(new BigDecimal("75.00"), Instant.parse("2026-06-10T08:00:00Z")),
                UserWeightResponse.class
        );
        assertThat(editable.getBody()).isNotNull();
        String editableUrl = weightsUrl + "/" + editable.getBody().id();

        ResponseEntity<UserWeightResponse> updated = putAuthorized(
                editableUrl,
                new UpdateUserWeightRequest(new BigDecimal("70.25"), editedAt),
                UserWeightResponse.class
        );
        assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updated.getBody()).isNotNull();
        assertThat(updated.getBody().weight()).isEqualByComparingTo("70.25");
        assertThat(updated.getBody().recordedAt()).isEqualTo(editedAt);

        ResponseEntity<UserWeightResponse[]> reordered = getAuthorized(weightsUrl + periodQuery, UserWeightResponse[].class);
        assertThat(reordered.getBody()).isNotNull();
        assertThat(reordered.getBody()).extracting(UserWeightResponse::recordedAt)
                .containsExactly(Instant.parse("2026-06-10T08:00:00Z"), editedAt);

        ResponseEntity<UserWeightStatsResponse> updatedStats = getAuthorized(
                weightsUrl + "/stats" + periodQuery,
                UserWeightStatsResponse.class
        );
        assertThat(updatedStats.getBody()).isNotNull();
        assertThat(updatedStats.getBody().highest().weight()).isEqualByComparingTo("75.00");
        assertThat(updatedStats.getBody().lowest().weight()).isEqualByComparingTo("70.25");

        ResponseEntity<String> invalid = putAuthorized(
                editableUrl,
                new UpdateUserWeightRequest(BigDecimal.ZERO, editedAt),
                String.class
        );
        assertThat(invalid.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        ResponseEntity<AuthResponse> otherUser = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1/auth/register",
                new RegisterRequest("other-weight-user-" + System.nanoTime(), "secret-password", REGISTRATION_CODE),
                AuthResponse.class
        );
        assertThat(otherUser.getBody()).isNotNull();
        String wrongUserUrl = "http://localhost:" + port + "/api/v1/users/"
                + otherUser.getBody().userId() + "/weights/" + editable.getBody().id();
        assertThat(putAuthorized(
                wrongUserUrl,
                new UpdateUserWeightRequest(new BigDecimal("65.00"), editedAt),
                String.class
        ).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(deleteAuthorized(wrongUserUrl, String.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        assertThat(deleteAuthorized(editableUrl, Void.class).getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        ResponseEntity<UserWeightStatsResponse> statsAfterDelete = getAuthorized(
                weightsUrl + "/stats" + periodQuery,
                UserWeightStatsResponse.class
        );
        assertThat(statsAfterDelete.getBody()).isNotNull();
        assertThat(statsAfterDelete.getBody().highest().weight()).isEqualByComparingTo("75.00");
        assertThat(statsAfterDelete.getBody().lowest().weight()).isEqualByComparingTo("75.00");
        assertThat(deleteAuthorized(editableUrl, String.class).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
    }

    @Test
    void proposedWeekMenuShouldStartEmptyAndCalculateOrderedDayTotals() {
        String productsUrl = "http://localhost:" + port + "/api/v1/products";
        String dayPartsUrl = "http://localhost:" + port + "/api/v1/planning/day-parts";
        String proposedMenusUrl = "http://localhost:" + port + "/api/v1/planning";
        Long yogurtId = createProduct(productsUrl, "Menu Yogurt", "Greek yogurt", "59", "3.6", "10", "0.4", "125");
        Long almondsId = createProduct(productsUrl, "Menu Almonds", "Raw almonds", "579", "22", "21", "50", "30");
        Long lunchDayPartId = createDayPart(dayPartsUrl, "Lunch", "Main meal of the day", 10);
        Long snackDayPartId = createDayPart(dayPartsUrl, "Snack", "Light meal between main meals", 20);

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
                                        snackDayPartId,
                                        List.of(new ProposedWeekMenuProductRequest(almondsId, new BigDecimal("2"), null, 10))
                                ),
                                new ProposedWeekMenuSectionRequest(
                                        lunchDayPartId,
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
        assertThat(updatedMenu.getBody().days().getFirst().sections())
                .extracting(section -> section.dayPartId())
                .containsExactly(lunchDayPartId, snackDayPartId);
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
    void planningCatalogShouldReturnCompactSummariesOrderedByDateWithDerivedState() {
        String planningUrl = "http://localhost:" + port + "/api/v1/planning";
        ProposedWeekMenuResponse draft = postAuthorized(
                planningUrl,
                new CreateProposedWeekMenuRequest(LocalDate.of(2040, 3, 1), LocalDate.of(2040, 3, 7)),
                ProposedWeekMenuResponse.class
        ).getBody();
        ProposedWeekMenuResponse establishedPlanning = postAuthorized(
                planningUrl,
                new CreateProposedWeekMenuRequest(LocalDate.of(2040, 2, 1), LocalDate.of(2040, 2, 7)),
                ProposedWeekMenuResponse.class
        ).getBody();
        ProposedWeekMenuResponse closedPlanning = postAuthorized(
                planningUrl,
                new CreateProposedWeekMenuRequest(LocalDate.of(2020, 1, 1), LocalDate.of(2020, 1, 7)),
                ProposedWeekMenuResponse.class
        ).getBody();
        CurrentWeekMenuResponse established = postAuthorized(
                planningUrl + "/" + establishedPlanning.id() + "/menu",
                new EstablishProposedWeekMenuRequest(authenticatedUserId()),
                CurrentWeekMenuResponse.class
        ).getBody();
        CurrentWeekMenuResponse closedMenu = postAuthorized(
                planningUrl + "/" + closedPlanning.id() + "/menu",
                new EstablishProposedWeekMenuRequest(authenticatedUserId()),
                CurrentWeekMenuResponse.class
        ).getBody();
        postAuthorized(
                "http://localhost:" + port + "/api/v1/menus/" + closedMenu.id() + "/close",
                new CloseCurrentWeekMenuRequest(List.of(authenticatedUserId())),
                CurrentWeekMenuStatsResponse.class
        );

        PlanningSummaryResponse[] summaries = getAuthorized(planningUrl, PlanningSummaryResponse[].class).getBody();
        Map<Long, PlanningSummaryResponse> byId = java.util.Arrays.stream(summaries)
                .collect(java.util.stream.Collectors.toMap(PlanningSummaryResponse::id, summary -> summary));
        assertThat(byId.get(draft.id()).state()).isEqualTo(com.eliascanalesnieto.foodhelper.domain.PlanningState.DRAFT);
        assertThat(byId.get(draft.id()).menuId()).isNull();
        assertThat(byId.get(establishedPlanning.id()).state()).isEqualTo(com.eliascanalesnieto.foodhelper.domain.PlanningState.ESTABLISHED);
        assertThat(byId.get(establishedPlanning.id()).menuId()).isEqualTo(established.id());
        assertThat(byId.get(closedPlanning.id()).state()).isEqualTo(com.eliascanalesnieto.foodhelper.domain.PlanningState.CLOSED);
        assertThat(byId.get(closedPlanning.id()).menuId()).isEqualTo(closedMenu.id());
        assertThat(byId.values()).allSatisfy(summary -> assertThat(summary.plannedDays()).isGreaterThanOrEqualTo(0));
        assertThat(java.util.Arrays.stream(summaries).map(PlanningSummaryResponse::startDate).toList())
                .isSortedAccordingTo(java.util.Comparator.reverseOrder());
    }

    @Test
    void proposedWeekMenuStockSummaryShouldHandleEmptyAndPlannedDaysWithStockAllocation() {
        String productsUrl = "http://localhost:" + port + "/api/v1/products";
        String dayPartsUrl = "http://localhost:" + port + "/api/v1/planning/day-parts";
        String stockUrl = "http://localhost:" + port + "/api/v1/stock";
        String proposedMenusUrl = "http://localhost:" + port + "/api/v1/planning";

        Long chickenId = createProduct(productsUrl, "Menu Stock Chicken", "Chicken breast", "100", "0", "0", "0", "100");
        Long riceId = createProduct(productsUrl, "Menu Stock Rice", "Rice", "100", "0", "0", "0", "100");
        Long beansId = createProduct(productsUrl, "Menu Stock Beans", "Beans", "100", "0", "0", "0", "100");
        Long lunchDayPartId = createDayPart(dayPartsUrl, "Lunch stock", "Main meal of the day", 10);
        Long dinnerDayPartId = createDayPart(dayPartsUrl, "Dinner stock", "Evening meal", 20);

        ResponseEntity<ProposedWeekMenuResponse> emptyMenu = postAuthorized(
                proposedMenusUrl,
                new CreateProposedWeekMenuRequest(LocalDate.of(2026, 6, 15), LocalDate.of(2026, 6, 21)),
                ProposedWeekMenuResponse.class
        );
        assertThat(emptyMenu.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(emptyMenu.getBody()).isNotNull();
        assertThat(emptyMenu.getBody().stockSummary()).isNotNull();
        assertThat(emptyMenu.getBody().stockSummary().plannedDays()).isZero();
        assertThat(emptyMenu.getBody().stockSummary().distinctProducts()).isZero();
        assertThat(emptyMenu.getBody().stockSummary().calories().averagePerPlannedDay()).isEqualByComparingTo("0.00");
        assertThat(emptyMenu.getBody().stockSummary().calories().maxDay()).isNull();
        assertThat(emptyMenu.getBody().stockSummary().calories().minDay()).isNull();
        assertThat(emptyMenu.getBody().stockSummary().requirements()).isEmpty();

        ResponseEntity<StockEntryResponse> chickenLotOne = postAuthorized(
                productsUrl + "/" + chickenId + "/stock",
                new CreateStockEntryRequest(new BigDecimal("1.5"), new BigDecimal("2.00"), LocalDate.of(2026, 6, 16), LocalDate.of(2026, 6, 10)),
                StockEntryResponse.class
        );
        ResponseEntity<StockEntryResponse> chickenLotTwo = postAuthorized(
                productsUrl + "/" + chickenId + "/stock",
                new CreateStockEntryRequest(new BigDecimal("2.0"), new BigDecimal("3.00"), LocalDate.of(2026, 6, 20), LocalDate.of(2026, 6, 11)),
                StockEntryResponse.class
        );
        ResponseEntity<StockEntryResponse> beansLot = postAuthorized(
                productsUrl + "/" + beansId + "/stock",
                new CreateStockEntryRequest(new BigDecimal("0.5"), new BigDecimal("1.50"), null, LocalDate.of(2026, 6, 12)),
                StockEntryResponse.class
        );
        assertThat(chickenLotOne.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(chickenLotTwo.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(beansLot.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<ProposedWeekMenuResponse> firstUpdatedMenu = restTemplate.exchange(
                proposedMenusUrl + "/" + emptyMenu.getBody().id() + "/days",
                HttpMethod.PUT,
                authorizedEntity(new UpsertProposedWeekMenuDayRequest(
                        LocalDate.of(2026, 6, 15),
                        List.of(
                                new ProposedWeekMenuSectionRequest(
                                        lunchDayPartId,
                                        List.of(
                                                new ProposedWeekMenuProductRequest(chickenId, new BigDecimal("1"), null, 10),
                                                new ProposedWeekMenuProductRequest(riceId, new BigDecimal("2"), null, 20)
                                        )
                                ),
                                new ProposedWeekMenuSectionRequest(
                                        dinnerDayPartId,
                                        List.of(new ProposedWeekMenuProductRequest(chickenId, new BigDecimal("1"), null, 10))
                                )
                        )
                )),
                ProposedWeekMenuResponse.class
        );
        assertThat(firstUpdatedMenu.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<ProposedWeekMenuResponse> secondUpdatedMenu = restTemplate.exchange(
                proposedMenusUrl + "/" + emptyMenu.getBody().id() + "/days",
                HttpMethod.PUT,
                authorizedEntity(new UpsertProposedWeekMenuDayRequest(
                        LocalDate.of(2026, 6, 16),
                        List.of(
                                new ProposedWeekMenuSectionRequest(
                                        lunchDayPartId,
                                        List.of(
                                                new ProposedWeekMenuProductRequest(chickenId, new BigDecimal("1.5"), null, 10),
                                                new ProposedWeekMenuProductRequest(beansId, null, new BigDecimal("100"), 20)
                                        )
                                )
                        )
                )),
                ProposedWeekMenuResponse.class
        );

        assertThat(secondUpdatedMenu.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(secondUpdatedMenu.getBody()).isNotNull();

        ProposedWeekMenuStockSummaryResponse stockSummary = secondUpdatedMenu.getBody().stockSummary();
        assertThat(stockSummary.plannedDays()).isEqualTo(2);
        assertThat(stockSummary.distinctProducts()).isEqualTo(3);
        assertThat(stockSummary.calories().averagePerPlannedDay()).isEqualByComparingTo("325.00");
        assertThat(stockSummary.calories().maxDay().date()).isEqualTo(LocalDate.of(2026, 6, 15));
        assertThat(stockSummary.calories().maxDay().calories()).isEqualByComparingTo("400.00");
        assertThat(stockSummary.calories().minDay().date()).isEqualTo(LocalDate.of(2026, 6, 16));
        assertThat(stockSummary.calories().minDay().calories()).isEqualByComparingTo("250.00");
        assertThat(stockSummary.estimatedCost()).isEqualByComparingTo("9.75");

        assertThat(stockSummary.requirements())
                .filteredOn(requirement -> requirement.productId().equals(chickenId))
                .singleElement()
                .satisfies(requirement -> {
                    assertThat(requirement.productName()).isEqualTo("Menu Stock Chicken");
                    assertThat(requirement.requiredUnits()).isEqualByComparingTo("3.50");
                    assertThat(requirement.availableUnits()).isEqualByComparingTo("3.50");
                    assertThat(requirement.coveredUnits()).isEqualByComparingTo("3.50");
                    assertThat(requirement.missingUnits()).isEqualByComparingTo("0.00");
                    assertThat(requirement.estimatedCost()).isEqualByComparingTo("9.00");
                });
        assertThat(stockSummary.requirements())
                .filteredOn(requirement -> requirement.productId().equals(riceId))
                .singleElement()
                .satisfies(requirement -> {
                    assertThat(requirement.requiredUnits()).isEqualByComparingTo("2.00");
                    assertThat(requirement.availableUnits()).isEqualByComparingTo("0.00");
                    assertThat(requirement.coveredUnits()).isEqualByComparingTo("0.00");
                    assertThat(requirement.missingUnits()).isEqualByComparingTo("2.00");
                    assertThat(requirement.estimatedCost()).isEqualByComparingTo("0.00");
                });
        assertThat(stockSummary.requirements())
                .filteredOn(requirement -> requirement.productId().equals(beansId))
                .singleElement()
                .satisfies(requirement -> {
                    assertThat(requirement.requiredUnits()).isEqualByComparingTo("1.00");
                    assertThat(requirement.availableUnits()).isEqualByComparingTo("0.50");
                    assertThat(requirement.coveredUnits()).isEqualByComparingTo("0.50");
                    assertThat(requirement.missingUnits()).isEqualByComparingTo("0.50");
                    assertThat(requirement.estimatedCost()).isEqualByComparingTo("0.75");
                });
    }

    @Test
    void establishingAProposedWeekShouldCreateCurrentWeekSnapshotConsumeStockExposeMissingItemsAndAllowClosing() {
        String productsUrl = "http://localhost:" + port + "/api/v1/products";
        String dayPartsUrl = "http://localhost:" + port + "/api/v1/planning/day-parts";
        String stockUrl = "http://localhost:" + port + "/api/v1/stock";
        String proposedMenusUrl = "http://localhost:" + port + "/api/v1/planning";
        String currentWeekMenusUrl = "http://localhost:" + port + "/api/v1/menus";
        String supermarketsUrl = "http://localhost:" + port + "/api/v1/supermarkets";
        String payerMoneyBoxUrl = "http://localhost:" + port + "/api/v1/users/" + authenticatedUserId() + "/money-box";
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(8);
        LocalDate endDate = today.minusDays(1);

        ResponseEntity<UserMoneyMovementResponse> positiveMovement = postAuthorized(
                payerMoneyBoxUrl + "/movements",
                new CreateUserMoneyMovementRequest(new BigDecimal("10.00"), "Initial contribution"),
                UserMoneyMovementResponse.class
        );
        ResponseEntity<UserMoneyMovementResponse> negativeMovement = postAuthorized(
                payerMoneyBoxUrl + "/movements",
                new CreateUserMoneyMovementRequest(new BigDecimal("-2.00"), "Manual adjustment"),
                UserMoneyMovementResponse.class
        );
        assertThat(positiveMovement.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(negativeMovement.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        Long marketAId = createSupermarket(supermarketsUrl, "Current Week Market A");
        Long marketBId = createSupermarket(supermarketsUrl, "Current Week Market B");
        Long chickenId = createProduct(productsUrl, "Current Week Chicken", "Chicken breast", "200", "0", "31", "3.6", "2.00", List.of(marketAId));
        Long riceId = createProduct(productsUrl, "Current Week Rice", "Rice", "100", "0", "2.7", "0.3", "1.20", List.of(marketAId));
        Long beansId = createProduct(productsUrl, "Current Week Beans", "Beans", "100", "22", "8", "1.2", "1.50", List.of(marketBId));
        Long lunchDayPartId = createDayPart(dayPartsUrl, "Lunch current", "Main meal of the day", 10);

        ResponseEntity<StockEntryResponse> chickenStock = postAuthorized(
                productsUrl + "/" + chickenId + "/stock",
                new CreateStockEntryRequest(new BigDecimal("1.50"), new BigDecimal("2.00"), LocalDate.of(2026, 6, 20), LocalDate.of(2026, 6, 10)),
                StockEntryResponse.class
        );
        ResponseEntity<StockEntryResponse> beansStock = postAuthorized(
                productsUrl + "/" + beansId + "/stock",
                new CreateStockEntryRequest(new BigDecimal("0.50"), new BigDecimal("1.50"), null, LocalDate.of(2026, 6, 11)),
                StockEntryResponse.class
        );
        assertThat(chickenStock.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(beansStock.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<ProposedWeekMenuResponse> createdMenu = postAuthorized(
                proposedMenusUrl,
                new CreateProposedWeekMenuRequest(startDate, endDate),
                ProposedWeekMenuResponse.class
        );
        assertThat(createdMenu.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<ProposedWeekMenuResponse> plannedMenu = restTemplate.exchange(
                proposedMenusUrl + "/" + createdMenu.getBody().id() + "/days",
                HttpMethod.PUT,
                authorizedEntity(new UpsertProposedWeekMenuDayRequest(
                        startDate,
                        List.of(
                                new ProposedWeekMenuSectionRequest(
                                        lunchDayPartId,
                                        List.of(
                                                new ProposedWeekMenuProductRequest(chickenId, new BigDecimal("1.50"), null, 10),
                                                new ProposedWeekMenuProductRequest(riceId, new BigDecimal("2.00"), null, 20),
                                                new ProposedWeekMenuProductRequest(beansId, new BigDecimal("1.00"), null, 30)
                                        )
                                )
                        )
                )),
                ProposedWeekMenuResponse.class
        );
        assertThat(plannedMenu.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(plannedMenu.getBody()).isNotNull();
        assertThat(plannedMenu.getBody().stockSummary().estimatedCost()).isEqualByComparingTo("3.75");

        ResponseEntity<CurrentWeekMenuResponse> established = postAuthorized(
                proposedMenusUrl + "/" + createdMenu.getBody().id() + "/menu",
                new EstablishProposedWeekMenuRequest(authenticatedUserId()),
                CurrentWeekMenuResponse.class
        );
        assertThat(established.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(established.getBody()).isNotNull();
        assertThat(established.getBody().planningId()).isEqualTo(createdMenu.getBody().id());
        assertThat(established.getBody().payerUserId()).isEqualTo(authenticatedUserId());
        assertThat(established.getBody().nutritionalValues().calories()).isEqualByComparingTo(plannedMenu.getBody().nutritionalValues().calories());
        assertThat(established.getBody().stockSummary().estimatedCost()).isEqualByComparingTo("3.75");
        assertThat(established.getBody().usedStock()).hasSize(2);
        assertThat(established.getBody().shoppingList()).hasSize(2);

        Long currentWeekId = established.getBody().id();
        ResponseEntity<UserMoneyBoxResponse> payerMoneyBox = getAuthorized(
                payerMoneyBoxUrl,
                UserMoneyBoxResponse.class
        );
        assertThat(payerMoneyBox.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(payerMoneyBox.getBody()).isNotNull();
        assertThat(payerMoneyBox.getBody().balance()).isEqualByComparingTo("4.25");
        assertThat(payerMoneyBox.getBody().movements()).hasSize(3);
        assertThat(payerMoneyBox.getBody().movements().getFirst().menuId()).isEqualTo(currentWeekId);

        ResponseEntity<CurrentWeekMenuStatsResponse> closed = postAuthorized(
                currentWeekMenusUrl + "/" + currentWeekId + "/close",
                new CloseCurrentWeekMenuRequest(List.of(authenticatedUserId())),
                CurrentWeekMenuStatsResponse.class
        );
        assertThat(closed.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(closed.getBody()).isNotNull();
        assertThat(closed.getBody().menuId()).isEqualTo(currentWeekId);
        assertThat(closed.getBody().period()).isNotNull();
        assertThat(closed.getBody().month()).isNotNull();
        assertThat(closed.getBody().period().maxDay().date()).isEqualTo(startDate);
        assertThat(closed.getBody().period().minDay().date()).isEqualTo(startDate);
        assertThat(closed.getBody().period().moneySpent()).isEqualByComparingTo("3.75");
        assertThat(closed.getBody().month().moneySpent()).isEqualByComparingTo("3.75");

        ResponseEntity<CurrentWeekMenuStatsResponse> stats = getAuthorized(
                currentWeekMenusUrl + "/" + currentWeekId + "/stats",
                CurrentWeekMenuStatsResponse.class
        );
        assertThat(stats.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(stats.getBody()).isEqualTo(closed.getBody());

        ResponseEntity<CurrentWeekMenuResponse> loadedCurrentWeek = getAuthorized(
                currentWeekMenusUrl + "/" + currentWeekId,
                CurrentWeekMenuResponse.class
        );
        assertThat(loadedCurrentWeek.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(loadedCurrentWeek.getBody()).isNotNull();
        assertThat(loadedCurrentWeek.getBody().days()).hasSize(1);
        assertThat(loadedCurrentWeek.getBody().days().getFirst().sections()).hasSize(1);

        ResponseEntity<CurrentWeekMenuUsedStockResponse[]> usedStock = getAuthorized(
                currentWeekMenusUrl + "/" + currentWeekId + "/used-stock",
                CurrentWeekMenuUsedStockResponse[].class
        );
        assertThat(usedStock.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(usedStock.getBody()).isNotNull();
        assertThat(usedStock.getBody()).hasSize(2);
        assertThat(usedStock.getBody())
                .extracting(CurrentWeekMenuUsedStockResponse::productId)
                .containsExactlyInAnyOrder(chickenId, beansId);

        ResponseEntity<CurrentWeekMenuShoppingListItemResponse[]> shoppingList = getAuthorized(
                currentWeekMenusUrl + "/" + currentWeekId + "/shopping-list",
                CurrentWeekMenuShoppingListItemResponse[].class
        );
        assertThat(shoppingList.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(shoppingList.getBody()).isNotNull();
        assertThat(shoppingList.getBody()).hasSize(2);
        assertThat(shoppingList.getBody())
                .filteredOn(item -> item.productId().equals(riceId))
                .singleElement()
                .satisfies(item -> assertThat(item.missingUnits()).isEqualByComparingTo("2.00"));

        ResponseEntity<CurrentWeekMenuShoppingListItemResponse[]> marketAList = getAuthorized(
                currentWeekMenusUrl + "/" + currentWeekId + "/shopping-list?supermarketId=" + marketAId,
                CurrentWeekMenuShoppingListItemResponse[].class
        );
        assertThat(marketAList.getBody()).extracting(CurrentWeekMenuShoppingListItemResponse::productId)
                .containsExactly(riceId);

        ResponseEntity<CurrentWeekMenuShoppingListItemResponse[]> marketBList = getAuthorized(
                currentWeekMenusUrl + "/" + currentWeekId + "/shopping-list?supermarketId=" + marketBId,
                CurrentWeekMenuShoppingListItemResponse[].class
        );
        assertThat(marketBList.getBody()).extracting(CurrentWeekMenuShoppingListItemResponse::productId)
                .containsExactly(beansId);
        assertThat(marketBList.getBody()[0].missingUnits()).isEqualByComparingTo("0.50");

        ResponseEntity<CurrentWeekMenuShoppingListItemResponse[]> shoppingListAgain = getAuthorized(
                currentWeekMenusUrl + "/" + currentWeekId + "/shopping-list",
                CurrentWeekMenuShoppingListItemResponse[].class
        );
        assertThat(shoppingListAgain.getBody()).containsExactly(shoppingList.getBody());

        ResponseEntity<String> missingSupermarket = getAuthorized(
                currentWeekMenusUrl + "/" + currentWeekId + "/shopping-list?supermarketId=999999999",
                String.class
        );
        assertThat(missingSupermarket.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        ResponseEntity<StockEntryResponse[]> chickenRemaining = getAuthorized(
                productsUrl + "/" + chickenId + "/stock",
                StockEntryResponse[].class
        );
        ResponseEntity<StockEntryResponse[]> beansRemaining = getAuthorized(
                productsUrl + "/" + beansId + "/stock",
                StockEntryResponse[].class
        );
        ResponseEntity<StockEntryResponse[]> riceRemaining = getAuthorized(
                productsUrl + "/" + riceId + "/stock",
                StockEntryResponse[].class
        );
        assertThat(chickenRemaining.getBody()).isEmpty();
        assertThat(beansRemaining.getBody()).isEmpty();
        assertThat(riceRemaining.getBody()).isEmpty();

        ResponseEntity<String> rejectedUpdate = restTemplate.exchange(
                proposedMenusUrl + "/" + createdMenu.getBody().id() + "/days",
                HttpMethod.PUT,
                authorizedEntity(new UpsertProposedWeekMenuDayRequest(
                        startDate,
                        List.of(
                                new ProposedWeekMenuSectionRequest(
                                        lunchDayPartId,
                                        List.of(new ProposedWeekMenuProductRequest(chickenId, new BigDecimal("1.00"), null, 10))
                                )
                        )
                )),
                String.class
        );
        assertThat(rejectedUpdate.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(rejectedUpdate.getBody()).contains("already closed");
    }

    @Test
    void closingBeforeTheWeekEndsShouldBeRejected() {
        String productsUrl = "http://localhost:" + port + "/api/v1/products";
        String proposedMenusUrl = "http://localhost:" + port + "/api/v1/planning";
        LocalDate today = LocalDate.now();
        Long chickenId = createProduct(productsUrl, "Future Week Chicken", "Chicken breast", "200", "0", "31", "3.6", "2.00");

        ResponseEntity<ProposedWeekMenuResponse> createdMenu = postAuthorized(
                proposedMenusUrl,
                new CreateProposedWeekMenuRequest(today, today.plusDays(7)),
                ProposedWeekMenuResponse.class
        );
        assertThat(createdMenu.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<CurrentWeekMenuResponse> established = postAuthorized(
                proposedMenusUrl + "/" + createdMenu.getBody().id() + "/menu",
                new EstablishProposedWeekMenuRequest(authenticatedUserId()),
                CurrentWeekMenuResponse.class
        );
        assertThat(established.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<String> rejectedClose = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/menus/" + established.getBody().id() + "/close",
                HttpMethod.POST,
                authorizedEntity(new CloseCurrentWeekMenuRequest(List.of(authenticatedUserId()))),
                String.class
        );
        assertThat(rejectedClose.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(rejectedClose.getBody()).contains("Menu can only be closed after its end date");
    }

    @Test
    void closeShouldRequireExistingPeopleAndRemainIdempotentForMultiplePeople() {
        String planningUrl = "http://localhost:" + port + "/api/v1/planning";
        AuthResponse secondPerson = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1/auth/register",
                new RegisterRequest("second-person-" + System.nanoTime(), "secret-password", REGISTRATION_CODE),
                AuthResponse.class
        ).getBody();
        ProposedWeekMenuResponse planning = postAuthorized(
                planningUrl,
                new CreateProposedWeekMenuRequest(LocalDate.of(2019, 1, 1), LocalDate.of(2019, 1, 7)),
                ProposedWeekMenuResponse.class
        ).getBody();
        CurrentWeekMenuResponse menu = postAuthorized(
                planningUrl + "/" + planning.id() + "/menu",
                new EstablishProposedWeekMenuRequest(authenticatedUserId()),
                CurrentWeekMenuResponse.class
        ).getBody();
        String closeUrl = "http://localhost:" + port + "/api/v1/menus/" + menu.id() + "/close";

        assertThat(postAuthorized(closeUrl, null, String.class).getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(postAuthorized(closeUrl, new CloseCurrentWeekMenuRequest(List.of()), String.class).getStatusCode())
                .isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(postAuthorized(closeUrl, new CloseCurrentWeekMenuRequest(List.of(999999999L)), String.class).getStatusCode())
                .isEqualTo(HttpStatus.NOT_FOUND);

        CloseCurrentWeekMenuRequest request = new CloseCurrentWeekMenuRequest(
                List.of(authenticatedUserId(), secondPerson.userId())
        );
        CurrentWeekMenuStatsResponse first = postAuthorized(closeUrl, request, CurrentWeekMenuStatsResponse.class).getBody();
        CurrentWeekMenuStatsResponse repeated = postAuthorized(closeUrl, request, CurrentWeekMenuStatsResponse.class).getBody();
        assertThat(repeated).isEqualTo(first);
        assertThat(getAuthorized(
                "http://localhost:" + port + "/api/v1/users/" + authenticatedUserId()
                        + "/menu-history/annual?year=2019",
                com.eliascanalesnieto.foodhelper.presentation.UserMenuHistoryResponse.class
        ).getBody().menus()).extracting(com.eliascanalesnieto.foodhelper.presentation.UserMenuHistoryEntryResponse::menuId)
                .contains(menu.id());
        assertThat(getAuthorized(
                "http://localhost:" + port + "/api/v1/users/" + secondPerson.userId()
                        + "/menu-history/annual?year=2019",
                com.eliascanalesnieto.foodhelper.presentation.UserMenuHistoryResponse.class
        ).getBody().menus()).extracting(com.eliascanalesnieto.foodhelper.presentation.UserMenuHistoryEntryResponse::menuId)
                .contains(menu.id());
    }

    @Test
    void proposedWeekMenuShouldRejectRepeatedDayPartsWithinSameDay() {
        String productsUrl = "http://localhost:" + port + "/api/v1/products";
        String dayPartsUrl = "http://localhost:" + port + "/api/v1/planning/day-parts";
        String proposedMenusUrl = "http://localhost:" + port + "/api/v1/planning";
        Long yogurtId = createProduct(productsUrl, "Duplicate Menu Yogurt", "Greek yogurt", "59", "3.6", "10", "0.4", "125");
        Long lunchDayPartId = createDayPart(dayPartsUrl, "Lunch duplicate", "Main meal of the day", 10);

        ResponseEntity<ProposedWeekMenuResponse> createdMenu = postAuthorized(
                proposedMenusUrl,
                new CreateProposedWeekMenuRequest(LocalDate.of(2026, 6, 15), LocalDate.of(2026, 6, 21)),
                ProposedWeekMenuResponse.class
        );
        assertThat(createdMenu.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<String> rejectedMenu = restTemplate.exchange(
                proposedMenusUrl + "/" + createdMenu.getBody().id() + "/days",
                HttpMethod.PUT,
                authorizedEntity(new UpsertProposedWeekMenuDayRequest(
                        LocalDate.of(2026, 6, 15),
                        List.of(
                                new ProposedWeekMenuSectionRequest(
                                        lunchDayPartId,
                                        List.of(new ProposedWeekMenuProductRequest(yogurtId, new BigDecimal("1"), null, 10))
                                ),
                                new ProposedWeekMenuSectionRequest(
                                        lunchDayPartId,
                                        List.of(new ProposedWeekMenuProductRequest(yogurtId, new BigDecimal("1"), null, 20))
                                )
                        )
                )),
                String.class
        );

        assertThat(rejectedMenu.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(rejectedMenu.getBody()).contains("Day parts must be unique within a day");
    }

    @Test
    void proposedWeekMenuShouldRejectRepeatedProductSortOrdersWithinTheSameSection() {
        String productsUrl = "http://localhost:" + port + "/api/v1/products";
        String dayPartsUrl = "http://localhost:" + port + "/api/v1/planning/day-parts";
        String proposedMenusUrl = "http://localhost:" + port + "/api/v1/planning";
        Long yogurtId = createProduct(productsUrl, "Duplicate Sort Yogurt", "Greek yogurt", "59", "3.6", "10", "0.4", "125");
        Long almondsId = createProduct(productsUrl, "Duplicate Sort Almonds", "Raw almonds", "579", "22", "21", "50", "30");
        Long lunchDayPartId = createDayPart(dayPartsUrl, "Lunch duplicate sort", "Main meal of the day", 10);

        ResponseEntity<ProposedWeekMenuResponse> createdMenu = postAuthorized(
                proposedMenusUrl,
                new CreateProposedWeekMenuRequest(LocalDate.of(2026, 6, 15), LocalDate.of(2026, 6, 21)),
                ProposedWeekMenuResponse.class
        );
        assertThat(createdMenu.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<String> rejectedMenu = restTemplate.exchange(
                proposedMenusUrl + "/" + createdMenu.getBody().id() + "/days",
                HttpMethod.PUT,
                authorizedEntity(new UpsertProposedWeekMenuDayRequest(
                        LocalDate.of(2026, 6, 15),
                        List.of(
                                new ProposedWeekMenuSectionRequest(
                                        lunchDayPartId,
                                        List.of(
                                                new ProposedWeekMenuProductRequest(yogurtId, new BigDecimal("1"), null, 10),
                                                new ProposedWeekMenuProductRequest(almondsId, new BigDecimal("2"), null, 10)
                                        )
                                )
                        )
                )),
                String.class
        );

        assertThat(rejectedMenu.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(rejectedMenu.getBody()).contains("Product sortOrder must be unique within each section");
    }

    @Test
    void proposedWeekMenuShouldAcceptRepeatedSortOrdersAcrossDifferentSectionsAndUniqueSortOrdersWithinEachSection() {
        String productsUrl = "http://localhost:" + port + "/api/v1/products";
        String dayPartsUrl = "http://localhost:" + port + "/api/v1/planning/day-parts";
        String proposedMenusUrl = "http://localhost:" + port + "/api/v1/planning";
        Long yogurtId = createProduct(productsUrl, "Valid Sort Yogurt", "Greek yogurt", "59", "3.6", "10", "0.4", "125");
        Long almondsId = createProduct(productsUrl, "Valid Sort Almonds", "Raw almonds", "579", "22", "21", "50", "30");
        Long lunchDayPartId = createDayPart(dayPartsUrl, "Lunch valid sort", "Main meal of the day", 10);
        Long snackDayPartId = createDayPart(dayPartsUrl, "Snack valid sort", "Light meal between main meals", 20);

        ResponseEntity<ProposedWeekMenuResponse> createdMenu = postAuthorized(
                proposedMenusUrl,
                new CreateProposedWeekMenuRequest(LocalDate.of(2026, 6, 15), LocalDate.of(2026, 6, 21)),
                ProposedWeekMenuResponse.class
        );
        assertThat(createdMenu.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<ProposedWeekMenuResponse> updatedMenu = restTemplate.exchange(
                proposedMenusUrl + "/" + createdMenu.getBody().id() + "/days",
                HttpMethod.PUT,
                authorizedEntity(new UpsertProposedWeekMenuDayRequest(
                        LocalDate.of(2026, 6, 15),
                        List.of(
                                new ProposedWeekMenuSectionRequest(
                                        lunchDayPartId,
                                        List.of(
                                                new ProposedWeekMenuProductRequest(yogurtId, new BigDecimal("1"), null, 10),
                                                new ProposedWeekMenuProductRequest(almondsId, new BigDecimal("2"), null, 20)
                                        )
                                ),
                                new ProposedWeekMenuSectionRequest(
                                        snackDayPartId,
                                        List.of(new ProposedWeekMenuProductRequest(yogurtId, new BigDecimal("1"), null, 10))
                                )
                        )
                )),
                ProposedWeekMenuResponse.class
        );

        assertThat(updatedMenu.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updatedMenu.getBody()).isNotNull();
        assertThat(updatedMenu.getBody().days()).hasSize(1);
        assertThat(updatedMenu.getBody().days().getFirst().sections()).hasSize(2);
        assertThat(updatedMenu.getBody().days().getFirst().sections().getFirst().products())
                .extracting(ProposedWeekMenuProductResponse::sortOrder)
                .containsExactly(10, 20);
    }

    @Test
    void proposedWeekMenuShouldAcceptTwoWeekRangeAndRejectLongerRanges() {
        String proposedMenusUrl = "http://localhost:" + port + "/api/v1/planning";

        ResponseEntity<ProposedWeekMenuResponse> acceptedMenu = postAuthorized(
                proposedMenusUrl,
                new CreateProposedWeekMenuRequest(LocalDate.of(2026, 6, 15), LocalDate.of(2026, 6, 30)),
                ProposedWeekMenuResponse.class
        );

        assertThat(acceptedMenu.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(acceptedMenu.getBody()).isNotNull();
        assertThat(acceptedMenu.getBody().startDate()).isEqualTo(LocalDate.of(2026, 6, 15));
        assertThat(acceptedMenu.getBody().endDate()).isEqualTo(LocalDate.of(2026, 6, 30));

        ResponseEntity<String> rejectedMenu = postAuthorized(
                proposedMenusUrl,
                new CreateProposedWeekMenuRequest(LocalDate.of(2026, 6, 15), LocalDate.of(2026, 7, 1)),
                String.class
        );

        assertThat(rejectedMenu.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(rejectedMenu.getBody()).contains("cannot span more than 16 days");
    }

    @Test
    void stockEndpointsShouldCreateAdjustAndFilterStock() {
        String productsUrl = "http://localhost:" + port + "/api/v1/products";
        String stockUrl = "http://localhost:" + port + "/api/v1/stock";
        Long appleId = createProduct(productsUrl, "Stock Apple", "Fresh apple", "52", "14", "0.3", "0.2");
        Long bananaId = createProduct(productsUrl, "Stock Banana", "Fresh banana", "89", "23", "1.1", "0.3");

        ResponseEntity<StockEntryResponse> createdAppleStock = postAuthorized(
                productsUrl + "/" + appleId + "/stock",
                new CreateStockEntryRequest(new BigDecimal("5.5"), new BigDecimal("4.99"), LocalDate.of(2026, 6, 14), LocalDate.of(2026, 6, 10)),
                StockEntryResponse.class
        );
        assertThat(createdAppleStock.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createdAppleStock.getBody()).isNotNull();
        assertThat(createdAppleStock.getBody().productId()).isEqualTo(appleId);
        assertThat(createdAppleStock.getBody().price()).isEqualByComparingTo("4.99");

        ResponseEntity<StockEntryResponse> createdBananaStock = postAuthorized(
                productsUrl + "/" + bananaId + "/stock",
                new CreateStockEntryRequest(new BigDecimal("4"), new BigDecimal("3.79"), LocalDate.of(2026, 6, 12), LocalDate.of(2026, 6, 9)),
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
        assertThat(allStock.getBody()[0].price()).isEqualByComparingTo("3.79");
        assertThat(allStock.getBody()[1].quantity()).isEqualByComparingTo("5.0");
        assertThat(allStock.getBody()[1].price()).isEqualByComparingTo("4.99");

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
        assertThat(appleStock.getBody()[0].price()).isEqualByComparingTo("4.99");

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
    void stockEntryUpdateShouldReplaceEditableFieldsAndValidatePayloads() {
        String productsUrl = "http://localhost:" + port + "/api/v1/products";
        String stockUrl = "http://localhost:" + port + "/api/v1/stock";
        Long appleId = createProduct(productsUrl, "Update Apple", "Fresh apple", "52", "14", "0.3", "0.2");

        ResponseEntity<StockEntryResponse> createdStock = postAuthorized(
                productsUrl + "/" + appleId + "/stock",
                new CreateStockEntryRequest(new BigDecimal("5"), new BigDecimal("4.99"), LocalDate.of(2026, 6, 14), LocalDate.of(2026, 6, 10)),
                StockEntryResponse.class
        );
        assertThat(createdStock.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createdStock.getBody()).isNotNull();
        Long stockEntryId = createdStock.getBody().id();

        ResponseEntity<StockEntryResponse> updatedStock = restTemplate.exchange(
                stockUrl + "/" + stockEntryId,
                HttpMethod.PUT,
                authorizedEntity(new UpdateStockEntryRequest(
                        new BigDecimal("7.25"),
                        new BigDecimal("5.49"),
                        LocalDate.of(2026, 6, 25),
                        LocalDate.of(2026, 6, 11)
                )),
                StockEntryResponse.class
        );
        assertThat(updatedStock.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updatedStock.getBody()).isNotNull();
        assertThat(updatedStock.getBody().id()).isEqualTo(stockEntryId);
        assertThat(updatedStock.getBody().productId()).isEqualTo(appleId);
        assertThat(updatedStock.getBody().quantity()).isEqualByComparingTo("7.25");
        assertThat(updatedStock.getBody().price()).isEqualByComparingTo("5.49");
        assertThat(updatedStock.getBody().expirationDate()).isEqualTo(LocalDate.of(2026, 6, 25));
        assertThat(updatedStock.getBody().entryDate()).isEqualTo(LocalDate.of(2026, 6, 11));

        ResponseEntity<String> invalidQuantity = restTemplate.exchange(
                stockUrl + "/" + stockEntryId,
                HttpMethod.PUT,
                authorizedEntity(new UpdateStockEntryRequest(
                        BigDecimal.ZERO,
                        new BigDecimal("5.49"),
                        LocalDate.of(2026, 6, 25),
                        LocalDate.of(2026, 6, 11)
                )),
                String.class
        );
        assertThat(invalidQuantity.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(invalidQuantity.getBody()).contains("quantity");

        ResponseEntity<String> invalidPrice = restTemplate.exchange(
                stockUrl + "/" + stockEntryId,
                HttpMethod.PUT,
                authorizedEntity(new UpdateStockEntryRequest(
                        new BigDecimal("7.25"),
                        new BigDecimal("-1"),
                        LocalDate.of(2026, 6, 25),
                        LocalDate.of(2026, 6, 11)
                )),
                String.class
        );
        assertThat(invalidPrice.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(invalidPrice.getBody()).contains("price");

        ResponseEntity<String> missingEntryDate = restTemplate.exchange(
                stockUrl + "/" + stockEntryId,
                HttpMethod.PUT,
                authorizedEntity(new UpdateStockEntryRequest(
                        new BigDecimal("7.25"),
                        new BigDecimal("5.49"),
                        LocalDate.of(2026, 6, 25),
                        null
                )),
                String.class
        );
        assertThat(missingEntryDate.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(missingEntryDate.getBody()).contains("entryDate");

        ResponseEntity<String> notFound = restTemplate.exchange(
                stockUrl + "/999999999",
                HttpMethod.PUT,
                authorizedEntity(new UpdateStockEntryRequest(
                        new BigDecimal("7.25"),
                        new BigDecimal("5.49"),
                        LocalDate.of(2026, 6, 25),
                        LocalDate.of(2026, 6, 11)
                )),
                String.class
        );
        assertThat(notFound.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        assertThat(notFound.getBody()).contains("Stock entry not found");
    }

    @Test
    void statsEndpointsShouldAggregateServerSideAndRefreshAfterMutations() {
        String productsUrl = "http://localhost:" + port + "/api/v1/products";
        String recipesUrl = "http://localhost:" + port + "/api/v1/recipes";
        String stockUrl = "http://localhost:" + port + "/api/v1/stock";

        Long chickenId = createProduct(productsUrl, "Stats Chicken", "Chicken breast", "7000", "0", "31", "3.6");
        Long riceId = createProduct(productsUrl, "Stats Rice", "Rice", "6000", "28", "2.7", "0.3");
        Long beansId = createProduct(productsUrl, "Stats Beans", "Beans", "5000", "22", "8", "1.2");

        ResponseEntity<StockEntryResponse> chickenStock = postAuthorized(
                productsUrl + "/" + chickenId + "/stock",
                new CreateStockEntryRequest(new BigDecimal("4"), new BigDecimal("5.25"), LocalDate.of(1900, 1, 1), LocalDate.of(2026, 6, 10)),
                StockEntryResponse.class
        );
        ResponseEntity<StockEntryResponse> riceStock = postAuthorized(
                productsUrl + "/" + riceId + "/stock",
                new CreateStockEntryRequest(new BigDecimal("2"), new BigDecimal("2.10"), null, LocalDate.of(2026, 6, 11)),
                StockEntryResponse.class
        );
        ResponseEntity<StockEntryResponse> beansStock = postAuthorized(
                productsUrl + "/" + beansId + "/stock",
                new CreateStockEntryRequest(new BigDecimal("1"), new BigDecimal("1.20"), null, LocalDate.of(2026, 6, 12)),
                StockEntryResponse.class
        );

        assertThat(chickenStock.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(riceStock.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(beansStock.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<RecipeResponse> createdRecipe = postAuthorized(
                recipesUrl,
                new CreateRecipeRequest(
                        "Stats curry",
                        "Stats curry description",
                        "Cook everything together.",
                        List.of(
                                new RecipeIngredientAssignmentRequest(chickenId, new BigDecimal("200")),
                                new RecipeIngredientAssignmentRequest(riceId, new BigDecimal("150"))
                        )
                ),
                RecipeResponse.class
        );
        assertThat(createdRecipe.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<ProductStatsResponse> initialProductStats = getAuthorized(productsUrl + "/stats", ProductStatsResponse.class);
        assertThat(initialProductStats.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(initialProductStats.getBody()).isNotNull();
        BigDecimal initialTotalQuantity = initialProductStats.getBody().stock().totalQuantity();
        long initialBatchCount = initialProductStats.getBody().stock().batchCount();
        assertThat(initialProductStats.getBody().caloriesTop().productName()).isEqualTo("Stats Chicken");
        assertThat(initialProductStats.getBody().stock().totalQuantity()).isGreaterThan(BigDecimal.ZERO);
        assertThat(initialProductStats.getBody().stock().batchCount()).isGreaterThan(0);
        assertThat(initialProductStats.getBody().earliestExpiration().productName()).isEqualTo("Stats Chicken");
        assertThat(initialProductStats.getBody().summaries())
                .anySatisfy(summary -> assertThat(summary.productName()).isEqualTo("Stats Chicken"));
        assertThat(initialProductStats.getBody().summaries())
                .anySatisfy(summary -> assertThat(summary.productName()).isEqualTo("Stats Rice"));
        assertThat(initialProductStats.getBody().summaries())
                .anySatisfy(summary -> assertThat(summary.productName()).isEqualTo("Stats Beans"));
        assertThat(initialProductStats.getBody().summaries())
                .filteredOn(summary -> summary.productName().equals("Stats Rice"))
                .singleElement()
                .satisfies(summary -> {
                    assertThat(summary.totalQuantity()).isEqualByComparingTo("2.00");
                    assertThat(summary.nextExpirationMessage()).isEqualTo("Sin caducidad");
                });

        ResponseEntity<RecipeStatsResponse> initialRecipeStats = getAuthorized(recipesUrl + "/stats", RecipeStatsResponse.class);
        assertThat(initialRecipeStats.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(initialRecipeStats.getBody()).isNotNull();
        long initialActiveRecipes = initialRecipeStats.getBody().activeRecipes();
        long initialTotalIngredients = initialRecipeStats.getBody().totalIngredients();
        BigDecimal initialAverageCalories = initialRecipeStats.getBody().averageCalories();
        assertThat(initialActiveRecipes).isGreaterThan(0);
        assertThat(initialTotalIngredients).isGreaterThan(0);
        assertThat(initialAverageCalories).isNotNull();
        assertThat(initialRecipeStats.getBody().recipesWithDerivedProduct()).isZero();

        ResponseEntity<StockEntryResponse> addedChicken = postAuthorized(
                stockUrl + "/" + chickenStock.getBody().id() + "/add",
                new AdjustStockQuantityRequest(new BigDecimal("1.5")),
                StockEntryResponse.class
        );
        assertThat(addedChicken.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<String> removedChicken = postAuthorized(
                stockUrl + "/" + chickenStock.getBody().id() + "/remove",
                new AdjustStockQuantityRequest(new BigDecimal("5.5")),
                String.class
        );
        assertThat(removedChicken.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<ProductStatsResponse> refreshedProductStats = getAuthorized(productsUrl + "/stats", ProductStatsResponse.class);
        assertThat(refreshedProductStats.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(refreshedProductStats.getBody()).isNotNull();
        assertThat(refreshedProductStats.getBody().stock().totalQuantity()).isEqualByComparingTo(initialTotalQuantity.add(new BigDecimal("-4.00")));
        assertThat(refreshedProductStats.getBody().stock().batchCount()).isEqualTo(initialBatchCount - 1);
        assertThat(refreshedProductStats.getBody().earliestExpiration().productName()).isNotEqualTo("Stats Chicken");
        assertThat(refreshedProductStats.getBody().summaries())
                .filteredOn(summary -> summary.productName().equals("Stats Chicken"))
                .singleElement()
                .satisfies(summary -> {
                    assertThat(summary.totalQuantity()).isEqualByComparingTo("0.00");
                    assertThat(summary.nextExpirationMessage()).isEqualTo("Sin lotes");
                });

        ResponseEntity<ProductResponse> updatedBeans = restTemplate.exchange(
                productsUrl + "/" + beansId,
                HttpMethod.PUT,
                authorizedEntity(new UpdateProductRequest(
                        "Stats Beans",
                        "Beans updated",
                        new BigDecimal("100"),
                        new BigDecimal("8000"),
                        new BigDecimal("22"),
                        new BigDecimal("8"),
                        new BigDecimal("1.2"),
                        new BigDecimal("1.49")
                )),
                ProductResponse.class
        );
        assertThat(updatedBeans.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updatedBeans.getBody()).isNotNull();
        assertThat(updatedBeans.getBody().defaultPrice()).isEqualByComparingTo("1.49");

        ResponseEntity<ProductStatsResponse> updatedProductStats = getAuthorized(productsUrl + "/stats", ProductStatsResponse.class);
        assertThat(updatedProductStats.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updatedProductStats.getBody()).isNotNull();
        assertThat(updatedProductStats.getBody().caloriesTop().productName()).isEqualTo("Stats Beans");
        assertThat(updatedProductStats.getBody().caloriesTop().value()).isEqualByComparingTo("8000.00");

        ResponseEntity<RecipeResponse> updatedRecipe = restTemplate.exchange(
                recipesUrl + "/" + createdRecipe.getBody().id(),
                HttpMethod.PUT,
                authorizedEntity(new UpdateRecipeRequest(
                        "Stats curry",
                        "Stats curry updated",
                        "Cook longer.",
                        List.of(
                                new RecipeIngredientAssignmentRequest(chickenId, new BigDecimal("250")),
                                new RecipeIngredientAssignmentRequest(riceId, new BigDecimal("150")),
                                new RecipeIngredientAssignmentRequest(beansId, new BigDecimal("50"))
                        )
                )),
                RecipeResponse.class
        );
        assertThat(updatedRecipe.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<RecipeStatsResponse> refreshedRecipeStats = getAuthorized(recipesUrl + "/stats", RecipeStatsResponse.class);
        assertThat(refreshedRecipeStats.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(refreshedRecipeStats.getBody()).isNotNull();
        assertThat(refreshedRecipeStats.getBody().activeRecipes()).isEqualTo(initialActiveRecipes);
        assertThat(refreshedRecipeStats.getBody().averageCalories()).isGreaterThan(initialAverageCalories);
        assertThat(refreshedRecipeStats.getBody().totalIngredients()).isEqualTo(initialTotalIngredients + 1);
        assertThat(refreshedRecipeStats.getBody().recipesWithDerivedProduct()).isZero();
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
        assertThat(created.getBody().photo()).startsWith("/api/v1/media/");
        assertThat(created.getBody().photo()).contains("expiresAt=");
        assertThat(created.getBody().photo()).contains("signature=");

        ResponseEntity<byte[]> mediaResponse = restTemplate.exchange(
                "http://localhost:" + port + created.getBody().photo(),
                HttpMethod.GET,
                HttpEntity.EMPTY,
                byte[].class
        );

        assertThat(mediaResponse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(mediaResponse.getHeaders().getContentType()).isEqualTo(org.springframework.http.MediaType.IMAGE_JPEG);
        assertThat(mediaResponse.getBody()).isNotNull();
        assertThat(mediaResponse.getBody().length).isLessThanOrEqualTo(153600);
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
        assertThat(created.getBody().photo()).startsWith("/api/v1/media/");
        assertThat(created.getBody().photo()).contains("expiresAt=");
        assertThat(created.getBody().photo()).contains("signature=");
    }

    @Test
    void nutritionalRulesShouldEvaluateFlexiblePlanningAndCreatedMenuByPlannedDay() {
        String baseUrl = "http://localhost:" + port + "/api/v1";
        SaveNutritionalRulesRequest rules = new SaveNutritionalRulesRequest(
                new NutrientRuleRequest(new BigDecimal("100"), new BigDecimal("200")),
                new NutrientRuleRequest(null, new BigDecimal("20")),
                new NutrientRuleRequest(new BigDecimal("10"), null),
                new NutrientRuleRequest(null, null)
        );

        ResponseEntity<NutritionalRulesResponse> savedRules = restTemplate.exchange(
                baseUrl + "/nutritional-rules",
                HttpMethod.PUT,
                authorizedEntity(rules),
                NutritionalRulesResponse.class
        );
        assertThat(savedRules.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(savedRules.getBody().calories().minimum()).isEqualByComparingTo("100.00");

        Long productId = createProduct(baseUrl + "/products", "Rules product", "Nutrition rules", "150", "25", "5", "2");
        Long dayPartId = createDayPart(baseUrl + "/planning/day-parts", "Rules lunch", "Rule evaluation meal", 991);
        LocalDate startDate = LocalDate.of(2026, 7, 1);
        ResponseEntity<ProposedWeekMenuResponse> planning = postAuthorized(
                baseUrl + "/planning",
                new CreateProposedWeekMenuRequest(startDate, startDate.plusDays(13)),
                ProposedWeekMenuResponse.class
        );

        ResponseEntity<ProposedWeekMenuResponse> planned = restTemplate.exchange(
                baseUrl + "/planning/" + planning.getBody().id() + "/days",
                HttpMethod.PUT,
                authorizedEntity(new UpsertProposedWeekMenuDayRequest(
                        startDate,
                        List.of(new ProposedWeekMenuSectionRequest(
                                dayPartId,
                                List.of(new ProposedWeekMenuProductRequest(productId, BigDecimal.ONE, new BigDecimal("100"), 1))
                        ))
                )),
                ProposedWeekMenuResponse.class
        );

        assertThat(planned.getBody().nutritionalRules().plannedDays()).isOne();
        assertThat(planned.getBody().nutritionalRules().calories().status()).isEqualTo(NutritionalRuleStatus.WITHIN_RANGE);
        assertThat(planned.getBody().nutritionalRules().carbohydrates().status()).isEqualTo(NutritionalRuleStatus.ABOVE_MAXIMUM);
        assertThat(planned.getBody().nutritionalRules().proteins().status()).isEqualTo(NutritionalRuleStatus.BELOW_MINIMUM);
        assertThat(planned.getBody().nutritionalRules().fats().status()).isEqualTo(NutritionalRuleStatus.NOT_CONFIGURED);

        ResponseEntity<CurrentWeekMenuResponse> menu = postAuthorized(
                baseUrl + "/planning/" + planning.getBody().id() + "/menu",
                new EstablishProposedWeekMenuRequest(authenticatedUserId()),
                CurrentWeekMenuResponse.class
        );
        assertThat(menu.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(menu.getBody().planningId()).isEqualTo(planning.getBody().id());
        assertThat(menu.getBody().nutritionalRules()).isEqualTo(planned.getBody().nutritionalRules());
    }

    @Test
    void userConfirmedStockAllocationShouldBeListedAndUndoShouldRestoreStockAndMoney() {
        String baseUrl = "http://localhost:" + port + "/api/v1";
        String suffix = Long.toString(System.nanoTime());
        Long productId = createProduct(
                baseUrl + "/products", "Allocation product " + suffix,
                "Product used to verify explicit menu stock allocation", "100", "10", "5", "2"
        );
        Long dayPartId = createDayPart(
                baseUrl + "/planning/day-parts", "Allocation meal " + suffix,
                "Meal used to verify explicit stock allocation", 1200
        );
        ResponseEntity<StockEntryResponse> expiringFirst = postAuthorized(
                baseUrl + "/products/" + productId + "/stock",
                new CreateStockEntryRequest(
                        new BigDecimal("2.00"), new BigDecimal("1.00"),
                        LocalDate.of(2030, 1, 10), LocalDate.of(2029, 12, 1)
                ),
                StockEntryResponse.class
        );
        ResponseEntity<StockEntryResponse> userSelected = postAuthorized(
                baseUrl + "/products/" + productId + "/stock",
                new CreateStockEntryRequest(
                        new BigDecimal("2.00"), new BigDecimal("2.00"),
                        LocalDate.of(2030, 1, 20), LocalDate.of(2029, 12, 2)
                ),
                StockEntryResponse.class
        );
        ResponseEntity<ProposedWeekMenuResponse> planning = postAuthorized(
                baseUrl + "/planning",
                new CreateProposedWeekMenuRequest(LocalDate.of(2030, 1, 1), LocalDate.of(2030, 1, 2)),
                ProposedWeekMenuResponse.class
        );
        restTemplate.exchange(
                baseUrl + "/planning/" + planning.getBody().id() + "/days",
                HttpMethod.PUT,
                authorizedEntity(new UpsertProposedWeekMenuDayRequest(
                        LocalDate.of(2030, 1, 1),
                        List.of(new ProposedWeekMenuSectionRequest(
                                dayPartId,
                                List.of(new ProposedWeekMenuProductRequest(
                                        productId, new BigDecimal("2.00"), null, 10
                                ))
                        ))
                )),
                ProposedWeekMenuResponse.class
        );
        BigDecimal balanceBefore = getAuthorized(
                baseUrl + "/users/" + authenticatedUserId() + "/money-box",
                UserMoneyBoxResponse.class
        ).getBody().balance();

        ResponseEntity<CurrentWeekMenuResponse> established = postAuthorized(
                baseUrl + "/planning/" + planning.getBody().id() + "/menu",
                new EstablishProposedWeekMenuRequest(
                        authenticatedUserId(),
                        List.of(new MenuStockAllocationRequest(
                                userSelected.getBody().id(), new BigDecimal("2.00")
                        ))
                ),
                CurrentWeekMenuResponse.class
        );

        assertThat(established.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(established.getBody().usedStock()).singleElement().satisfies(used -> {
            assertThat(used.stockEntryId()).isEqualTo(userSelected.getBody().id());
            assertThat(used.usedUnits()).isEqualByComparingTo("2.00");
        });
        assertThat(getAuthorized(baseUrl + "/menus", CurrentWeekMenuResponse[].class).getBody())
                .extracting(CurrentWeekMenuResponse::id)
                .contains(established.getBody().id());
        ResponseEntity<StockEntryResponse[]> appliedStock = getAuthorized(
                baseUrl + "/stock?productIds=" + productId,
                StockEntryResponse[].class
        );
        assertThat(appliedStock.getBody())
                .filteredOn(stock -> stock.id().equals(expiringFirst.getBody().id()))
                .singleElement().satisfies(stock -> assertThat(stock.quantity()).isEqualByComparingTo("2.00"));
        assertThat(appliedStock.getBody())
                .noneMatch(stock -> stock.id().equals(userSelected.getBody().id()));
        assertThat(getAuthorized(
                baseUrl + "/users/" + authenticatedUserId() + "/money-box",
                UserMoneyBoxResponse.class
        ).getBody().balance()).isEqualByComparingTo(balanceBefore.subtract(new BigDecimal("4.00")));

        ResponseEntity<Void> undone = restTemplate.exchange(
                baseUrl + "/menus/" + established.getBody().id(),
                HttpMethod.DELETE,
                authorizedEntity(null),
                Void.class
        );

        assertThat(undone.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);
        assertThat(getAuthorized(
                baseUrl + "/stock?productIds=" + productId,
                StockEntryResponse[].class
        ).getBody())
                .filteredOn(stock -> stock.id().equals(userSelected.getBody().id()))
                .singleElement().satisfies(stock -> assertThat(stock.quantity()).isEqualByComparingTo("2.00"));
        assertThat(getAuthorized(
                baseUrl + "/users/" + authenticatedUserId() + "/money-box",
                UserMoneyBoxResponse.class
        ).getBody().balance()).isEqualByComparingTo(balanceBefore);
        assertThat(getAuthorized(
                baseUrl + "/menus/" + established.getBody().id(),
                String.class
        ).getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
        ResponseEntity<CurrentWeekMenuResponse> reestablished = postAuthorized(
                baseUrl + "/planning/" + planning.getBody().id() + "/menu",
                new EstablishProposedWeekMenuRequest(authenticatedUserId()),
                CurrentWeekMenuResponse.class
        );
        assertThat(reestablished.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(reestablished.getBody().planningId()).isEqualTo(planning.getBody().id());
    }

    private Long createProduct(String productsUrl, String name, String description, String calories, String carbohydrates, String proteins, String fats) {
        return createProduct(productsUrl, name, description, calories, carbohydrates, proteins, fats, "100");
    }

    private Long createProduct(String productsUrl, String name, String description, String calories, String carbohydrates, String proteins, String fats, String gramsPerUnit) {
        return createProduct(productsUrl, name, description, calories, carbohydrates, proteins, fats, gramsPerUnit, List.of());
    }

    private Long createProduct(String productsUrl, String name, String description, String calories, String carbohydrates, String proteins, String fats, String gramsPerUnit, List<Long> supermarketIds) {
        ResponseEntity<ProductResponse> created = postAuthorized(
                productsUrl,
                new CreateProductRequest(
                        name,
                        description,
                        new BigDecimal(gramsPerUnit),
                        new BigDecimal(calories),
                        new BigDecimal(carbohydrates),
                        new BigDecimal(proteins),
                        new BigDecimal(fats),
                        null,
                        supermarketIds
                ),
                ProductResponse.class
        );
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).isNotNull();
        return created.getBody().id();
    }

    private Long createSupermarket(String supermarketsUrl, String name) {
        ResponseEntity<SupermarketResponse> created = postAuthorized(
                supermarketsUrl, new SupermarketRequest(name), SupermarketResponse.class
        );
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).isNotNull();
        return created.getBody().id();
    }

    private Long createDayPart(String dayPartsUrl, String name, String description, int sortOrder) {
        ResponseEntity<ProposedWeekMenuDayPartResponse> created = postAuthorized(
                dayPartsUrl,
                new ProposedWeekMenuDayPartRequest(name, description, sortOrder),
                ProposedWeekMenuDayPartResponse.class
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

    private <T> ResponseEntity<T> putAuthorized(String url, Object body, Class<T> responseType) {
        return restTemplate.exchange(url, HttpMethod.PUT, authorizedEntity(body), responseType);
    }

    private <T> ResponseEntity<T> deleteAuthorized(String url, Class<T> responseType) {
        return restTemplate.exchange(url, HttpMethod.DELETE, authorizedEntity(null), responseType);
    }

    private void assertOpenApiGroup(String group, String... expectedPaths) {
        ResponseEntity<String> response = restTemplate.getForEntity("http://localhost:" + port + "/v3/api-docs/" + group, String.class);

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(response.getBody()).isNotNull();
        for (String expectedPath : expectedPaths) {
            assertThat(response.getBody()).contains(expectedPath);
        }
    }

    private HttpEntity<Object> authorizedEntity(Object body) {
        HttpHeaders headers = authHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        return new HttpEntity<>(body, headers);
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
            authenticatedUserId = registered.getBody().userId();
        }
        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(accessToken);
        return headers;
    }

    private Long authenticatedUserId() {
        authHeaders();
        return authenticatedUserId;
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

    private static PostgreSQLContainer<?> postgres(String imageName) {
        PostgreSQLContainer<?> container = new PostgreSQLContainer<>(imageName)
                .withDatabaseName("foodhelper")
                .withUsername("foodhelper")
                .withPassword("foodhelper")
                .withInitScript("db/test-init.sql");

        String fixedPort = System.getenv("TESTCONTAINERS_POSTGRES_HOST_PORT");
        if (fixedPort != null && !fixedPort.isBlank()) {
            container.setPortBindings(List.of(fixedPort + ":5432"));
        }

        return container;
    }

}
