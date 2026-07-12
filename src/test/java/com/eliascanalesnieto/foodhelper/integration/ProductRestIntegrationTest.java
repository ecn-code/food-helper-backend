package com.eliascanalesnieto.foodhelper.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.tuple;

import com.eliascanalesnieto.foodhelper.presentation.PhotoUploadRequest;
import com.eliascanalesnieto.foodhelper.infra.NutritionalValuesCrudRepository;
import com.eliascanalesnieto.foodhelper.infra.NutritionalValuesEntity;
import java.util.Arrays;
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
import com.eliascanalesnieto.foodhelper.presentation.NutritionalRulesPeriodRequest;
import com.eliascanalesnieto.foodhelper.presentation.NutritionalRulesResponse;
import com.eliascanalesnieto.foodhelper.presentation.SaveNutritionalRulesRequest;
import com.eliascanalesnieto.foodhelper.presentation.CreateUserMoneyMovementRequest;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuResponse;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuCloseSummaryResponse;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuRecipeProductionResponse;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuRangeStatsResponse;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuShoppingListItemResponse;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuStockItemRequest;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuStockItemResponse;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuStatsResponse;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuUsedStockResponse;
import com.eliascanalesnieto.foodhelper.presentation.CreateMenuStockMovementRequest;
import com.eliascanalesnieto.foodhelper.presentation.CreateMenuStockTransferRequest;
import com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuPeriodStatsResponse;
import com.eliascanalesnieto.foodhelper.presentation.MenuPageResponse;
import com.eliascanalesnieto.foodhelper.presentation.ProductStatsResponse;
import com.eliascanalesnieto.foodhelper.presentation.ProductResponse;
import com.eliascanalesnieto.foodhelper.presentation.ProductPageResponse;
import com.eliascanalesnieto.foodhelper.presentation.AdjustStockQuantityRequest;
import com.eliascanalesnieto.foodhelper.presentation.ProposedWeekMenuDayPartRequest;
import com.eliascanalesnieto.foodhelper.presentation.ProposedWeekMenuDayPartResponse;
import com.eliascanalesnieto.foodhelper.presentation.ProposedWeekMenuProductRequest;
import com.eliascanalesnieto.foodhelper.presentation.ProposedWeekMenuRecipeProductionRequest;
import com.eliascanalesnieto.foodhelper.presentation.ProposedWeekMenuProductResponse;
import com.eliascanalesnieto.foodhelper.presentation.ProposedWeekMenuResponse;
import com.eliascanalesnieto.foodhelper.presentation.ProposedWeekMenuStockSummaryResponse;
import com.eliascanalesnieto.foodhelper.presentation.ProposedWeekMenuSectionRequest;
import com.eliascanalesnieto.foodhelper.presentation.MenuStockMovementResponse;
import com.eliascanalesnieto.foodhelper.presentation.UpdateCurrentWeekMenuPayerRequest;
import com.eliascanalesnieto.foodhelper.presentation.UpdateCurrentWeekMenuStockRequest;
import com.eliascanalesnieto.foodhelper.presentation.RecipeIngredientAssignmentRequest;
import com.eliascanalesnieto.foodhelper.presentation.RecipeStatsResponse;
import com.eliascanalesnieto.foodhelper.presentation.RecipeResponse;
import com.eliascanalesnieto.foodhelper.presentation.RecipePageResponse;
import com.eliascanalesnieto.foodhelper.presentation.RecipeDerivedProductResponse;
import com.eliascanalesnieto.foodhelper.presentation.RegisterRequest;
import com.eliascanalesnieto.foodhelper.presentation.CreateStockEntryRequest;
import com.eliascanalesnieto.foodhelper.presentation.StockEntryResponse;
import com.eliascanalesnieto.foodhelper.presentation.StockMovementPageResponse;
import com.eliascanalesnieto.foodhelper.presentation.StockMovementResponse;
import com.eliascanalesnieto.foodhelper.presentation.StockReconciliationResponse;
import com.eliascanalesnieto.foodhelper.presentation.SupermarketRequest;
import com.eliascanalesnieto.foodhelper.presentation.SupermarketResponse;
import com.eliascanalesnieto.foodhelper.presentation.ProductStatsSummaryResponse;
import com.eliascanalesnieto.foodhelper.presentation.UpdateStockEntryRequest;
import com.eliascanalesnieto.foodhelper.presentation.EstablishProposedWeekMenuRequest;
import com.eliascanalesnieto.foodhelper.presentation.MenuStockAllocationRequest;
import com.eliascanalesnieto.foodhelper.presentation.PlanningSummaryResponse;
import com.eliascanalesnieto.foodhelper.presentation.CouponResponse;
import com.eliascanalesnieto.foodhelper.presentation.PlanningCouponResponse;
import com.eliascanalesnieto.foodhelper.presentation.PlanningCouponUnavailabilityReason;
import com.eliascanalesnieto.foodhelper.presentation.ValidateProposedWeekMenuCouponsRequest;
import com.eliascanalesnieto.foodhelper.presentation.UpsertProposedWeekMenuDayRequest;
import com.eliascanalesnieto.foodhelper.presentation.UpdateRecipeRequest;
import com.eliascanalesnieto.foodhelper.presentation.UpdateProductRequest;
import com.eliascanalesnieto.foodhelper.presentation.UserMoneyBoxResponse;
import com.eliascanalesnieto.foodhelper.presentation.UserMoneyMovementResponse;
import com.eliascanalesnieto.foodhelper.presentation.CreateUserWeightRequest;
import com.eliascanalesnieto.foodhelper.presentation.UserMenuHistoryEntryResponse;
import com.eliascanalesnieto.foodhelper.presentation.UserMenuHistoryResponse;
import com.eliascanalesnieto.foodhelper.presentation.UserResponse;
import com.eliascanalesnieto.foodhelper.presentation.UserWeightResponse;
import com.eliascanalesnieto.foodhelper.presentation.UserWeightStatsResponse;
import com.eliascanalesnieto.foodhelper.presentation.UpdateUserWeightRequest;
import com.eliascanalesnieto.foodhelper.domain.QuantityType;
import java.math.BigDecimal;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.time.LocalDate;
import java.time.Instant;
import java.time.temporal.ChronoUnit;
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
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;
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

    @Autowired
    private ObjectMapper objectMapper;

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
    void getByIdShouldReturnDerivedProductRecipeAndComposition() {
        String productsUrl = "http://localhost:" + port + "/api/v1/products";
        String recipesUrl = "http://localhost:" + port + "/api/v1/recipes";

        Long ingredientId = createProduct(productsUrl, "Derived Curry Ingredient", "Ingredient", "165", "0", "31", "3.6");
        ResponseEntity<RecipeResponse> createdRecipe = postAuthorized(
                recipesUrl,
                new CreateRecipeRequest(
                        "Derived Curry",
                        "Creamy curry",
                        "Cook gently.",
                        List.of(new RecipeIngredientAssignmentRequest(ingredientId, new BigDecimal("200"), QuantityType.GRAMS))
                ),
                RecipeResponse.class
        );
        assertThat(createdRecipe.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(createdRecipe.getBody()).isNotNull();

        Long recipeId = createdRecipe.getBody().id();
        ResponseEntity<RecipeDerivedProductResponse> derivedProduct = postAuthorized(
                recipesUrl + "/" + recipeId + "/derived-product",
                new CreateRecipeDerivedProductRequest("Derived Curry Base", new BigDecimal("4")),
                RecipeDerivedProductResponse.class
        );
        assertThat(derivedProduct.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(derivedProduct.getBody()).isNotNull();

        ResponseEntity<ProductResponse> product = getAuthorized(
                productsUrl + "/" + derivedProduct.getBody().productId(),
                ProductResponse.class
        );
        assertThat(product.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(product.getBody()).isNotNull();
        assertThat(product.getBody().derivedProduct()).isNotNull();
        assertThat(product.getBody().derivedProduct().recipeId()).isEqualTo(recipeId);
        assertThat(product.getBody().derivedProduct().ingredients()).hasSize(1);
        assertThat(product.getBody().derivedProduct().ingredients().getFirst().productId()).isEqualTo(ingredientId);
        assertThat(product.getBody().derivedProduct().ingredients().getFirst().productName()).isEqualTo("Derived Curry Ingredient");
        assertThat(product.getBody().derivedProduct().ingredients().getFirst().quantity()).isEqualByComparingTo("50.00");
        assertThat(product.getBody().derivedProduct().ingredients().getFirst().quantityType()).isEqualTo(QuantityType.GRAMS);
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
        String suffix = Long.toString(System.nanoTime());
        createProduct(baseUrl, "List Banana " + suffix, "Fresh banana " + suffix, "89", "23", "1.1", "0.3");
        createProduct(baseUrl, "List Apple " + suffix, "Fresh apple " + suffix, "52", "14", "0.3", "0.2");

        ResponseEntity<ProductPageResponse> after = getAuthorized(baseUrl + "?search=" + suffix + "&page=0&size=2", ProductPageResponse.class);

        assertThat(after.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(after.getBody()).isNotNull();
        assertThat(after.getBody().page()).isEqualTo(0);
        assertThat(after.getBody().size()).isEqualTo(2);
        assertThat(after.getBody().items()).extracting(ProductResponse::name)
                .containsExactly(
                        "List Apple " + suffix,
                        "List Banana " + suffix
                );
    }

    @Test
    void listProductsShouldSupportSearchAndNutritionalFilters() {
        String baseUrl = "http://localhost:" + port + "/api/v1/products";
        String suffix = Long.toString(System.nanoTime());

        createProduct(baseUrl, "Filter Chicken " + suffix, "Searchable chicken " + suffix, "165", "0", "31", "3.6");
        createProduct(baseUrl, "Filter Banana " + suffix, "Searchable banana " + suffix, "89", "23", "1.1", "0.3");
        createProduct(baseUrl, "Filter Apple " + suffix, "Searchable apple " + suffix, "52", "14", "0.3", "0.2");

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
        Long chickenId = createProduct(productsUrl, "Recipe Chicken " + suffix, "Chicken breast", "165", "0", "31", "3.6");

        ResponseEntity<RecipeResponse> soup = postAuthorized(
                recipesUrl,
                new CreateRecipeRequest(
                        "Soup " + suffix,
                        "Simple soup " + suffix,
                        "Cook gently.",
                        List.of(new RecipeIngredientAssignmentRequest(chickenId, new BigDecimal("100"), QuantityType.GRAMS))
                ),
                RecipeResponse.class
        );

        assertThat(soup.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<RecipeResponse> created = postAuthorized(
                recipesUrl,
                new CreateRecipeRequest(
                        recipeName,
                        "Creamy curry " + suffix,
                        "Cook everything together.",
                        List.of(new RecipeIngredientAssignmentRequest(chickenId, new BigDecimal("200"), QuantityType.GRAMS))
                ),
                RecipeResponse.class
        );

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<RecipePageResponse> paged = getAuthorized(
                recipesUrl + "?search=" + suffix + "&page=0&size=2",
                RecipePageResponse.class
        );

        assertThat(paged.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(paged.getBody()).isNotNull();
        assertThat(paged.getBody().items()).extracting(RecipeResponse::name)
                .containsExactly(
                        recipeName,
                        "Soup " + suffix
                );
    }

    @Test
    void preflightFromLanOriginShouldReturnCorsHeaders() {
        String productsUrl = "http://localhost:" + port + "/api/v1/products";
        HttpHeaders headers = new HttpHeaders();
        headers.setOrigin("http://192.168.1.133");
        headers.add(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET");
        headers.add(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization,content-type");

        ResponseEntity<String> response = restTemplate.exchange(
                productsUrl,
                HttpMethod.OPTIONS,
                new HttpEntity<>(headers),
                String.class
        );

        assertThat(response.getStatusCode()).isEqualTo(HttpStatus.OK);
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
                        List.of(new RecipeIngredientAssignmentRequest(saffronId, new BigDecimal("100"), QuantityType.GRAMS))
                ),
                RecipeResponse.class
        ).getBody();
        postAuthorized(
                recipesUrl,
                new CreateRecipeRequest(
                        "Other recipe " + suffix,
                        "Unrelated description",
                        "Bake briefly.",
                        List.of(new RecipeIngredientAssignmentRequest(saffronId, new BigDecimal("10"), QuantityType.GRAMS))
                ),
                RecipeResponse.class
        );
        postAuthorized(
                recipesUrl + "/" + matching.id() + "/derived-product",
                new CreateRecipeDerivedProductRequest("Saffron base", new BigDecimal("5")),
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
        assertThat(response.getBody()).contains("/api/v1/stock/movements");
        assertThat(response.getBody()).contains("/api/v1/products/{productId}/stock");
        assertThat(response.getBody()).contains("/api/v1/products/{productId}/stock/reconciliation");
        assertThat(response.getBody()).contains("/api/v1/stock/{stockEntryId}/add");
        assertThat(response.getBody()).contains("/api/v1/stock/{stockEntryId}/remove");
        assertThat(response.getBody()).contains("StockMovementPageResponse");
        assertThat(response.getBody()).contains("StockMovementResponse");
        assertThat(response.getBody()).contains("StockReconciliationResponse");
        assertThat(response.getBody()).contains("defaultPrice");
        assertThat(response.getBody()).contains("\"price\"");
        assertThat(response.getBody()).contains("/api/v1/planning");
        assertThat(response.getBody()).contains("/api/v1/planning/{id}");
        assertThat(response.getBody()).contains("deletePlanning");
        assertThat(response.getBody()).contains("/api/v1/planning/{id}/days");
        assertThat(response.getBody()).contains("/api/v1/planning/{id}/menu");
        assertThat(response.getBody()).contains("Number of users covered by the planning");
        assertThat(response.getBody()).contains("/api/v1/planning/day-parts");
        assertThat(response.getBody()).contains("/api/v1/coupons");
        assertThat(response.getBody()).contains("CouponResponse");
        assertThat(response.getBody()).contains("/api/v1/planning/{id}/coupons");
        assertThat(response.getBody()).contains("PlanningCouponResponse");
        assertThat(response.getBody()).contains("PlanningCouponUnavailabilityReason");
        assertThat(response.getBody()).contains("PlanningRecipeProductionRequest");
        assertThat(response.getBody()).contains("PlanningRecipeProductionResponse");
        assertThat(response.getBody()).contains("unique within each section");
        assertThat(response.getBody()).contains("stockSummary");
        assertThat(response.getBody()).contains("PlanningStockSummaryResponse");
        assertThat(response.getBody()).contains("PlanningStockRequirementResponse");
        assertThat(response.getBody()).contains("/api/v1/nutritional-rules");
        assertThat(response.getBody()).contains("NutritionalRulesEvaluationResponse");
        assertThat(response.getBody()).contains("inclusive date range cannot span more than 16 calendar days");
        assertThat(response.getBody()).contains("/api/v1/users/{userId}/money-box");
        assertThat(response.getBody()).contains("/api/v1/users/{userId}/money-box/movements");
        assertThat(response.getBody()).contains("/api/v1/users");
        assertThat(response.getBody()).contains("/api/v1/users/{personId}/menu-history");
        assertThat(response.getBody()).contains("/api/v1/users/{personId}/menu-history/monthly");
        assertThat(response.getBody()).contains("/api/v1/users/{personId}/menu-history/annual");
        assertThat(response.getBody()).contains("/api/v1/users/{userId}/weights");
        assertThat(response.getBody()).contains("/api/v1/users/{userId}/weights/stats");
        assertThat(response.getBody()).contains("/api/v1/users/{userId}/weights/{weightId}");
        assertThat(response.getBody()).contains("UserWeightStatsResponse");
        assertThat(response.getBody()).contains("createdAt");
        assertThat(response.getBody()).contains("updatedAt");
        assertThat(response.getBody()).contains("notes");
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
        assertThat(response.getBody()).contains("/api/v1/menus/{id}/recipe-productions/{recipeProductionId}/transfer");
        assertThat(response.getBody()).contains("/api/v1/menus/{id}/stock-movements");
        assertThat(response.getBody()).contains("/api/v1/menus/{id}/week-stock/transfer");
        assertThat(response.getBody()).contains("/api/v1/menus/{id}/week-stock");
        assertThat(response.getBody()).contains("/api/v1/menus/{id}/payer");
        assertThat(response.getBody()).contains("MenuPageResponse");
        assertThat(response.getBody()).contains("state");
        assertThat(response.getBody()).contains("MenuRecipeProductionResponse");
        assertThat(response.getBody()).contains("MenuStockMovementResponse");
        assertThat(response.getBody()).contains("MenuRangeStatsResponse");
        assertThat(response.getBody()).contains("WeekStockItemResponse");
        assertThat(response.getBody()).contains("/api/v1/menus/{id}/close/summary");
        assertThat(response.getBody()).contains("CloseMenuSummaryResponse");
        assertThat(response.getBody()).contains("transferWeekStock");
        assertThat(response.getBody()).contains("Temporary stock lines tracked for the established week with quantity and unit price");
        assertThat(response.getBody()).contains("Unit price stored for the menu stock item");
        assertThat(response.getBody()).contains("CreateMenuStockMovementRequest");
        assertThat(response.getBody()).contains("CreateMenuStockTransferRequest");
        assertThat(response.getBody()).contains("UpdateCurrentWeekMenuStockRequest");
        assertThat(response.getBody()).contains("UpdateCurrentWeekMenuPayerRequest");

        JsonNode contract = objectMapper.readTree(response.getBody());
        assertThat(contract.get("security").get(0).has("bearerAuth")).isTrue();
        assertThat(contract.get("paths").get("/api/v1/auth/login").get("post").get("security").isEmpty()).isTrue();
        assertThat(contract.get("paths").get("/api/v1/auth/register").get("post").get("security").isEmpty()).isTrue();
        assertThat(contract.get("paths").get("/api/v1/health").get("get").get("security").isEmpty()).isTrue();
        assertThat(contract.get("paths").get("/api/v1/media/{id}").get("get").get("security").isEmpty()).isTrue();
    }

    @Test
    void openApiGroupedDocsShouldExposeEachDomainSeparately() {
        assertOpenApiGroup("auth", "/api/v1/auth/register", "/api/v1/auth/login");
        assertOpenApiGroup("health", "/api/v1/health");
        assertOpenApiGroup("media", "/api/v1/media/{id}");
        assertOpenApiGroup("products", "/api/v1/products", "/api/v1/products/stats", "/api/v1/products/{id}");
        assertOpenApiGroup("supermarkets", "/api/v1/supermarkets", "/api/v1/supermarkets/{id}");
        assertOpenApiGroup("recipes", "/api/v1/recipes", "/api/v1/recipes/stats", "/api/v1/recipes/{id}", "/api/v1/recipes/{id}/derived-product");
        assertOpenApiGroup("stock", "/api/v1/stock", "/api/v1/stock/{stockEntryId}", "/api/v1/stock/{stockEntryId}/add", "/api/v1/stock/{stockEntryId}/remove", "/api/v1/stock/movements", "/api/v1/products/{productId}/stock", "/api/v1/products/{productId}/stock/reconciliation");
        assertOpenApiGroup(
                "users",
                "/api/v1/users",
                "/api/v1/users/{personId}/menu-history",
                "/api/v1/users/{personId}/menu-history/monthly",
                "/api/v1/users/{personId}/menu-history/annual",
                "/api/v1/users/{userId}/money-box",
                "/api/v1/users/{userId}/money-box/movements"
        );
        assertOpenApiGroup(
                "user-weights",
                "/api/v1/users/{userId}/weights",
                "/api/v1/users/{userId}/weights/stats",
                "/api/v1/users/{userId}/weights/{weightId}"
        );
        assertOpenApiGroup("money-boxes", "/api/v1/money-boxes", "/api/v1/money-boxes/{moneyBoxId}", "/api/v1/money-boxes/{moneyBoxId}/movements", "/api/v1/money-boxes/{moneyBoxId}/movements/{movementId}");
        assertOpenApiGroup("coupons", "/api/v1/coupons");
        assertOpenApiGroup("planning", "/api/v1/planning", "/api/v1/planning/{id}", "/api/v1/planning/{id}/days", "/api/v1/planning/{id}/menu", "/api/v1/planning/{id}/coupons", "/api/v1/planning/day-parts");
        assertOpenApiGroup(
                "menus",
                "/api/v1/menus",
                "/api/v1/menus/{id}",
                "/api/v1/menus/{id}/recipe-productions/{recipeProductionId}/transfer",
                "/api/v1/menus/{id}/used-stock",
                "/api/v1/menus/{id}/shopping-list",
                "/api/v1/menus/{id}/stock-movements",
                "/api/v1/menus/{id}/week-stock/transfer",
                "/api/v1/menus/{id}/week-stock",
                "/api/v1/menus/{id}/payer",
                "/api/v1/menus/{id}/close/summary",
                "/api/v1/menus/{id}/close",
                "/api/v1/menus/stats",
                "/api/v1/menus/{id}/stats"
        );
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

        ResponseEntity<UserWeightResponse> beforePeriodWeight = postAuthorized(
                weightsUrl,
                new CreateUserWeightRequest(new BigDecimal("80.00"), beforePeriod, "before period"),
                UserWeightResponse.class
        );
        assertThat(beforePeriodWeight.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(beforePeriodWeight.getBody()).isNotNull();
        assertThat(beforePeriodWeight.getBody().notes()).isEqualTo("before period");
        assertThat(beforePeriodWeight.getBody().createdAt()).isNotNull();
        assertThat(beforePeriodWeight.getBody().updatedAt()).isNotNull();
        assertThat(beforePeriodWeight.getBody().createdAt()).isEqualTo(beforePeriodWeight.getBody().updatedAt());
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
        assertThat(created.getBody().notes()).isNull();
        assertThat(created.getBody().createdAt()).isNotNull();
        assertThat(created.getBody().updatedAt()).isNotNull();
        assertThat(created.getBody().createdAt()).isEqualTo(created.getBody().updatedAt());
        assertThat(getAuthorized(weightsUrl + "?from=2026-05-01T00:00:00Z&to=2026-05-31T23:59:59Z", UserWeightResponse[].class)
                .getBody()).extracting(UserWeightResponse::notes)
                .containsExactly("before period");

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

        ResponseEntity<String> missingTo = getAuthorized(
                weightsUrl + "?from=2026-06-01T00:00:00Z",
                String.class
        );
        assertThat(missingTo.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(missingTo.getBody()).contains("to");

        ResponseEntity<String> missingFrom = getAuthorized(
                weightsUrl + "?to=2026-06-30T23:59:59Z",
                String.class
        );
        assertThat(missingFrom.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(missingFrom.getBody()).contains("from");

        ResponseEntity<String> legacyWeightRoute = getAuthorized(
                "http://localhost:" + port + "/api/v1/weights/" + created.getBody().id(),
                String.class
        );
        assertThat(legacyWeightRoute.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);
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
                new UpdateUserWeightRequest(new BigDecimal("70.25"), editedAt, "edited note"),
                UserWeightResponse.class
        );
        assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updated.getBody()).isNotNull();
        assertThat(updated.getBody().weight()).isEqualByComparingTo("70.25");
        assertThat(updated.getBody().recordedAt()).isEqualTo(editedAt);
        assertThat(updated.getBody().notes()).isEqualTo("edited note");
        assertThat(updated.getBody().updatedAt()).isNotNull();
        assertThat(updated.getBody().createdAt()).isNotNull();
        assertThat(updated.getBody().updatedAt()).isAfterOrEqualTo(updated.getBody().createdAt());

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

        ResponseEntity<UserResponse[]> people = getAuthorized("http://localhost:" + port + "/api/v1/users", UserResponse[].class);
        assertThat(people.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(people.getBody()).isNotNull();
        assertThat(people.getBody()).extracting(UserResponse::id).contains(userId);

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
    void proposedWeekMenuShouldAllowManualNutritionItemsWithoutCatalogProducts() {
        String productsUrl = "http://localhost:" + port + "/api/v1/products";
        String dayPartsUrl = "http://localhost:" + port + "/api/v1/planning/day-parts";
        String proposedMenusUrl = "http://localhost:" + port + "/api/v1/planning";
        Long yogurtId = createProduct(productsUrl, "Manual Yogurt", "Greek yogurt", "59", "3.6", "10", "0.4", "125");
        Long lunchDayPartId = createDayPart(dayPartsUrl, "Lunch manual", "Main meal of the day", 10);

        ResponseEntity<ProposedWeekMenuResponse> createdMenu = postAuthorized(
                proposedMenusUrl,
                new CreateProposedWeekMenuRequest(LocalDate.of(2026, 8, 1), LocalDate.of(2026, 8, 7)),
                ProposedWeekMenuResponse.class
        );
        assertThat(createdMenu.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<ProposedWeekMenuResponse> updatedMenu = restTemplate.exchange(
                proposedMenusUrl + "/" + createdMenu.getBody().id() + "/days",
                HttpMethod.PUT,
                authorizedEntity(new UpsertProposedWeekMenuDayRequest(
                        LocalDate.of(2026, 8, 1),
                        List.of(
                                new ProposedWeekMenuSectionRequest(
                                        lunchDayPartId,
                                        List.of(
                                                new ProposedWeekMenuProductRequest(yogurtId, new BigDecimal("1"), new BigDecimal("100"), 10),
                                                new ProposedWeekMenuProductRequest(
                                                        "Homemade fruit bowl",
                                                        new BigDecimal("180"),
                                                        new BigDecimal("24"),
                                                        new BigDecimal("5"),
                                                        new BigDecimal("6"),
                                                        20
                                                )
                                        )
                                )
                        )
                )),
                ProposedWeekMenuResponse.class
        );

        assertThat(updatedMenu.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updatedMenu.getBody()).isNotNull();
        assertThat(updatedMenu.getBody().days()).hasSize(1);
        assertThat(updatedMenu.getBody().days().getFirst().sections()).hasSize(1);
        assertThat(updatedMenu.getBody().days().getFirst().sections().getFirst().products()).hasSize(2);
        assertThat(updatedMenu.getBody().days().getFirst().sections().getFirst().products())
                .filteredOn(product -> product.productId() == null)
                .singleElement()
                .satisfies(product -> {
                    assertThat(product.productName()).isEqualTo("Homemade fruit bowl");
                    assertThat(product.units()).isNull();
                    assertThat(product.grams()).isNull();
                    assertThat(product.nutritionalValues().calories()).isEqualByComparingTo("180.00");
                    assertThat(product.nutritionalValues().carbohydrates()).isEqualByComparingTo("24.00");
                    assertThat(product.nutritionalValues().proteins()).isEqualByComparingTo("5.00");
                    assertThat(product.nutritionalValues().fats()).isEqualByComparingTo("6.00");
                });
        assertThat(updatedMenu.getBody().nutritionalValues().calories()).isEqualByComparingTo("239.00");
        assertThat(updatedMenu.getBody().nutritionalValues().carbohydrates()).isEqualByComparingTo("27.60");
        assertThat(updatedMenu.getBody().stockSummary().distinctProducts()).isEqualTo(1);
        assertThat(updatedMenu.getBody().stockSummary().requirements()).hasSize(1);
        assertThat(updatedMenu.getBody().stockSummary().requirements().getFirst().productId()).isEqualTo(yogurtId);

        ResponseEntity<CurrentWeekMenuResponse> establishedMenu = postAuthorized(
                proposedMenusUrl + "/" + createdMenu.getBody().id() + "/menu",
                new EstablishProposedWeekMenuRequest(authenticatedUserId()),
                CurrentWeekMenuResponse.class
        );

        assertThat(establishedMenu.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(establishedMenu.getBody()).isNotNull();
        assertThat(establishedMenu.getBody().days().getFirst().sections().getFirst().products())
                .filteredOn(product -> product.productId() == null)
                .singleElement()
                .satisfies(product -> {
                    assertThat(product.productName()).isEqualTo("Homemade fruit bowl");
                    assertThat(product.units()).isNull();
                    assertThat(product.grams()).isNull();
                    assertThat(product.nutritionalValues().calories()).isEqualByComparingTo("180.00");
                    assertThat(product.nutritionalValues().carbohydrates()).isEqualByComparingTo("24.00");
                    assertThat(product.nutritionalValues().proteins()).isEqualByComparingTo("5.00");
                    assertThat(product.nutritionalValues().fats()).isEqualByComparingTo("6.00");
                });
    }

    @Test
    void proposedWeekMenuShouldRejectManualQuantity() {
        String proposedMenusUrl = "http://localhost:" + port + "/api/v1/planning";
        Long lunchDayPartId = createDayPart(
                "http://localhost:" + port + "/api/v1/planning/day-parts",
                "Lunch quantity",
                "Main meal of the day",
                15
        );

        ResponseEntity<ProposedWeekMenuResponse> createdMenu = postAuthorized(
                proposedMenusUrl,
                new CreateProposedWeekMenuRequest(LocalDate.of(2026, 9, 1), LocalDate.of(2026, 9, 7)),
                ProposedWeekMenuResponse.class
        );

        ResponseEntity<ProposedWeekMenuResponse> rejectedMenu = restTemplate.exchange(
                proposedMenusUrl + "/" + createdMenu.getBody().id() + "/days",
                HttpMethod.PUT,
                authorizedEntity(new UpsertProposedWeekMenuDayRequest(
                        LocalDate.of(2026, 9, 1),
                        List.of(new ProposedWeekMenuSectionRequest(
                                lunchDayPartId,
                                List.of(new ProposedWeekMenuProductRequest(
                                        null,
                                        "Homemade fruit bowl",
                                        new BigDecimal("1"),
                                        new BigDecimal("50"),
                                        new BigDecimal("180"),
                                        new BigDecimal("24"),
                                        new BigDecimal("5"),
                                        new BigDecimal("6"),
                                        10
                                ))
                        ))
                )),
                ProposedWeekMenuResponse.class
        );

        assertThat(rejectedMenu.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
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
    void planningShouldScaleStockRequirementsByTheNumberOfUsers() {
        String productsUrl = "http://localhost:" + port + "/api/v1/products";
        String dayPartsUrl = "http://localhost:" + port + "/api/v1/planning/day-parts";
        String proposedMenusUrl = "http://localhost:" + port + "/api/v1/planning";

        Long riceId = createProduct(productsUrl, "Users Rice", "Rice", "100", "0", "2.7", "0.3", "1.20");
        Long lunchDayPartId = createDayPart(dayPartsUrl, "Lunch users", "Main meal of the day", 10);

        postAuthorized(
                productsUrl + "/" + riceId + "/stock",
                new CreateStockEntryRequest(new BigDecimal("2.00"), new BigDecimal("1.25"), null, LocalDate.of(2026, 6, 12)),
                StockEntryResponse.class
        );

        ResponseEntity<ProposedWeekMenuResponse> planning = postAuthorized(
                proposedMenusUrl,
                new CreateProposedWeekMenuRequest(LocalDate.of(2026, 6, 15), LocalDate.of(2026, 6, 21), 3),
                ProposedWeekMenuResponse.class
        );
        assertThat(planning.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(planning.getBody()).isNotNull();
        assertThat(planning.getBody().users()).isEqualTo(3);

        ResponseEntity<ProposedWeekMenuResponse> plannedMenu = restTemplate.exchange(
                proposedMenusUrl + "/" + planning.getBody().id() + "/days",
                HttpMethod.PUT,
                authorizedEntity(new UpsertProposedWeekMenuDayRequest(
                        LocalDate.of(2026, 6, 15),
                        List.of(
                                new ProposedWeekMenuSectionRequest(
                                        lunchDayPartId,
                                        List.of(new ProposedWeekMenuProductRequest(riceId, new BigDecimal("1.00"), null, 10))
                                )
                        )
                )),
                ProposedWeekMenuResponse.class
        );
        assertThat(plannedMenu.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(plannedMenu.getBody()).isNotNull();
        assertThat(plannedMenu.getBody().stockSummary().requirements()).singleElement().satisfies(requirement -> {
            assertThat(requirement.requiredUnits()).isEqualByComparingTo("3.00");
            assertThat(requirement.availableUnits()).isEqualByComparingTo("2.00");
            assertThat(requirement.coveredUnits()).isEqualByComparingTo("2.00");
            assertThat(requirement.missingUnits()).isEqualByComparingTo("1.00");
        });

        ResponseEntity<CurrentWeekMenuResponse> established = postAuthorized(
                proposedMenusUrl + "/" + planning.getBody().id() + "/menu",
                new EstablishProposedWeekMenuRequest(authenticatedUserId()),
                CurrentWeekMenuResponse.class
        );
        assertThat(established.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(established.getBody()).isNotNull();
        assertThat(established.getBody().shoppingList()).singleElement().satisfies(item ->
                assertThat(item.missingUnits()).isEqualByComparingTo("1.00")
        );
    }

    @Test
    void planningShouldBeDeletableAndDisappearFromTheCatalog() {
        String planningUrl = "http://localhost:" + port + "/api/v1/planning";

        ResponseEntity<ProposedWeekMenuResponse> created = postAuthorized(
                planningUrl,
                new CreateProposedWeekMenuRequest(LocalDate.of(2040, 4, 1), LocalDate.of(2040, 4, 7)),
                ProposedWeekMenuResponse.class
        );
        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).isNotNull();

        ResponseEntity<String> deleted = restTemplate.exchange(
                planningUrl + "/" + created.getBody().id(),
                HttpMethod.DELETE,
                authorizedEntity(null),
                String.class
        );
        assertThat(deleted.getStatusCode()).isEqualTo(HttpStatus.NO_CONTENT);

        ResponseEntity<String> missing = restTemplate.exchange(
                planningUrl + "/" + created.getBody().id(),
                HttpMethod.GET,
                authorizedEntity(null),
                String.class
        );
        assertThat(missing.getStatusCode()).isEqualTo(HttpStatus.NOT_FOUND);

        PlanningSummaryResponse[] summaries = getAuthorized(planningUrl, PlanningSummaryResponse[].class).getBody();
        assertThat(java.util.Arrays.stream(summaries).map(PlanningSummaryResponse::id)).doesNotContain(created.getBody().id());
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
        assertThat(established.getBody().personIds()).isEmpty();
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
        assertThat(loadedCurrentWeek.getBody().personIds()).containsExactly(authenticatedUserId());
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
    void establishingAProposedWeekWithEmptyStockAllocationsShouldAutoConsumeAvailableStock() {
        String productsUrl = "http://localhost:" + port + "/api/v1/products";
        String dayPartsUrl = "http://localhost:" + port + "/api/v1/planning/day-parts";
        String stockUrl = "http://localhost:" + port + "/api/v1/stock";
        String proposedMenusUrl = "http://localhost:" + port + "/api/v1/planning";
        Long riceId = createProduct(productsUrl, "Auto Allocate Rice", "Rice", "100", "0", "0", "0", "2");
        Long lunchDayPartId = createDayPart(dayPartsUrl, "Lunch auto allocate", "Main meal of the day", 10);

        ResponseEntity<StockEntryResponse> riceStock = postAuthorized(
                productsUrl + "/" + riceId + "/stock",
                new CreateStockEntryRequest(new BigDecimal("3.00"), new BigDecimal("2.00"), LocalDate.of(2026, 6, 20), LocalDate.of(2026, 6, 10)),
                StockEntryResponse.class
        );
        assertThat(riceStock.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<ProposedWeekMenuResponse> planning = postAuthorized(
                proposedMenusUrl,
                new CreateProposedWeekMenuRequest(LocalDate.of(2026, 6, 15), LocalDate.of(2026, 6, 21)),
                ProposedWeekMenuResponse.class
        );
        assertThat(planning.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(planning.getBody()).isNotNull();

        restTemplate.exchange(
                proposedMenusUrl + "/" + planning.getBody().id() + "/days",
                HttpMethod.PUT,
                authorizedEntity(new UpsertProposedWeekMenuDayRequest(
                        LocalDate.of(2026, 6, 15),
                        List.of(
                                new ProposedWeekMenuSectionRequest(
                                        lunchDayPartId,
                                        List.of(new ProposedWeekMenuProductRequest(riceId, new BigDecimal("2.00"), null, 10))
                                )
                        )
                )),
                ProposedWeekMenuResponse.class
        );

        ResponseEntity<CurrentWeekMenuResponse> established = postAuthorized(
                proposedMenusUrl + "/" + planning.getBody().id() + "/menu",
                new EstablishProposedWeekMenuRequest(authenticatedUserId(), List.of()),
                CurrentWeekMenuResponse.class
        );

        assertThat(established.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(established.getBody()).isNotNull();
        assertThat(established.getBody().usedStock()).singleElement().satisfies(used -> {
            assertThat(used.productId()).isEqualTo(riceId);
            assertThat(used.usedUnits()).isEqualByComparingTo("2.00");
        });
        assertThat(established.getBody().shoppingList()).isEmpty();

        ResponseEntity<StockEntryResponse[]> appliedStock = getAuthorized(
                stockUrl + "?productIds=" + riceId,
                StockEntryResponse[].class
        );
        assertThat(appliedStock.getBody()).singleElement().satisfies(stock ->
                assertThat(stock.quantity()).isEqualByComparingTo("1.00")
        );
    }

    @Test
    void menuRangeStatsShouldAggregateMenusInsideTheRequestedWindow() {
        String productsUrl = "http://localhost:" + port + "/api/v1/products";
        String dayPartsUrl = "http://localhost:" + port + "/api/v1/planning/day-parts";
        String planningUrl = "http://localhost:" + port + "/api/v1/planning";
        String menusUrl = "http://localhost:" + port + "/api/v1/menus";

        Long lunchDayPartId = createDayPart(dayPartsUrl, "Range Lunch", "Range lunch", 10);
        Long tomatoId = createProduct(productsUrl, "Range Tomato", "Tomato", "100", "20", "4", "1", "1.00");
        Long potatoId = createProduct(productsUrl, "Range Potato", "Potato", "200", "40", "8", "2", "1.00");
        Long firstPlanningId = null;
        Long secondPlanningId = null;

        try {
            postAuthorized(
                    productsUrl + "/" + tomatoId + "/stock",
                    new CreateStockEntryRequest(new BigDecimal("1.00"), new BigDecimal("2.50"), null, LocalDate.of(2026, 6, 1)),
                    StockEntryResponse.class
            );
            postAuthorized(
                    productsUrl + "/" + potatoId + "/stock",
                    new CreateStockEntryRequest(new BigDecimal("1.00"), new BigDecimal("3.00"), null, LocalDate.of(2026, 6, 1)),
                    StockEntryResponse.class
            );

            ResponseEntity<ProposedWeekMenuResponse> firstPlanning = postAuthorized(
                    planningUrl,
                    new CreateProposedWeekMenuRequest(LocalDate.of(2034, 6, 3), LocalDate.of(2034, 6, 3)),
                    ProposedWeekMenuResponse.class
            );
            assertThat(firstPlanning.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            firstPlanningId = firstPlanning.getBody().id();
            ResponseEntity<ProposedWeekMenuResponse> firstPlanned = putAuthorized(
                    planningUrl + "/" + firstPlanningId + "/days",
                    new UpsertProposedWeekMenuDayRequest(
                            LocalDate.of(2034, 6, 3),
                            List.of(new ProposedWeekMenuSectionRequest(
                                    lunchDayPartId,
                                    List.of(new ProposedWeekMenuProductRequest(tomatoId, new BigDecimal("1.00"), null, 10))
                            ))
                    ),
                    ProposedWeekMenuResponse.class
            );
            assertThat(firstPlanned.getStatusCode()).isEqualTo(HttpStatus.OK);
            CurrentWeekMenuResponse firstMenu = postAuthorized(
                    planningUrl + "/" + firstPlanningId + "/menu",
                    new EstablishProposedWeekMenuRequest(authenticatedUserId()),
                    CurrentWeekMenuResponse.class
            ).getBody();
            assertThat(firstMenu).isNotNull();

            ResponseEntity<ProposedWeekMenuResponse> secondPlanning = postAuthorized(
                    planningUrl,
                    new CreateProposedWeekMenuRequest(LocalDate.of(2034, 6, 10), LocalDate.of(2034, 6, 10)),
                    ProposedWeekMenuResponse.class
            );
            assertThat(secondPlanning.getStatusCode()).isEqualTo(HttpStatus.CREATED);
            secondPlanningId = secondPlanning.getBody().id();
            ResponseEntity<ProposedWeekMenuResponse> secondPlanned = putAuthorized(
                    planningUrl + "/" + secondPlanningId + "/days",
                    new UpsertProposedWeekMenuDayRequest(
                            LocalDate.of(2034, 6, 10),
                            List.of(new ProposedWeekMenuSectionRequest(
                                    lunchDayPartId,
                                    List.of(new ProposedWeekMenuProductRequest(potatoId, new BigDecimal("1.00"), null, 10))
                            ))
                    ),
                    ProposedWeekMenuResponse.class
            );
            assertThat(secondPlanned.getStatusCode()).isEqualTo(HttpStatus.OK);
            CurrentWeekMenuResponse secondMenu = postAuthorized(
                    planningUrl + "/" + secondPlanningId + "/menu",
                    new EstablishProposedWeekMenuRequest(authenticatedUserId()),
                    CurrentWeekMenuResponse.class
            ).getBody();
            assertThat(secondMenu).isNotNull();

            ResponseEntity<CurrentWeekMenuRangeStatsResponse> partialRange = getAuthorized(
                    menusUrl + "/stats?from=2034-06-03&to=2034-06-03",
                    CurrentWeekMenuRangeStatsResponse.class
            );
            assertThat(partialRange.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(partialRange.getBody()).isNotNull();
            assertThat(partialRange.getBody().from()).isEqualTo(LocalDate.of(2034, 6, 3));
            assertThat(partialRange.getBody().to()).isEqualTo(LocalDate.of(2034, 6, 3));
            assertThat(partialRange.getBody().plannedDays()).isEqualTo(1);
            assertThat(partialRange.getBody().calories()).isEqualByComparingTo("1.00");
            assertThat(partialRange.getBody().distinctProducts()).isEqualTo(1L);
            assertThat(partialRange.getBody().estimatedCost()).isEqualByComparingTo("2.50");
            assertThat(partialRange.getBody().menuIds()).containsExactly(firstMenu.id());

            ResponseEntity<CurrentWeekMenuRangeStatsResponse> multiRange = getAuthorized(
                    menusUrl + "/stats?from=2034-06-03&to=2034-06-10",
                    CurrentWeekMenuRangeStatsResponse.class
            );
            assertThat(multiRange.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(multiRange.getBody()).isNotNull();
            assertThat(multiRange.getBody().plannedDays()).isEqualTo(2);
            assertThat(multiRange.getBody().calories()).isEqualByComparingTo("3.00");
            assertThat(multiRange.getBody().distinctProducts()).isEqualTo(2L);
            assertThat(multiRange.getBody().estimatedCost()).isEqualByComparingTo("5.50");
            assertThat(multiRange.getBody().menuIds()).containsExactly(firstMenu.id(), secondMenu.id());

            ResponseEntity<CurrentWeekMenuRangeStatsResponse> emptyRange = getAuthorized(
                    menusUrl + "/stats?from=2034-07-01&to=2034-07-07",
                    CurrentWeekMenuRangeStatsResponse.class
            );
            assertThat(emptyRange.getStatusCode()).isEqualTo(HttpStatus.OK);
            assertThat(emptyRange.getBody()).isNotNull();
            assertThat(emptyRange.getBody().plannedDays()).isZero();
            assertThat(emptyRange.getBody().calories()).isEqualByComparingTo("0.00");
            assertThat(emptyRange.getBody().distinctProducts()).isZero();
            assertThat(emptyRange.getBody().estimatedCost()).isEqualByComparingTo("0.00");
            assertThat(emptyRange.getBody().menuIds()).isEmpty();
        } finally {
            if (firstPlanningId != null) {
                deleteAuthorized(planningUrl + "/" + firstPlanningId, String.class);
            }
            if (secondPlanningId != null) {
                deleteAuthorized(planningUrl + "/" + secondPlanningId, String.class);
            }
            deleteAuthorized(productsUrl + "/" + tomatoId, String.class);
            deleteAuthorized(productsUrl + "/" + potatoId, String.class);
        }
    }

    @Test
    void establishingAnOverlappingMenuForTheSameUserShouldBeRejected() {
        String proposedMenusUrl = "http://localhost:" + port + "/api/v1/planning";
        LocalDate firstStart = LocalDate.of(2026, 6, 15);
        LocalDate firstEnd = LocalDate.of(2026, 6, 21);
        LocalDate secondStart = LocalDate.of(2026, 6, 20);
        LocalDate secondEnd = LocalDate.of(2026, 6, 27);

        ResponseEntity<ProposedWeekMenuResponse> firstPlanning = postAuthorized(
                proposedMenusUrl,
                new CreateProposedWeekMenuRequest(firstStart, firstEnd),
                ProposedWeekMenuResponse.class
        );
        assertThat(firstPlanning.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<CurrentWeekMenuResponse> firstMenu = postAuthorized(
                proposedMenusUrl + "/" + firstPlanning.getBody().id() + "/menu",
                new EstablishProposedWeekMenuRequest(authenticatedUserId()),
                CurrentWeekMenuResponse.class
        );
        assertThat(firstMenu.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<ProposedWeekMenuResponse> secondPlanning = postAuthorized(
                proposedMenusUrl,
                new CreateProposedWeekMenuRequest(secondStart, secondEnd),
                ProposedWeekMenuResponse.class
        );
        assertThat(secondPlanning.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<String> rejectedMenu = restTemplate.exchange(
                proposedMenusUrl + "/" + secondPlanning.getBody().id() + "/menu",
                HttpMethod.POST,
                authorizedEntity(new EstablishProposedWeekMenuRequest(authenticatedUserId())),
                String.class
        );

        assertThat(rejectedMenu.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
        assertThat(rejectedMenu.getBody()).contains("overlapping menu");
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
    void menuShouldTrackStockRepercussionsAndUseResponsibleAsDefault() {
        String productsUrl = "http://localhost:" + port + "/api/v1/products";
        String dayPartsUrl = "http://localhost:" + port + "/api/v1/planning/day-parts";
        String proposedMenusUrl = "http://localhost:" + port + "/api/v1/planning";
        String menusUrl = "http://localhost:" + port + "/api/v1/menus";
        Long chickenId = createProduct(productsUrl, "Ledger Chicken", "Chicken breast", "200", "0", "31", "3.6", "2.00");
        Long riceId = createProduct(productsUrl, "Ledger Rice", "Rice", "130", "28", "2.5", "0.3", "1.50");
        Long beansId = createProduct(productsUrl, "Ledger Beans", "Beans", "110", "20", "8", "0.5", "1.00");
        Long lunchDayPartId = createDayPart(dayPartsUrl, "Lunch ledger", "Main meal of the day", 10);

        AuthResponse secondPerson = restTemplate.postForEntity(
                "http://localhost:" + port + "/api/v1/auth/register",
                new RegisterRequest("ledger-person-" + System.nanoTime(), "secret-password", REGISTRATION_CODE),
                AuthResponse.class
        ).getBody();
        assertThat(secondPerson).isNotNull();

        ProposedWeekMenuResponse planning = postAuthorized(
                proposedMenusUrl,
                new CreateProposedWeekMenuRequest(LocalDate.of(2026, 6, 15), LocalDate.of(2026, 6, 21)),
                ProposedWeekMenuResponse.class
        ).getBody();
        assertThat(planning).isNotNull();

        ResponseEntity<ProposedWeekMenuResponse> plannedMenu = restTemplate.exchange(
                proposedMenusUrl + "/" + planning.id() + "/days",
                HttpMethod.PUT,
                authorizedEntity(new UpsertProposedWeekMenuDayRequest(
                        LocalDate.of(2026, 6, 15),
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

        CurrentWeekMenuResponse established = postAuthorized(
                proposedMenusUrl + "/" + planning.id() + "/menu",
                new EstablishProposedWeekMenuRequest(
                        authenticatedUserId(),
                        null,
                        null,
                        List.of(authenticatedUserId(), secondPerson.userId())
                ),
                CurrentWeekMenuResponse.class
        ).getBody();
        assertThat(established).isNotNull();
        assertThat(established.personIds()).containsExactly(authenticatedUserId(), secondPerson.userId());

        ResponseEntity<CurrentWeekMenuResponse> responsibleUpdated = restTemplate.exchange(
                menusUrl + "/" + established.id() + "/payer",
                HttpMethod.PUT,
                authorizedEntity(new UpdateCurrentWeekMenuPayerRequest(secondPerson.userId())),
                CurrentWeekMenuResponse.class
        );
        assertThat(responsibleUpdated.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(responsibleUpdated.getBody()).isNotNull();
        assertThat(responsibleUpdated.getBody().payerUserId()).isEqualTo(secondPerson.userId());

        ResponseEntity<CurrentWeekMenuResponse> repercussion = restTemplate.exchange(
                menusUrl + "/" + established.id() + "/stock-movements",
                HttpMethod.POST,
                authorizedEntity(new CreateMenuStockMovementRequest(
                        null,
                        riceId,
                        new BigDecimal("1.00"),
                        new BigDecimal("1.25"),
                        "Rice top up"
                )),
                CurrentWeekMenuResponse.class
        );
        assertThat(repercussion.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(repercussion.getBody()).isNotNull();
        assertThat(repercussion.getBody().payerUserId()).isEqualTo(secondPerson.userId());
        assertThat(repercussion.getBody().shoppingList())
                .filteredOn(item -> item.productId().equals(riceId))
                .singleElement()
                .satisfies(item -> assertThat(item.missingUnits()).isEqualByComparingTo("1.00"));
        assertThat(repercussion.getBody().stockMovements()).singleElement().satisfies(movement -> {
            assertThat(movement.userId()).isEqualTo(secondPerson.userId());
            assertThat(movement.productId()).isEqualTo(riceId);
            assertThat(movement.totalCost()).isEqualByComparingTo("1.25");
        });

        ResponseEntity<MenuStockMovementResponse[]> ledger = getAuthorized(
                menusUrl + "/" + established.id() + "/stock-movements",
                MenuStockMovementResponse[].class
        );
        assertThat(ledger.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(ledger.getBody()).isNotNull();
        assertThat(ledger.getBody()).hasSize(1);
        assertThat(ledger.getBody()[0].userId()).isEqualTo(secondPerson.userId());

        ResponseEntity<UserMoneyBoxResponse> secondMoneyBox = getAuthorized(
                "http://localhost:" + port + "/api/v1/users/" + secondPerson.userId() + "/money-box",
                UserMoneyBoxResponse.class
        );
        assertThat(secondMoneyBox.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(secondMoneyBox.getBody()).isNotNull();
        assertThat(secondMoneyBox.getBody().movements()).singleElement().satisfies(movement -> {
            assertThat(movement.menuId()).isEqualTo(established.id());
            assertThat(movement.amount()).isEqualByComparingTo("-1.25");
        });

        ResponseEntity<CurrentWeekMenuStatsResponse> closed = postAuthorized(
                menusUrl + "/" + established.id() + "/close",
                new CloseCurrentWeekMenuRequest(List.of(authenticatedUserId(), secondPerson.userId())),
                CurrentWeekMenuStatsResponse.class
        );
        assertThat(closed.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(closed.getBody()).isNotNull();

        ResponseEntity<StockEntryResponse[]> riceStock = getAuthorized(
                productsUrl + "/" + riceId + "/stock",
                StockEntryResponse[].class
        );
        assertThat(riceStock.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(riceStock.getBody()).singleElement().satisfies(stock -> assertThat(stock.quantity()).isEqualByComparingTo("1.00"));
        ResponseEntity<Void> cleanedRiceStock = restTemplate.exchange(
                "http://localhost:" + port + "/api/v1/stock/" + riceStock.getBody()[0].id() + "/remove",
                HttpMethod.POST,
                authorizedEntity(new AdjustStockQuantityRequest(new BigDecimal("1.00"))),
                Void.class
        );
        assertThat(cleanedRiceStock.getStatusCode()).isEqualTo(HttpStatus.OK);
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
        ResponseEntity<UserMenuHistoryResponse> rangeHistory = getAuthorized(
                "http://localhost:" + port + "/api/v1/users/" + authenticatedUserId()
                        + "/menu-history?from=2018-12-31T00:00:00Z&to=2019-01-31T23:59:59Z",
                UserMenuHistoryResponse.class
        );
        assertThat(rangeHistory.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(rangeHistory.getBody()).isNotNull();
        assertThat(rangeHistory.getBody().personId()).isEqualTo(authenticatedUserId());
        assertThat(rangeHistory.getBody().personName()).isNotBlank();
        assertThat(rangeHistory.getBody().from()).isEqualTo(Instant.parse("2018-12-31T00:00:00Z"));
        assertThat(rangeHistory.getBody().to()).isEqualTo(Instant.parse("2019-01-31T23:59:59Z"));
        assertThat(rangeHistory.getBody().totals()).isNotNull();
        assertThat(rangeHistory.getBody().menus()).extracting(UserMenuHistoryEntryResponse::menuId)
                .contains(menu.id());

        ResponseEntity<String> invalidRange = getAuthorized(
                "http://localhost:" + port + "/api/v1/users/" + authenticatedUserId()
                        + "/menu-history?from=2019-01-31T00:00:00Z&to=2018-12-31T23:59:59Z",
                String.class
        );
        assertThat(invalidRange.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        ResponseEntity<String> missingRangeTo = getAuthorized(
                "http://localhost:" + port + "/api/v1/users/" + authenticatedUserId()
                        + "/menu-history?from=2018-12-31T00:00:00Z",
                String.class
        );
        assertThat(missingRangeTo.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);

        ResponseEntity<String> missingRangeFrom = getAuthorized(
                "http://localhost:" + port + "/api/v1/users/" + authenticatedUserId()
                        + "/menu-history?to=2019-01-31T23:59:59Z",
                String.class
        );
        assertThat(missingRangeFrom.getStatusCode()).isEqualTo(HttpStatus.BAD_REQUEST);
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
        LocalDate today = LocalDate.now();
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
        assertThat(allStock.getBody()).extracting(StockEntryResponse::productId)
                .contains(appleId, bananaId);
        assertThat(allStock.getBody())
                .filteredOn(stock -> stock.productId().equals(bananaId))
                .singleElement()
                .satisfies(stock -> {
                    assertThat(stock.price()).isEqualByComparingTo("3.79");
                    assertThat(stock.quantity()).isEqualByComparingTo("4.0");
                });
        assertThat(allStock.getBody())
                .filteredOn(stock -> stock.productId().equals(appleId))
                .singleElement()
                .satisfies(stock -> {
                    assertThat(stock.quantity()).isEqualByComparingTo("5.0");
                    assertThat(stock.price()).isEqualByComparingTo("4.99");
                });

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

        ResponseEntity<StockMovementPageResponse> appleMovements = getAuthorized(
                stockUrl + "/movements?fromDate=2026-06-10&toDate=" + today + "&productIds=" + appleId + "&page=0&size=20",
                StockMovementPageResponse.class
        );
        assertThat(appleMovements.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(appleMovements.getBody()).isNotNull();
        assertThat(appleMovements.getBody().totalElements()).isEqualTo(4);
        assertThat(appleMovements.getBody().items()).hasSize(4);
        assertThat(appleMovements.getBody().items().getFirst().movementType()).isEqualTo("ADJUSTMENT");
        assertThat(appleMovements.getBody().items().getFirst().signedQuantity()).isEqualByComparingTo("-5.00");
        assertThat(appleMovements.getBody().items().getLast().movementType()).isEqualTo("ENTRY");
        assertThat(appleMovements.getBody().items().getLast().signedQuantity()).isEqualByComparingTo("5.50");

        ResponseEntity<StockReconciliationResponse> appleReconciliation = getAuthorized(
                productsUrl + "/" + appleId + "/stock/reconciliation",
                StockReconciliationResponse.class
        );
        assertThat(appleReconciliation.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(appleReconciliation.getBody()).isNotNull();
        assertThat(appleReconciliation.getBody().calculatedQuantity()).isEqualByComparingTo("0.00");
        assertThat(appleReconciliation.getBody().liveQuantity()).isEqualByComparingTo("0.00");
        assertThat(appleReconciliation.getBody().difference()).isEqualByComparingTo("0.00");
        assertThat(appleReconciliation.getBody().totalIn()).isEqualByComparingTo("7.00");
        assertThat(appleReconciliation.getBody().totalOut()).isEqualByComparingTo("7.00");
        assertThat(appleReconciliation.getBody().movementCount()).isEqualTo(4);

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
                                new RecipeIngredientAssignmentRequest(chickenId, new BigDecimal("200"), QuantityType.GRAMS),
                                new RecipeIngredientAssignmentRequest(riceId, new BigDecimal("150"), QuantityType.GRAMS)
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
                                new RecipeIngredientAssignmentRequest(chickenId, new BigDecimal("250"), QuantityType.GRAMS),
                                new RecipeIngredientAssignmentRequest(riceId, new BigDecimal("150"), QuantityType.GRAMS),
                                new RecipeIngredientAssignmentRequest(beansId, new BigDecimal("50"), QuantityType.GRAMS)
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
                                new RecipeIngredientAssignmentRequest(chickenId, new BigDecimal("200"), QuantityType.GRAMS),
                                new RecipeIngredientAssignmentRequest(coconutMilkId, new BigDecimal("100"), QuantityType.GRAMS)
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
                new CreateRecipeDerivedProductRequest("Curry base", new BigDecimal("4")),
                RecipeDerivedProductResponse.class
        );

        assertThat(derivedProduct.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(derivedProduct.getBody()).isNotNull();
        assertThat(derivedProduct.getBody().name()).isEqualTo("Curry base");
        assertThat(derivedProduct.getBody().unitsProduced()).isEqualByComparingTo("4.00");
        assertThat(derivedProduct.getBody().stockFromComposition()).isTrue();

        ResponseEntity<RecipeResponse> switchedStockMode = restTemplate.exchange(
                recipesUrl + "/" + recipeId,
                HttpMethod.PUT,
                authorizedEntity(new UpdateRecipeRequest(
                        "Curry",
                        "Creamy curry",
                        "Cook everything together.",
                        List.of(
                                new RecipeIngredientAssignmentRequest(chickenId, new BigDecimal("200"), QuantityType.GRAMS),
                                new RecipeIngredientAssignmentRequest(coconutMilkId, new BigDecimal("100"), QuantityType.GRAMS)
                        ),
                        null,
                        false,
                        null
                )),
                RecipeResponse.class
        );
        assertThat(switchedStockMode.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(switchedStockMode.getBody()).isNotNull();
        assertThat(switchedStockMode.getBody().derivedProduct()).isNotNull();
        assertThat(switchedStockMode.getBody().derivedProduct().stockFromComposition()).isFalse();

        ResponseEntity<StockEntryResponse> derivedProductStock = postAuthorized(
                productsUrl + "/" + derivedProduct.getBody().productId() + "/stock",
                new CreateStockEntryRequest(
                        new BigDecimal("2.00"),
                        new BigDecimal("0.00"),
                        LocalDate.of(2026, 6, 30),
                        LocalDate.of(2026, 6, 10)
                ),
                StockEntryResponse.class
        );
        assertThat(derivedProductStock.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<ProductStatsResponse> derivedProductStats = getAuthorized(productsUrl + "/stats", ProductStatsResponse.class);
        assertThat(derivedProductStats.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(derivedProductStats.getBody()).isNotNull();
        assertThat(derivedProductStats.getBody().summaries())
                .filteredOn(summary -> summary.productName().equals("Chicken breast"))
                .singleElement()
                .satisfies(summary -> assertThat(summary.totalQuantity()).isEqualByComparingTo("0.00"));
        assertThat(derivedProductStats.getBody().summaries())
                .filteredOn(summary -> summary.productName().equals("Coconut milk"))
                .singleElement()
                .satisfies(summary -> assertThat(summary.totalQuantity()).isEqualByComparingTo("0.00"));
        assertThat(derivedProductStats.getBody().summaries())
                .filteredOn(summary -> summary.productName().equals("Curry base"))
                .singleElement()
                .satisfies(summary -> {
                    assertThat(summary.totalQuantity()).isEqualByComparingTo("2.00");
                    assertThat(summary.batchCount()).isEqualTo(1);
                });

        ResponseEntity<RecipeResponse> updatedRecipe = restTemplate.exchange(
                recipesUrl + "/" + recipeId,
                HttpMethod.PUT,
                authorizedEntity(new UpdateRecipeRequest(
                        "Curry",
                        "Creamy curry updated",
                        "Cook slowly and reduce.",
                        List.of(
                                new RecipeIngredientAssignmentRequest(chickenId, new BigDecimal("250"), QuantityType.GRAMS),
                                new RecipeIngredientAssignmentRequest(coconutMilkId, new BigDecimal("100"), QuantityType.GRAMS)
                        ),
                        null,
                        false,
                        null
                )),
                RecipeResponse.class
        );

        assertThat(updatedRecipe.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updatedRecipe.getBody()).isNotNull();
        assertThat(updatedRecipe.getBody().nutritionalValues().calories()).isEqualByComparingTo("642.50");
        assertThat(updatedRecipe.getBody().derivedProduct()).isNotNull();
        assertThat(updatedRecipe.getBody().derivedProduct().name()).isEqualTo("Curry base");
        assertThat(updatedRecipe.getBody().derivedProduct().stockFromComposition()).isFalse();

        NutritionalValuesEntity linkedProductValues = nutritionalValuesRepository.findById(derivedProduct.getBody().productId()).orElseThrow();
        assertThat(linkedProductValues.calories()).isEqualByComparingTo("160.63");

        ResponseEntity<String> secondDerivedProductAttempt = postAuthorized(
                recipesUrl + "/" + recipeId + "/derived-product",
                new CreateRecipeDerivedProductRequest("Curry base", new BigDecimal("4"), false),
                String.class
        );
        assertThat(secondDerivedProductAttempt.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
    }

    @Test
    void recipeDerivedProductShouldRejectExistingProductNames() {
        String baseUrl = "http://localhost:" + port + "/api/v1";
        String suffix = Long.toString(System.nanoTime());
        String productName = "Derived product conflict " + suffix;

        Long ingredientId = createProduct(baseUrl + "/products", "Derived ingredient " + suffix, "Ingredient", "100", "10", "5", "2");
        postAuthorized(
                baseUrl + "/products",
                new CreateProductRequest(productName, "Already in the catalog", new BigDecimal("100"), new BigDecimal("10"), new BigDecimal("5"), new BigDecimal("2"), new BigDecimal("1.50")),
                ProductResponse.class
        );

        ResponseEntity<RecipeResponse> createdRecipe = postAuthorized(
                baseUrl + "/recipes",
                new CreateRecipeRequest(
                        "Recipe for " + suffix,
                        "Recipe description",
                        "Mix everything together.",
                        List.of(new RecipeIngredientAssignmentRequest(ingredientId, new BigDecimal("100"), QuantityType.GRAMS))
                ),
                RecipeResponse.class
        );

        ResponseEntity<String> conflict = postAuthorized(
                baseUrl + "/recipes/" + createdRecipe.getBody().id() + "/derived-product",
                new CreateRecipeDerivedProductRequest(productName, new BigDecimal("4")),
                String.class
        );

        assertThat(conflict.getStatusCode()).isEqualTo(HttpStatus.CONFLICT);
        assertThat(conflict.getBody()).contains("Product name already exists");
    }

    @Test
    void recipeDerivedProductShouldWorkWithoutStoredDerivedProductNameColumn() {
        String baseUrl = "http://localhost:" + port + "/api/v1";
        String suffix = Long.toString(System.nanoTime());

        jdbcTemplate.execute("ALTER TABLE recipe_product_origins DROP COLUMN IF EXISTS derived_product_name");

        Long ingredientId = createProduct(baseUrl + "/products", "Compatibility ingredient " + suffix, "Ingredient", "100", "10", "5", "2");
        ResponseEntity<RecipeResponse> createdRecipe = postAuthorized(
                baseUrl + "/recipes",
                new CreateRecipeRequest(
                        "Compatibility recipe " + suffix,
                        "Recipe description",
                        "Mix everything together.",
                        List.of(new RecipeIngredientAssignmentRequest(ingredientId, new BigDecimal("100"), QuantityType.GRAMS))
                ),
                RecipeResponse.class
        );

        ResponseEntity<RecipeDerivedProductResponse> derivedProduct = postAuthorized(
                baseUrl + "/recipes/" + createdRecipe.getBody().id() + "/derived-product",
                new CreateRecipeDerivedProductRequest("Compatibility base " + suffix, new BigDecimal("4")),
                RecipeDerivedProductResponse.class
        );

        assertThat(derivedProduct.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(derivedProduct.getBody()).isNotNull();
        assertThat(derivedProduct.getBody().name()).isEqualTo("Compatibility base " + suffix);
    }

    @Test
    void recipeProductionsShouldTransferToStockManuallyAndAutomaticallyAtClose() {
        String baseUrl = "http://localhost:" + port + "/api/v1";
        String suffix = Long.toString(System.nanoTime());
        Long ingredientId = createProduct(baseUrl + "/products", "Recipe ingredient " + suffix, "Ingredient", "100", "10", "5", "2");
        Long dayPartId = createDayPart(baseUrl + "/planning/day-parts", "Recipe day " + suffix, "Recipe stock day", 997);

        ResponseEntity<RecipeResponse> recipeA = postAuthorized(
                baseUrl + "/recipes",
                new CreateRecipeRequest(
                        "Recipe A " + suffix,
                        "Recipe A",
                        "Prepare recipe A.",
                        List.of(new RecipeIngredientAssignmentRequest(ingredientId, new BigDecimal("100"), QuantityType.GRAMS))
                ),
                RecipeResponse.class
        );
        ResponseEntity<RecipeDerivedProductResponse> derivedA = postAuthorized(
                baseUrl + "/recipes/" + recipeA.getBody().id() + "/derived-product",
                new CreateRecipeDerivedProductRequest("Recipe A base", new BigDecimal("4")),
                RecipeDerivedProductResponse.class
        );

        ResponseEntity<RecipeResponse> recipeB = postAuthorized(
                baseUrl + "/recipes",
                new CreateRecipeRequest(
                        "Recipe B " + suffix,
                        "Recipe B",
                        "Prepare recipe B.",
                        List.of(new RecipeIngredientAssignmentRequest(ingredientId, new BigDecimal("120"), QuantityType.GRAMS))
                ),
                RecipeResponse.class
        );
        ResponseEntity<RecipeDerivedProductResponse> derivedB = postAuthorized(
                baseUrl + "/recipes/" + recipeB.getBody().id() + "/derived-product",
                new CreateRecipeDerivedProductRequest("Recipe B base", new BigDecimal("6")),
                RecipeDerivedProductResponse.class
        );

        LocalDate startDate = LocalDate.of(2026, 5, 1);
        ResponseEntity<ProposedWeekMenuResponse> planning = postAuthorized(
                baseUrl + "/planning",
                new CreateProposedWeekMenuRequest(startDate, startDate.plusDays(6)),
                ProposedWeekMenuResponse.class
        );

        ResponseEntity<ProposedWeekMenuResponse> planned = restTemplate.exchange(
                baseUrl + "/planning/" + planning.getBody().id() + "/days",
                HttpMethod.PUT,
                authorizedEntity(new UpsertProposedWeekMenuDayRequest(
                        startDate,
                        List.of(),
                        List.of(
                                new ProposedWeekMenuRecipeProductionRequest(recipeA.getBody().id(), new BigDecimal("400"), 1),
                                new ProposedWeekMenuRecipeProductionRequest(recipeB.getBody().id(), new BigDecimal("600"), 2)
                        )
                )),
                ProposedWeekMenuResponse.class
        );
        assertThat(planned.getBody()).isNotNull();

        ResponseEntity<CurrentWeekMenuResponse> established = postAuthorized(
                baseUrl + "/planning/" + planning.getBody().id() + "/menu",
                new EstablishProposedWeekMenuRequest(authenticatedUserId()),
                CurrentWeekMenuResponse.class
        );
        assertThat(established.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<CurrentWeekMenuStatsResponse> closed = postAuthorized(
                baseUrl + "/menus/" + established.getBody().id() + "/close",
                new CloseCurrentWeekMenuRequest(List.of(authenticatedUserId())),
                CurrentWeekMenuStatsResponse.class
        );
        assertThat(closed.getStatusCode()).isEqualTo(HttpStatus.OK);
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
                        List.of(new RecipeIngredientAssignmentRequest(ingredientId, new BigDecimal("180"), QuantityType.GRAMS)),
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
                new NutritionalRulesPeriodRequest(
                        new NutrientRuleRequest(new BigDecimal("100"), new BigDecimal("200")),
                        new NutrientRuleRequest(null, new BigDecimal("20")),
                        new NutrientRuleRequest(new BigDecimal("10"), null),
                        new NutrientRuleRequest(null, null)
                ),
                new NutritionalRulesPeriodRequest(
                        new NutrientRuleRequest(new BigDecimal("120"), new BigDecimal("180")),
                        new NutrientRuleRequest(null, new BigDecimal("20")),
                        new NutrientRuleRequest(new BigDecimal("0"), new BigDecimal("20")),
                        new NutrientRuleRequest(null, null)
                )
        );

        ResponseEntity<NutritionalRulesResponse> savedRules = restTemplate.exchange(
                baseUrl + "/nutritional-rules",
                HttpMethod.PUT,
                authorizedEntity(rules),
                NutritionalRulesResponse.class
        );
        assertThat(savedRules.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(savedRules.getBody().daily().calories().minimum()).isEqualByComparingTo("100.00");
        assertThat(savedRules.getBody().weekly().calories().minimum()).isEqualByComparingTo("120.00");

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

        assertThat(planned.getBody().nutritionalRules().daily().plannedDays()).isOne();
        assertThat(planned.getBody().nutritionalRules().daily().calories().status()).isEqualTo(NutritionalRuleStatus.WITHIN_RANGE);
        assertThat(planned.getBody().nutritionalRules().daily().carbohydrates().status()).isEqualTo(NutritionalRuleStatus.ABOVE_MAXIMUM);
        assertThat(planned.getBody().nutritionalRules().daily().proteins().status()).isEqualTo(NutritionalRuleStatus.BELOW_MINIMUM);
        assertThat(planned.getBody().nutritionalRules().daily().fats().status()).isEqualTo(NutritionalRuleStatus.NOT_CONFIGURED);
        assertThat(planned.getBody().nutritionalRules().weekly().plannedDays()).isOne();
        assertThat(planned.getBody().nutritionalRules().weekly().calories().status()).isEqualTo(NutritionalRuleStatus.WITHIN_RANGE);
        assertThat(planned.getBody().nutritionalRules().weekly().carbohydrates().status()).isEqualTo(NutritionalRuleStatus.ABOVE_MAXIMUM);
        assertThat(planned.getBody().nutritionalRules().weekly().proteins().status()).isEqualTo(NutritionalRuleStatus.WITHIN_RANGE);
        assertThat(planned.getBody().nutritionalRules().weekly().fats().status()).isEqualTo(NutritionalRuleStatus.NOT_CONFIGURED);

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
        assertThat(getAuthorized(baseUrl + "/menus", MenuPageResponse.class).getBody().items())
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

    @Test
    void couponAvailabilityShouldExposeLastUseAndNextAvailabilityAfterRedeeming() {
        String baseUrl = "http://localhost:" + port + "/api/v1";
        String suffix = Long.toString(System.nanoTime());
        Long productId = createProduct(
                baseUrl + "/products", "Coupon product " + suffix,
                "Product used to verify coupon availability", "100", "10", "5", "2"
        );
        Long secondProductId = createProduct(
                baseUrl + "/products", "Coupon product 2 " + suffix,
                "Second product used to verify coupon availability", "100", "11", "6", "3"
        );
        Long thirdProductId = createProduct(
                baseUrl + "/products", "Coupon product 3 " + suffix,
                "Third product used to verify coupon availability", "100", "12", "7", "4"
        );
        Long dayPartId = createDayPart(
                baseUrl + "/planning/day-parts", "Coupon meal " + suffix,
                "Meal used to verify coupon availability", 1300
        );

        ResponseEntity<ProposedWeekMenuResponse> planning = postAuthorized(
                baseUrl + "/planning",
                new CreateProposedWeekMenuRequest(LocalDate.of(2031, 2, 1), LocalDate.of(2031, 2, 2)),
                ProposedWeekMenuResponse.class
        );
        assertThat(planning.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        restTemplate.exchange(
                baseUrl + "/planning/" + planning.getBody().id() + "/days",
                HttpMethod.PUT,
                authorizedEntity(new UpsertProposedWeekMenuDayRequest(
                        LocalDate.of(2031, 2, 1),
                        List.of(new ProposedWeekMenuSectionRequest(
                                dayPartId,
                                List.of(
                                        new ProposedWeekMenuProductRequest(productId, new BigDecimal("1.00"), null, 10),
                                        new ProposedWeekMenuProductRequest(secondProductId, new BigDecimal("1.00"), null, 20),
                                        new ProposedWeekMenuProductRequest(thirdProductId, new BigDecimal("1.00"), null, 30)
                                )
                        ))
                )),
                ProposedWeekMenuResponse.class
        );
        restTemplate.exchange(
                baseUrl + "/planning/" + planning.getBody().id() + "/days",
                HttpMethod.PUT,
                authorizedEntity(new UpsertProposedWeekMenuDayRequest(
                        LocalDate.of(2031, 2, 2),
                        List.of(new ProposedWeekMenuSectionRequest(
                                dayPartId,
                                List.of(
                                        new ProposedWeekMenuProductRequest(createProduct(
                                                baseUrl + "/products", "Coupon product 4 " + suffix,
                                                "Fourth product used to verify coupon availability", "100", "13", "8", "5"
                                        ), new BigDecimal("1.00"), null, 10),
                                        new ProposedWeekMenuProductRequest(createProduct(
                                                baseUrl + "/products", "Coupon product 5 " + suffix,
                                                "Fifth product used to verify coupon availability", "100", "14", "9", "6"
                                        ), new BigDecimal("1.00"), null, 20),
                                        new ProposedWeekMenuProductRequest(createProduct(
                                                baseUrl + "/products", "Coupon product 6 " + suffix,
                                                "Sixth product used to verify coupon availability", "100", "15", "10", "7"
                                        ), new BigDecimal("1.00"), null, 30)
                                )
                        ))
                )),
                ProposedWeekMenuResponse.class
        );

        ResponseEntity<PlanningCouponResponse[]> beforeUse = getAuthorized(
                baseUrl + "/planning/" + planning.getBody().id() + "/coupons?payerUserId=" + authenticatedUserId(),
                PlanningCouponResponse[].class
        );
        assertThat(beforeUse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(beforeUse.getBody()).extracting(PlanningCouponResponse::code)
                .doesNotContain("SUSHI", "INOVACION");
        assertThat(findCoupon(beforeUse.getBody(), "NO_REPEATED_PRODUCTS")).satisfies(coupon -> {
            assertThat(coupon.code()).isEqualTo("NO_REPEATED_PRODUCTS");
            assertThat(coupon.conditionDescription()).isNotBlank();
            assertThat(coupon.conditionMet()).isTrue();
            assertThat(coupon.available()).isTrue();
            assertThat(coupon.usedRecently()).isFalse();
            assertThat(coupon.informativeAvailabilityState()).isEqualTo(com.eliascanalesnieto.foodhelper.presentation.PlanningCouponAvailabilityState.AVAILABLE);
            assertThat(coupon.lastUsedAt()).isNull();
            assertThat(coupon.nextAvailableAt()).isNull();
            assertThat(coupon.unavailableReasons()).isEmpty();
        });

        BigDecimal balanceBefore = getAuthorized(
                baseUrl + "/users/" + authenticatedUserId() + "/money-box",
                UserMoneyBoxResponse.class
        ).getBody().balance();

        ResponseEntity<CurrentWeekMenuResponse> established = postAuthorized(
                baseUrl + "/planning/" + planning.getBody().id() + "/menu",
                new EstablishProposedWeekMenuRequest(authenticatedUserId(), null, List.of("NO_REPEATED_PRODUCTS")),
                CurrentWeekMenuResponse.class
        );
        assertThat(established.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        assertThat(getAuthorized(
                baseUrl + "/users/" + authenticatedUserId() + "/money-box",
                UserMoneyBoxResponse.class
        ).getBody().balance()).isEqualByComparingTo(balanceBefore.add(new BigDecimal("15.00")));

        ResponseEntity<PlanningCouponResponse[]> afterUse = getAuthorized(
                baseUrl + "/planning/" + planning.getBody().id() + "/coupons?payerUserId=" + authenticatedUserId(),
                PlanningCouponResponse[].class
        );
        assertThat(afterUse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(afterUse.getBody()).extracting(PlanningCouponResponse::code)
                .doesNotContain("SUSHI", "INOVACION");
        assertThat(findCoupon(afterUse.getBody(), "NO_REPEATED_PRODUCTS")).satisfies(coupon -> {
            assertThat(coupon.conditionMet()).isTrue();
            assertThat(coupon.available()).isFalse();
            assertThat(coupon.usedRecently()).isTrue();
            assertThat(coupon.informativeAvailabilityState()).isEqualTo(com.eliascanalesnieto.foodhelper.presentation.PlanningCouponAvailabilityState.USED_RECENTLY);
            assertThat(coupon.lastUsedAt()).isNotNull();
            assertThat(coupon.nextAvailableAt()).isEqualTo(coupon.lastUsedAt().plus(30, ChronoUnit.DAYS));
            assertThat(coupon.unavailableReasons()).contains(PlanningCouponUnavailabilityReason.USED_WITHIN_PERIOD);
        });
    }

    @Test
    void globalCouponCatalogShouldBeFilteredByPayerAndAllowOnlyAvailableFiltering() {
        String baseUrl = "http://localhost:" + port + "/api/v1";
        String suffix = Long.toString(System.nanoTime());

        AuthResponse otherUser = restTemplate.postForEntity(
                baseUrl + "/auth/register",
                new RegisterRequest("coupon-filter-user-" + suffix, "secret-password", REGISTRATION_CODE),
                AuthResponse.class
        ).getBody();
        assertThat(otherUser).isNotNull();

        Long ingredientId = createProduct(
                baseUrl + "/products", "Global coupon ingredient " + suffix,
                "Ingredient used to validate global coupon availability", "100", "10", "5", "2"
        );
        Long dayPartId = createDayPart(baseUrl + "/planning/day-parts", "Global coupon lunch " + suffix, "Lunch", 1100);
        Long secondProductId = createProduct(
                baseUrl + "/products", "Global coupon ingredient 2 " + suffix,
                "Second ingredient used to validate global coupon availability", "100", "11", "6", "3"
        );
        Long thirdProductId = createProduct(
                baseUrl + "/products", "Global coupon ingredient 3 " + suffix,
                "Third ingredient used to validate global coupon availability", "100", "12", "7", "4"
        );

        ResponseEntity<ProposedWeekMenuResponse> planning = postAuthorized(
                baseUrl + "/planning",
                new CreateProposedWeekMenuRequest(LocalDate.of(2031, 4, 1), LocalDate.of(2031, 4, 1)),
                ProposedWeekMenuResponse.class
        );
        assertThat(planning.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        restTemplate.exchange(
                baseUrl + "/planning/" + planning.getBody().id() + "/days",
                HttpMethod.PUT,
                authorizedEntity(new UpsertProposedWeekMenuDayRequest(
                        LocalDate.of(2031, 4, 1),
                        List.of(new ProposedWeekMenuSectionRequest(
                                dayPartId,
                                List.of(
                                        new ProposedWeekMenuProductRequest(ingredientId, new BigDecimal("1.00"), null, 10),
                                        new ProposedWeekMenuProductRequest(secondProductId, new BigDecimal("1.00"), null, 20),
                                        new ProposedWeekMenuProductRequest(thirdProductId, new BigDecimal("1.00"), null, 30)
                                )
                        ))
                )),
                ProposedWeekMenuResponse.class
        );

        ResponseEntity<PlanningCouponResponse[]> validated = getAuthorized(
                baseUrl + "/planning/" + planning.getBody().id() + "/coupons?payerUserId=" + authenticatedUserId(),
                PlanningCouponResponse[].class
        );
        assertThat(validated.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(validated.getBody()).isNotNull();
        assertThat(validated.getBody()).isNotEmpty();
        assertThat(findCoupon(validated.getBody(), "NO_REPEATED_PRODUCTS").available()).isTrue();

        ResponseEntity<PlanningCouponResponse[]> validation = postAuthorized(
                baseUrl + "/planning/" + planning.getBody().id() + "/coupons/validate",
                new ValidateProposedWeekMenuCouponsRequest(
                        authenticatedUserId(),
                        List.of("NO_REPEATED_PRODUCTS")
                ),
                PlanningCouponResponse[].class
        );
        assertThat(validation.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(validation.getBody()).isNotNull();
        assertThat(validation.getBody()).hasSize(1);

        ResponseEntity<CurrentWeekMenuResponse> established = postAuthorized(
                baseUrl + "/planning/" + planning.getBody().id() + "/menu",
                new EstablishProposedWeekMenuRequest(
                        authenticatedUserId(),
                        null,
                        List.of("NO_REPEATED_PRODUCTS")
                ),
                CurrentWeekMenuResponse.class
        );
        assertThat(established.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        Instant redeemedAt = Instant.now();
        for (String couponCode : List.of("INOVACION", "VINTAGE", "OUTSIDE", "CAPRICHO", "LUXURY", "SUSHI")) {
            jdbcTemplate.update(
                    """
                            INSERT INTO planning_coupon_redemptions (user_id, coupon_code, planning_id, current_week_menu_id, reward_amount, used_at)
                            VALUES (?, ?, ?, ?, ?, ?)
                            """,
                    authenticatedUserId(),
                    couponCode,
                    planning.getBody().id(),
                    established.getBody().id(),
                    new BigDecimal("1.00"),
                    java.sql.Timestamp.from(redeemedAt)
            );
        }

        ResponseEntity<CouponResponse[]> afterUse = getAuthorized(
                baseUrl + "/coupons?payerUserId=" + authenticatedUserId(),
                CouponResponse[].class
        );
        assertThat(afterUse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(afterUse.getBody()).isNotNull();
        assertThat(afterUse.getBody()).hasSize(7);
        assertThat(findCoupon(afterUse.getBody(), "NO_REPEATED_PRODUCTS").available()).isFalse();
        assertThat(findCoupon(afterUse.getBody(), "NO_REPEATED_PRODUCTS").unavailableReasons())
                .contains(PlanningCouponUnavailabilityReason.USED_WITHIN_PERIOD);

        ResponseEntity<CouponResponse[]> onlyAvailableAfterUse = getAuthorized(
                baseUrl + "/coupons?payerUserId=" + authenticatedUserId() + "&onlyAvailable=true",
                CouponResponse[].class
        );
        assertThat(onlyAvailableAfterUse.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(onlyAvailableAfterUse.getBody()).isNotNull();
        assertThat(onlyAvailableAfterUse.getBody()).isEmpty();

        ResponseEntity<CouponResponse[]> otherUserCoupons = getAuthorized(
                baseUrl + "/coupons?payerUserId=" + otherUser.userId(),
                CouponResponse[].class
        );
        assertThat(otherUserCoupons.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(otherUserCoupons.getBody()).isNotNull();
        assertThat(findCoupon(otherUserCoupons.getBody(), "NO_REPEATED_PRODUCTS").available()).isTrue();
    }

    private PlanningCouponResponse findCoupon(PlanningCouponResponse[] coupons, String code) {
        return Arrays.stream(coupons)
                .filter(coupon -> coupon.code().equals(code))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Coupon not found: " + code));
    }

    private CouponResponse findCoupon(CouponResponse[] coupons, String code) {
        return Arrays.stream(coupons)
                .filter(coupon -> coupon.code().equals(code))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Coupon not found: " + code));
    }

    @Test
    void weekStockShouldReduceTheShoppingListAndTransferBackToProductStockWhenClosing() {
        String productsUrl = "http://localhost:" + port + "/api/v1/products";
        String dayPartsUrl = "http://localhost:" + port + "/api/v1/planning/day-parts";
        String proposedMenusUrl = "http://localhost:" + port + "/api/v1/planning";
        String menusUrl = "http://localhost:" + port + "/api/v1/menus";
        Long riceId = createProduct(productsUrl, "Week Stock Rice", "Rice", "100", "0", "2.7", "0.3");
        Long lunchDayPartId = createDayPart(dayPartsUrl, "Lunch week stock", "Main meal of the day", 10);

        ResponseEntity<ProposedWeekMenuResponse> planning = postAuthorized(
                proposedMenusUrl,
                new CreateProposedWeekMenuRequest(LocalDate.of(2026, 6, 15), LocalDate.of(2026, 6, 21)),
                ProposedWeekMenuResponse.class
        );
        assertThat(planning.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<ProposedWeekMenuResponse> plannedMenu = restTemplate.exchange(
                proposedMenusUrl + "/" + planning.getBody().id() + "/days",
                HttpMethod.PUT,
                authorizedEntity(new UpsertProposedWeekMenuDayRequest(
                        LocalDate.of(2026, 6, 15),
                        List.of(
                                new ProposedWeekMenuSectionRequest(
                                        lunchDayPartId,
                                        List.of(new ProposedWeekMenuProductRequest(riceId, new BigDecimal("2.00"), null, 10))
                                )
                        )
                )),
                ProposedWeekMenuResponse.class
        );
        assertThat(plannedMenu.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(plannedMenu.getBody()).isNotNull();

        ResponseEntity<CurrentWeekMenuResponse> established = postAuthorized(
                proposedMenusUrl + "/" + planning.getBody().id() + "/menu",
                new EstablishProposedWeekMenuRequest(authenticatedUserId()),
                CurrentWeekMenuResponse.class
        );
        assertThat(established.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(established.getBody()).isNotNull();
        assertThat(established.getBody().weekStock()).isEmpty();
        assertThat(established.getBody().shoppingList()).singleElement().satisfies(item ->
                assertThat(item.missingUnits()).isEqualByComparingTo("2.00")
        );

        ResponseEntity<CurrentWeekMenuResponse> updated = restTemplate.exchange(
                menusUrl + "/" + established.getBody().id() + "/week-stock",
                HttpMethod.PUT,
                authorizedEntity(new UpdateCurrentWeekMenuStockRequest(List.of(
                        new CurrentWeekMenuStockItemRequest(riceId, new BigDecimal("1.00"), new BigDecimal("2.10")),
                        new CurrentWeekMenuStockItemRequest(riceId, new BigDecimal("0.50"), new BigDecimal("1.80"))
                ))),
                CurrentWeekMenuResponse.class
        );
        assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updated.getBody()).isNotNull();
        assertThat(updated.getBody().weekStock()).hasSize(2);
        assertThat(updated.getBody().weekStock()).extracting(
                CurrentWeekMenuStockItemResponse::productId,
                CurrentWeekMenuStockItemResponse::quantity,
                CurrentWeekMenuStockItemResponse::price
        ).containsExactly(
                tuple(riceId, new BigDecimal("1.00"), new BigDecimal("2.10")),
                tuple(riceId, new BigDecimal("0.50"), new BigDecimal("1.80"))
        );
        assertThat(updated.getBody().shoppingList()).singleElement().satisfies(item ->
                assertThat(item.missingUnits()).isEqualByComparingTo("0.50")
        );

        ResponseEntity<CurrentWeekMenuStockItemResponse[]> weekStock = getAuthorized(
                menusUrl + "/" + established.getBody().id() + "/week-stock",
                CurrentWeekMenuStockItemResponse[].class
        );
        assertThat(weekStock.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(weekStock.getBody()).isNotNull();
        assertThat(weekStock.getBody()).hasSize(2);
        assertThat(weekStock.getBody()[0].quantity()).isEqualByComparingTo("1.00");
        assertThat(weekStock.getBody()[0].price()).isEqualByComparingTo("2.10");
        assertThat(weekStock.getBody()[1].quantity()).isEqualByComparingTo("0.50");
        assertThat(weekStock.getBody()[1].price()).isEqualByComparingTo("1.80");

        ResponseEntity<CurrentWeekMenuStatsResponse> closed = postAuthorized(
                menusUrl + "/" + established.getBody().id() + "/close",
                new CloseCurrentWeekMenuRequest(List.of(authenticatedUserId())),
                CurrentWeekMenuStatsResponse.class
        );
        assertThat(closed.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<StockEntryResponse[]> riceStock = getAuthorized(
                productsUrl + "/" + riceId + "/stock",
                StockEntryResponse[].class
        );
        assertThat(riceStock.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(riceStock.getBody()).hasSize(2);
        assertThat(riceStock.getBody()[0].quantity()).isEqualByComparingTo("1.00");
        assertThat(riceStock.getBody()[0].price()).isEqualByComparingTo("2.10");
        assertThat(riceStock.getBody()[1].quantity()).isEqualByComparingTo("0.50");
        assertThat(riceStock.getBody()[1].price()).isEqualByComparingTo("1.80");
    }

    @Test
    void closeSummaryShouldExposeTransferableWeekStockAndMoneyMovements() {
        String productsUrl = "http://localhost:" + port + "/api/v1/products";
        String dayPartsUrl = "http://localhost:" + port + "/api/v1/planning/day-parts";
        String proposedMenusUrl = "http://localhost:" + port + "/api/v1/planning";
        String menusUrl = "http://localhost:" + port + "/api/v1/menus";
        Long riceId = createProduct(productsUrl, "Close Summary Rice", "Rice", "100", "0", "2.7", "0.3");
        Long beansId = createProduct(productsUrl, "Close Summary Beans", "Beans", "100", "22", "8", "1.2");
        Long carrotId = createProduct(productsUrl, "Close Summary Carrot", "Carrot", "41", "10", "1", "0.2");
        Long lunchDayPartId = createDayPart(dayPartsUrl, "Close summary lunch", "Main meal of the day", 10);

        ResponseEntity<ProposedWeekMenuResponse> planning = postAuthorized(
                proposedMenusUrl,
                new CreateProposedWeekMenuRequest(LocalDate.of(2026, 6, 15), LocalDate.of(2026, 6, 15)),
                ProposedWeekMenuResponse.class
        );
        assertThat(planning.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<ProposedWeekMenuResponse> plannedMenu = restTemplate.exchange(
                proposedMenusUrl + "/" + planning.getBody().id() + "/days",
                HttpMethod.PUT,
                authorizedEntity(new UpsertProposedWeekMenuDayRequest(
                        LocalDate.of(2026, 6, 15),
                        List.of(
                                new ProposedWeekMenuSectionRequest(
                                        lunchDayPartId,
                                        List.of(
                                                new ProposedWeekMenuProductRequest(riceId, new BigDecimal("1.00"), null, 10),
                                                new ProposedWeekMenuProductRequest(beansId, new BigDecimal("1.00"), null, 20),
                                                new ProposedWeekMenuProductRequest(carrotId, new BigDecimal("1.00"), null, 30)
                                        )
                                )
                        )
                )),
                ProposedWeekMenuResponse.class
        );
        assertThat(plannedMenu.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<StockEntryResponse> riceStock = postAuthorized(
                productsUrl + "/" + riceId + "/stock",
                new CreateStockEntryRequest(
                        new BigDecimal("1.00"),
                        new BigDecimal("1.25"),
                        null,
                        LocalDate.of(2026, 6, 10)
                ),
                StockEntryResponse.class
        );
        assertThat(riceStock.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<CurrentWeekMenuResponse> established = postAuthorized(
                proposedMenusUrl + "/" + planning.getBody().id() + "/menu",
                new EstablishProposedWeekMenuRequest(
                        authenticatedUserId(),
                        null,
                        List.of("NO_REPEATED_PRODUCTS")
                ),
                CurrentWeekMenuResponse.class
        );
        assertThat(established.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(established.getBody()).isNotNull();

        ResponseEntity<CurrentWeekMenuResponse> updated = restTemplate.exchange(
                menusUrl + "/" + established.getBody().id() + "/week-stock",
                HttpMethod.PUT,
                authorizedEntity(new UpdateCurrentWeekMenuStockRequest(List.of(
                        new CurrentWeekMenuStockItemRequest(beansId, new BigDecimal("0.50"), new BigDecimal("2.10"))
                ))),
                CurrentWeekMenuResponse.class
        );
        assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updated.getBody()).isNotNull();
        assertThat(updated.getBody().weekStock()).singleElement().satisfies(item ->
                assertThat(item.quantity()).isEqualByComparingTo("0.50")
        );

        ResponseEntity<CurrentWeekMenuCloseSummaryResponse> summary = getAuthorized(
                menusUrl + "/" + established.getBody().id() + "/close/summary",
                CurrentWeekMenuCloseSummaryResponse.class
        );
        assertThat(summary.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(summary.getBody()).isNotNull();
        assertThat(summary.getBody().menuId()).isEqualTo(established.getBody().id());
        assertThat(summary.getBody().transferableWeekStock()).singleElement().satisfies(item -> {
            assertThat(item.productId()).isEqualTo(beansId);
            assertThat(item.quantity()).isEqualByComparingTo("0.50");
            assertThat(item.price()).isEqualByComparingTo("2.10");
        });
        assertThat(summary.getBody().transferableWeekStockValue()).isEqualByComparingTo("1.05");
        assertThat(summary.getBody().couponRewards()).isEqualByComparingTo("15.00");
        assertThat(summary.getBody().menuExpense()).isEqualByComparingTo("1.25");
        assertThat(summary.getBody().netMoneyImpact()).isEqualByComparingTo("13.75");
        assertThat(summary.getBody().moneyMovements()).hasSize(2);
        assertThat(summary.getBody().moneyMovements()).extracting(UserMoneyMovementResponse::amount)
                .anySatisfy(amount -> assertThat(amount).isPositive())
                .anySatisfy(amount -> assertThat(amount).isNegative());

        ResponseEntity<CurrentWeekMenuStatsResponse> closed = postAuthorized(
                menusUrl + "/" + established.getBody().id() + "/close",
                new CloseCurrentWeekMenuRequest(List.of(authenticatedUserId()), false),
                CurrentWeekMenuStatsResponse.class
        );
        assertThat(closed.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<StockEntryResponse[]> beansStock = getAuthorized(
                productsUrl + "/" + beansId + "/stock",
                StockEntryResponse[].class
        );
        assertThat(beansStock.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(beansStock.getBody()).isEmpty();
    }

    @Test
    void transferFromGlobalStockShouldReduceInventoryAndApplyToWeekStock() {
        String productsUrl = "http://localhost:" + port + "/api/v1/products";
        String dayPartsUrl = "http://localhost:" + port + "/api/v1/planning/day-parts";
        String proposedMenusUrl = "http://localhost:" + port + "/api/v1/planning";
        String menusUrl = "http://localhost:" + port + "/api/v1/menus";
        Long riceId = createProduct(productsUrl, "Transfer Rice", "Rice", "100", "0", "2.7", "0.3");
        Long lunchDayPartId = createDayPart(dayPartsUrl, "Transfer lunch", "Main meal of the day", 20);

        ResponseEntity<ProposedWeekMenuResponse> planning = postAuthorized(
                proposedMenusUrl,
                new CreateProposedWeekMenuRequest(LocalDate.of(2026, 6, 15), LocalDate.of(2026, 6, 21)),
                ProposedWeekMenuResponse.class
        );
        assertThat(planning.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<ProposedWeekMenuResponse> plannedMenu = restTemplate.exchange(
                proposedMenusUrl + "/" + planning.getBody().id() + "/days",
                HttpMethod.PUT,
                authorizedEntity(new UpsertProposedWeekMenuDayRequest(
                        LocalDate.of(2026, 6, 15),
                        List.of(
                                new ProposedWeekMenuSectionRequest(
                                        lunchDayPartId,
                                        List.of(new ProposedWeekMenuProductRequest(riceId, new BigDecimal("2.00"), null, 10))
                                )
                        )
                )),
                ProposedWeekMenuResponse.class
        );
        assertThat(plannedMenu.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<CurrentWeekMenuResponse> established = postAuthorized(
                proposedMenusUrl + "/" + planning.getBody().id() + "/menu",
                new EstablishProposedWeekMenuRequest(authenticatedUserId()),
                CurrentWeekMenuResponse.class
        );
        assertThat(established.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(established.getBody()).isNotNull();
        assertThat(established.getBody().shoppingList()).singleElement().satisfies(item ->
                assertThat(item.missingUnits()).isEqualByComparingTo("2.00")
        );

        ResponseEntity<StockEntryResponse> riceStock = postAuthorized(
                productsUrl + "/" + riceId + "/stock",
                new CreateStockEntryRequest(
                        new BigDecimal("1.00"),
                        new BigDecimal("1.25"),
                        null,
                        LocalDate.of(2026, 6, 10)
                ),
                StockEntryResponse.class
        );
        ResponseEntity<StockEntryResponse> riceStockSecond = postAuthorized(
                productsUrl + "/" + riceId + "/stock",
                new CreateStockEntryRequest(
                        new BigDecimal("1.00"),
                        new BigDecimal("1.80"),
                        null,
                        LocalDate.of(2026, 6, 11)
                ),
                StockEntryResponse.class
        );
        assertThat(riceStock.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(riceStock.getBody()).isNotNull();
        assertThat(riceStockSecond.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(riceStockSecond.getBody()).isNotNull();

        ResponseEntity<CurrentWeekMenuResponse> transferred = restTemplate.exchange(
                menusUrl + "/" + established.getBody().id() + "/week-stock/transfer",
                HttpMethod.POST,
                authorizedEntity(new CreateMenuStockTransferRequest(
                        riceStock.getBody().id(),
                        new BigDecimal("1.00")
                )),
                CurrentWeekMenuResponse.class
        );
        ResponseEntity<CurrentWeekMenuResponse> transferredSecond = restTemplate.exchange(
                menusUrl + "/" + established.getBody().id() + "/week-stock/transfer",
                HttpMethod.POST,
                authorizedEntity(new CreateMenuStockTransferRequest(
                        riceStockSecond.getBody().id(),
                        new BigDecimal("1.00")
                )),
                CurrentWeekMenuResponse.class
        );
        assertThat(transferred.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(transferred.getBody()).isNotNull();
        assertThat(transferredSecond.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(transferredSecond.getBody()).isNotNull();
        assertThat(transferredSecond.getBody().weekStock()).hasSize(2);
        assertThat(transferredSecond.getBody().weekStock()).extracting(
                CurrentWeekMenuStockItemResponse::productId,
                CurrentWeekMenuStockItemResponse::quantity,
                CurrentWeekMenuStockItemResponse::price
        ).containsExactly(
                tuple(riceId, new BigDecimal("1.00"), new BigDecimal("1.25")),
                tuple(riceId, new BigDecimal("1.00"), new BigDecimal("1.80"))
        );
        assertThat(transferredSecond.getBody().shoppingList()).isEmpty();

        ResponseEntity<StockEntryResponse[]> remainingStock = getAuthorized(
                productsUrl + "/" + riceId + "/stock",
                StockEntryResponse[].class
        );
        assertThat(remainingStock.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(remainingStock.getBody()).isEmpty();
    }

    @Test
    void shoppingListFilteringShouldKeepUnassignedProductsForEverySupermarket() {
        String productsUrl = "http://localhost:" + port + "/api/v1/products";
        String dayPartsUrl = "http://localhost:" + port + "/api/v1/planning/day-parts";
        String proposedMenusUrl = "http://localhost:" + port + "/api/v1/planning";
        String menusUrl = "http://localhost:" + port + "/api/v1/menus";
        String supermarketsUrl = "http://localhost:" + port + "/api/v1/supermarkets";
        Long supermarketAId = createSupermarket(supermarketsUrl, "Filter Market A");
        Long supermarketBId = createSupermarket(supermarketsUrl, "Filter Market B");
        Long chickenId = createProduct(productsUrl, "Filter Chicken", "Chicken", "200", "0", "31", "3.6", "2.00", List.of(supermarketAId));
        Long beansId = createProduct(productsUrl, "Filter Beans", "Beans", "100", "22", "8", "1.2", "1.50", List.of(supermarketBId));
        Long saltId = createProduct(productsUrl, "Filter Salt", "Salt", "0", "0", "0", "0", "1.00");
        Long lunchDayPartId = createDayPart(dayPartsUrl, "Lunch filter", "Main meal of the day", 10);

        ResponseEntity<ProposedWeekMenuResponse> planning = postAuthorized(
                proposedMenusUrl,
                new CreateProposedWeekMenuRequest(LocalDate.of(2026, 6, 15), LocalDate.of(2026, 6, 21)),
                ProposedWeekMenuResponse.class
        );
        assertThat(planning.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<ProposedWeekMenuResponse> plannedMenu = restTemplate.exchange(
                proposedMenusUrl + "/" + planning.getBody().id() + "/days",
                HttpMethod.PUT,
                authorizedEntity(new UpsertProposedWeekMenuDayRequest(
                        LocalDate.of(2026, 6, 15),
                        List.of(
                                new ProposedWeekMenuSectionRequest(
                                        lunchDayPartId,
                                        List.of(
                                                new ProposedWeekMenuProductRequest(chickenId, new BigDecimal("1.00"), null, 10),
                                                new ProposedWeekMenuProductRequest(beansId, new BigDecimal("1.00"), null, 20),
                                                new ProposedWeekMenuProductRequest(saltId, new BigDecimal("1.00"), null, 30)
                                        )
                                )
                        )
                )),
                ProposedWeekMenuResponse.class
        );
        assertThat(plannedMenu.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(plannedMenu.getBody()).isNotNull();
        assertThat(plannedMenu.getBody().days().getFirst().sections().getFirst().products().getFirst().nutritionalValues().calories())
                .isEqualByComparingTo("4.00");

        ResponseEntity<CurrentWeekMenuResponse> established = postAuthorized(
                proposedMenusUrl + "/" + planning.getBody().id() + "/menu",
                new EstablishProposedWeekMenuRequest(authenticatedUserId()),
                CurrentWeekMenuResponse.class
        );
        assertThat(established.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        ResponseEntity<CurrentWeekMenuShoppingListItemResponse[]> supermarketAList = getAuthorized(
                menusUrl + "/" + established.getBody().id() + "/shopping-list?supermarketId=" + supermarketAId,
                CurrentWeekMenuShoppingListItemResponse[].class
        );
        assertThat(supermarketAList.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(supermarketAList.getBody()).extracting(CurrentWeekMenuShoppingListItemResponse::productId)
                .containsExactlyInAnyOrder(chickenId, saltId);

        ResponseEntity<CurrentWeekMenuShoppingListItemResponse[]> supermarketBList = getAuthorized(
                menusUrl + "/" + established.getBody().id() + "/shopping-list?supermarketId=" + supermarketBId,
                CurrentWeekMenuShoppingListItemResponse[].class
        );
        assertThat(supermarketBList.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(supermarketBList.getBody()).extracting(CurrentWeekMenuShoppingListItemResponse::productId)
                .containsExactlyInAnyOrder(beansId, saltId);
    }

    @Test
    void establishedMenuShoppingListShouldExpandCompositionProductsIntoIngredients() {
        String productsUrl = "http://localhost:" + port + "/api/v1/products";
        String recipesUrl = "http://localhost:" + port + "/api/v1/recipes";
        String dayPartsUrl = "http://localhost:" + port + "/api/v1/planning/day-parts";
        String proposedMenusUrl = "http://localhost:" + port + "/api/v1/planning";
        String menusUrl = "http://localhost:" + port + "/api/v1/menus";
        String suffix = Long.toString(System.nanoTime());
        Long chickenId = createProduct(productsUrl, "Menu Chicken " + suffix, "Chicken", "165", "0", "31", "3.6");
        Long beansId = createProduct(productsUrl, "Menu Beans " + suffix, "Beans", "347", "63", "21", "1.2");
        Long lunchDayPartId = createDayPart(dayPartsUrl, "Lunch menu " + suffix, "Main meal of the day", 10);

        ResponseEntity<RecipeResponse> recipe = postAuthorized(
                recipesUrl,
                new CreateRecipeRequest(
                        "Menu Curry " + suffix,
                        "Derived menu product " + suffix,
                        "Cook slowly until ready.",
                        List.of(
                                new RecipeIngredientAssignmentRequest(chickenId, new BigDecimal("200")),
                                new RecipeIngredientAssignmentRequest(beansId, new BigDecimal("100"))
                        )
                ),
                RecipeResponse.class
        );
        assertThat(recipe.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(recipe.getBody()).isNotNull();

        ResponseEntity<RecipeDerivedProductResponse> derivedProduct = postAuthorized(
                recipesUrl + "/" + recipe.getBody().id() + "/derived-product",
                new CreateRecipeDerivedProductRequest("Menu curry base " + suffix, new BigDecimal("4")),
                RecipeDerivedProductResponse.class
        );
        assertThat(derivedProduct.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(derivedProduct.getBody()).isNotNull();

        ResponseEntity<ProposedWeekMenuResponse> planning = postAuthorized(
                proposedMenusUrl,
                new CreateProposedWeekMenuRequest(LocalDate.of(2026, 6, 15), LocalDate.of(2026, 6, 21)),
                ProposedWeekMenuResponse.class
        );
        assertThat(planning.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(planning.getBody()).isNotNull();

        ResponseEntity<ProposedWeekMenuResponse> plannedMenu = restTemplate.exchange(
                proposedMenusUrl + "/" + planning.getBody().id() + "/days",
                HttpMethod.PUT,
                authorizedEntity(new UpsertProposedWeekMenuDayRequest(
                        LocalDate.of(2026, 6, 15),
                        List.of(
                                new ProposedWeekMenuSectionRequest(
                                        lunchDayPartId,
                                        List.of(new ProposedWeekMenuProductRequest(
                                                derivedProduct.getBody().productId(),
                                                new BigDecimal("1.00"),
                                                null,
                                                10
                                        ))
                                )
                        )
                )),
                ProposedWeekMenuResponse.class
        );
        assertThat(plannedMenu.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<CurrentWeekMenuResponse> established = postAuthorized(
                proposedMenusUrl + "/" + planning.getBody().id() + "/menu",
                new EstablishProposedWeekMenuRequest(authenticatedUserId()),
                CurrentWeekMenuResponse.class
        );
        assertThat(established.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(established.getBody()).isNotNull();
        assertThat(established.getBody().shoppingList()).extracting(CurrentWeekMenuShoppingListItemResponse::productId)
                .containsExactlyInAnyOrder(chickenId, beansId);
        assertThat(established.getBody().shoppingList()).extracting(CurrentWeekMenuShoppingListItemResponse::productName)
                .containsExactlyInAnyOrder("Menu Chicken " + suffix, "Menu Beans " + suffix);
    }

    @Test
    void legacyMenuShoppingListSnapshotsShouldBeNormalizedOnRead() throws Exception {
        String productsUrl = "http://localhost:" + port + "/api/v1/products";
        String recipesUrl = "http://localhost:" + port + "/api/v1/recipes";
        String dayPartsUrl = "http://localhost:" + port + "/api/v1/planning/day-parts";
        String proposedMenusUrl = "http://localhost:" + port + "/api/v1/planning";
        String menusUrl = "http://localhost:" + port + "/api/v1/menus";
        String suffix = Long.toString(System.nanoTime());
        Long chickenId = createProduct(productsUrl, "Legacy Chicken " + suffix, "Chicken", "165", "0", "31", "3.6");
        Long beansId = createProduct(productsUrl, "Legacy Beans " + suffix, "Beans", "347", "63", "21", "1.2");
        Long lunchDayPartId = createDayPart(dayPartsUrl, "Lunch legacy " + suffix, "Main meal of the day", 10);

        ResponseEntity<RecipeResponse> recipe = postAuthorized(
                recipesUrl,
                new CreateRecipeRequest(
                        "Legacy Curry " + suffix,
                        "Derived menu product " + suffix,
                        "Cook slowly until ready.",
                        List.of(
                                new RecipeIngredientAssignmentRequest(chickenId, new BigDecimal("200")),
                                new RecipeIngredientAssignmentRequest(beansId, new BigDecimal("100"))
                        )
                ),
                RecipeResponse.class
        );
        assertThat(recipe.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(recipe.getBody()).isNotNull();

        ResponseEntity<RecipeDerivedProductResponse> derivedProduct = postAuthorized(
                recipesUrl + "/" + recipe.getBody().id() + "/derived-product",
                new CreateRecipeDerivedProductRequest("Legacy curry base " + suffix, new BigDecimal("4")),
                RecipeDerivedProductResponse.class
        );
        assertThat(derivedProduct.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(derivedProduct.getBody()).isNotNull();

        ResponseEntity<ProposedWeekMenuResponse> planning = postAuthorized(
                proposedMenusUrl,
                new CreateProposedWeekMenuRequest(LocalDate.of(2026, 6, 15), LocalDate.of(2026, 6, 21)),
                ProposedWeekMenuResponse.class
        );
        assertThat(planning.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(planning.getBody()).isNotNull();

        ResponseEntity<ProposedWeekMenuResponse> plannedMenu = restTemplate.exchange(
                proposedMenusUrl + "/" + planning.getBody().id() + "/days",
                HttpMethod.PUT,
                authorizedEntity(new UpsertProposedWeekMenuDayRequest(
                        LocalDate.of(2026, 6, 15),
                        List.of(
                                new ProposedWeekMenuSectionRequest(
                                        lunchDayPartId,
                                        List.of(new ProposedWeekMenuProductRequest(
                                                derivedProduct.getBody().productId(),
                                                new BigDecimal("1.00"),
                                                null,
                                                10
                                        ))
                                )
                        )
                )),
                ProposedWeekMenuResponse.class
        );
        assertThat(plannedMenu.getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<CurrentWeekMenuResponse> established = postAuthorized(
                proposedMenusUrl + "/" + planning.getBody().id() + "/menu",
                new EstablishProposedWeekMenuRequest(authenticatedUserId()),
                CurrentWeekMenuResponse.class
        );
        assertThat(established.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(established.getBody()).isNotNull();

        tools.jackson.databind.node.ObjectNode snapshot = (tools.jackson.databind.node.ObjectNode) objectMapper.readTree(
                jdbcTemplate.queryForObject(
                        "SELECT snapshot_json FROM current_week_menus WHERE id = ?",
                        String.class,
                        established.getBody().id()
                )
        );
        tools.jackson.databind.node.ArrayNode legacyShoppingList = objectMapper.createArrayNode();
        legacyShoppingList.addObject()
                .put("productId", derivedProduct.getBody().productId())
                .put("productName", derivedProduct.getBody().name())
                .put("missingUnits", 1.00);
        snapshot.set("shoppingList", legacyShoppingList);
        jdbcTemplate.update(
                "UPDATE current_week_menus SET snapshot_json = ? WHERE id = ?",
                objectMapper.writeValueAsString(snapshot),
                established.getBody().id()
        );

        ResponseEntity<CurrentWeekMenuResponse> reloaded = getAuthorized(
                menusUrl + "/" + established.getBody().id(),
                CurrentWeekMenuResponse.class
        );
        assertThat(reloaded.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(reloaded.getBody()).isNotNull();
        assertThat(reloaded.getBody().shoppingList()).extracting(CurrentWeekMenuShoppingListItemResponse::productId)
                .containsExactlyInAnyOrder(chickenId, beansId);
        assertThat(reloaded.getBody().shoppingList()).extracting(CurrentWeekMenuShoppingListItemResponse::productName)
                .containsExactlyInAnyOrder("Legacy Chicken " + suffix, "Legacy Beans " + suffix);

        ResponseEntity<CurrentWeekMenuShoppingListItemResponse[]> shoppingList = getAuthorized(
                menusUrl + "/" + established.getBody().id() + "/shopping-list",
                CurrentWeekMenuShoppingListItemResponse[].class
        );
        assertThat(shoppingList.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(shoppingList.getBody()).isNotNull();
        assertThat(shoppingList.getBody()).extracting(CurrentWeekMenuShoppingListItemResponse::productId)
                .containsExactlyInAnyOrder(chickenId, beansId);
    }

    @Test
    void menusShouldBePagedAndFilteredByState() {
        String proposedMenusUrl = "http://localhost:" + port + "/api/v1/planning";
        String menusUrl = "http://localhost:" + port + "/api/v1/menus";
        Long userId = authenticatedUserId();

        ResponseEntity<ProposedWeekMenuResponse> oldestPlanning = postAuthorized(
                proposedMenusUrl,
                new CreateProposedWeekMenuRequest(LocalDate.of(2025, 5, 25), LocalDate.of(2025, 5, 31)),
                ProposedWeekMenuResponse.class
        );
        ResponseEntity<ProposedWeekMenuResponse> middlePlanning = postAuthorized(
                proposedMenusUrl,
                new CreateProposedWeekMenuRequest(LocalDate.of(2025, 6, 1), LocalDate.of(2025, 6, 7)),
                ProposedWeekMenuResponse.class
        );
        ResponseEntity<ProposedWeekMenuResponse> newestPlanning = postAuthorized(
                proposedMenusUrl,
                new CreateProposedWeekMenuRequest(LocalDate.of(2025, 6, 8), LocalDate.of(2025, 6, 14)),
                ProposedWeekMenuResponse.class
        );

        ResponseEntity<CurrentWeekMenuResponse> oldestMenu = postAuthorized(
                proposedMenusUrl + "/" + oldestPlanning.getBody().id() + "/menu",
                new EstablishProposedWeekMenuRequest(userId),
                CurrentWeekMenuResponse.class
        );
        ResponseEntity<CurrentWeekMenuResponse> middleMenu = postAuthorized(
                proposedMenusUrl + "/" + middlePlanning.getBody().id() + "/menu",
                new EstablishProposedWeekMenuRequest(userId),
                CurrentWeekMenuResponse.class
        );
        ResponseEntity<CurrentWeekMenuResponse> newestMenu = postAuthorized(
                proposedMenusUrl + "/" + newestPlanning.getBody().id() + "/menu",
                new EstablishProposedWeekMenuRequest(userId),
                CurrentWeekMenuResponse.class
        );

        assertThat(oldestMenu.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(middleMenu.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(newestMenu.getStatusCode()).isEqualTo(HttpStatus.CREATED);

        assertThat(postAuthorized(
                menusUrl + "/" + middleMenu.getBody().id() + "/close",
                new CloseCurrentWeekMenuRequest(List.of(userId)),
                CurrentWeekMenuStatsResponse.class
        ).getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(postAuthorized(
                menusUrl + "/" + newestMenu.getBody().id() + "/close",
                new CloseCurrentWeekMenuRequest(List.of(userId)),
                CurrentWeekMenuStatsResponse.class
        ).getStatusCode()).isEqualTo(HttpStatus.OK);

        ResponseEntity<MenuPageResponse> allMenus = getAuthorized(menusUrl + "?page=0&size=100", MenuPageResponse.class);
        assertThat(allMenus.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(allMenus.getBody()).isNotNull();
        assertThat(allMenus.getBody().page()).isEqualTo(0);
        assertThat(allMenus.getBody().size()).isEqualTo(100);
        assertThat(allMenus.getBody().totalElements()).isGreaterThanOrEqualTo(3);
        assertThat(allMenus.getBody().items())
                .filteredOn(menu -> java.util.Objects.equals(menu.id(), oldestMenu.getBody().id())
                        || java.util.Objects.equals(menu.id(), middleMenu.getBody().id())
                        || java.util.Objects.equals(menu.id(), newestMenu.getBody().id()))
                .extracting(
                        CurrentWeekMenuResponse::id,
                        CurrentWeekMenuResponse::state,
                        CurrentWeekMenuResponse::isActive,
                        CurrentWeekMenuResponse::canEdit,
                        CurrentWeekMenuResponse::canClose,
                        CurrentWeekMenuResponse::canUndo,
                        CurrentWeekMenuResponse::planningId,
                        CurrentWeekMenuResponse::startDate
                )
                .containsExactly(
                        tuple(newestMenu.getBody().id(), com.eliascanalesnieto.foodhelper.domain.CurrentWeekMenuState.CLOSED, false, false, false, false, newestMenu.getBody().planningId(), LocalDate.of(2025, 6, 8)),
                        tuple(middleMenu.getBody().id(), com.eliascanalesnieto.foodhelper.domain.CurrentWeekMenuState.CLOSED, false, false, false, false, middleMenu.getBody().planningId(), LocalDate.of(2025, 6, 1)),
                        tuple(oldestMenu.getBody().id(), com.eliascanalesnieto.foodhelper.domain.CurrentWeekMenuState.ESTABLISHED, true, true, true, true, oldestMenu.getBody().planningId(), LocalDate.of(2025, 5, 25))
                );

        ResponseEntity<MenuPageResponse> closedMenus = getAuthorized(menusUrl + "?page=0&size=100&state=CLOSED", MenuPageResponse.class);
        assertThat(closedMenus.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(closedMenus.getBody()).isNotNull();
        assertThat(closedMenus.getBody().totalElements()).isGreaterThanOrEqualTo(2);
        assertThat(closedMenus.getBody().items())
                .filteredOn(menu -> java.util.Objects.equals(menu.id(), newestMenu.getBody().id())
                        || java.util.Objects.equals(menu.id(), middleMenu.getBody().id()))
                .extracting(CurrentWeekMenuResponse::id, CurrentWeekMenuResponse::state, CurrentWeekMenuResponse::isActive)
                .containsExactly(
                        tuple(newestMenu.getBody().id(), com.eliascanalesnieto.foodhelper.domain.CurrentWeekMenuState.CLOSED, false),
                        tuple(middleMenu.getBody().id(), com.eliascanalesnieto.foodhelper.domain.CurrentWeekMenuState.CLOSED, false)
                );

        ResponseEntity<MenuPageResponse> establishedMenus = getAuthorized(menusUrl + "?page=0&size=100&state=ESTABLISHED", MenuPageResponse.class);
        assertThat(establishedMenus.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(establishedMenus.getBody()).isNotNull();
        assertThat(establishedMenus.getBody().totalElements()).isGreaterThanOrEqualTo(1);
        assertThat(establishedMenus.getBody().items())
                .filteredOn(menu -> java.util.Objects.equals(menu.id(), oldestMenu.getBody().id()))
                .extracting(CurrentWeekMenuResponse::id, CurrentWeekMenuResponse::state, CurrentWeekMenuResponse::isActive)
                .containsExactly(
                        tuple(oldestMenu.getBody().id(), com.eliascanalesnieto.foodhelper.domain.CurrentWeekMenuState.ESTABLISHED, true)
                );

        ResponseEntity<CurrentWeekMenuResponse> closedMenuDetail = getAuthorized(
                menusUrl + "/" + newestMenu.getBody().id(),
                CurrentWeekMenuResponse.class
        );
        assertThat(closedMenuDetail.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(closedMenuDetail.getBody()).isNotNull();
        assertThat(closedMenuDetail.getBody().state()).isEqualTo(com.eliascanalesnieto.foodhelper.domain.CurrentWeekMenuState.CLOSED);
        assertThat(closedMenuDetail.getBody().isActive()).isFalse();
        assertThat(closedMenuDetail.getBody().canEdit()).isFalse();
        assertThat(closedMenuDetail.getBody().planningId()).isEqualTo(newestMenu.getBody().planningId());

        ResponseEntity<CurrentWeekMenuResponse> openMenuDetail = getAuthorized(
                menusUrl + "/" + oldestMenu.getBody().id(),
                CurrentWeekMenuResponse.class
        );
        assertThat(openMenuDetail.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(openMenuDetail.getBody()).isNotNull();
        assertThat(openMenuDetail.getBody().state()).isEqualTo(com.eliascanalesnieto.foodhelper.domain.CurrentWeekMenuState.ESTABLISHED);
        assertThat(openMenuDetail.getBody().isActive()).isTrue();
        assertThat(openMenuDetail.getBody().canClose()).isTrue();
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
