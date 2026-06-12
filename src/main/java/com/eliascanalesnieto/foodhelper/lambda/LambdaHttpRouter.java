package com.eliascanalesnieto.foodhelper.lambda;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.eliascanalesnieto.foodhelper.application.AuthService;
import com.eliascanalesnieto.foodhelper.application.JwtService;
import com.eliascanalesnieto.foodhelper.application.ProductService;
import com.eliascanalesnieto.foodhelper.application.ProposedWeekMenuService;
import com.eliascanalesnieto.foodhelper.application.RecipeService;
import com.eliascanalesnieto.foodhelper.application.StockService;
import com.eliascanalesnieto.foodhelper.domain.Product;
import com.eliascanalesnieto.foodhelper.domain.RecipeIngredient;
import com.eliascanalesnieto.foodhelper.presentation.AdjustStockQuantityRequest;
import com.eliascanalesnieto.foodhelper.presentation.CreateProductRequest;
import com.eliascanalesnieto.foodhelper.presentation.CreateProposedWeekMenuRequest;
import com.eliascanalesnieto.foodhelper.presentation.CreateRecipeDerivedProductRequest;
import com.eliascanalesnieto.foodhelper.presentation.CreateRecipeRequest;
import com.eliascanalesnieto.foodhelper.presentation.CreateStockEntryRequest;
import com.eliascanalesnieto.foodhelper.presentation.LoginRequest;
import com.eliascanalesnieto.foodhelper.presentation.ProductApiMapper;
import com.eliascanalesnieto.foodhelper.presentation.ProposedWeekMenuApiMapper;
import com.eliascanalesnieto.foodhelper.presentation.RecipeIngredientAssignmentRequest;
import com.eliascanalesnieto.foodhelper.presentation.RegisterRequest;
import com.eliascanalesnieto.foodhelper.presentation.UpsertProposedWeekMenuDayRequest;
import com.eliascanalesnieto.foodhelper.presentation.UpdateRecipeRequest;
import com.eliascanalesnieto.foodhelper.presentation.UpdateProductRequest;
import com.eliascanalesnieto.foodhelper.presentation.error.DuplicateResourceException;
import com.eliascanalesnieto.foodhelper.presentation.error.ResourceNotFoundException;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

@Configuration
@RequiredArgsConstructor
public class LambdaHttpRouter {

    private final ProductService service;
    private final RecipeService recipeService;
    private final StockService stockService;
    private final ProposedWeekMenuService proposedWeekMenuService;
    private final AuthService authService;
    private final JwtService jwtService;
    private final ProductApiMapper mapper;
    private final ProposedWeekMenuApiMapper proposedWeekMenuMapper;
    private final ObjectMapper objectMapper;

    @Bean
    public Function<APIGatewayProxyRequestEvent, APIGatewayProxyResponseEvent> productHttpHandler() {
        return request -> {
            try {
                return route(request);
            } catch (ResourceNotFoundException ex) {
                return json(404, Map.of("message", ex.getMessage()));
            } catch (DuplicateResourceException ex) {
                return json(409, Map.of("message", ex.getMessage()));
            } catch (IllegalArgumentException ex) {
                return json(400, Map.of("message", ex.getMessage()));
            } catch (Exception ex) {
                return json(500, Map.of("message", "Internal server error"));
            }
        };
    }

    private APIGatewayProxyResponseEvent route(APIGatewayProxyRequestEvent request) {
        String method = request.getHttpMethod();
        String path = request.getPath();

        if ("GET".equals(method) && "/api/v1/health".equals(path)) {
            return json(200, Map.of("status", "UP"));
        }

        if ("POST".equals(method) && "/api/v1/auth/register".equals(path)) {
            RegisterRequest body = parseRegister(request.getBody());
            return json(201, authService.register(body.username(), body.password(), body.registrationCode()));
        }

        if ("POST".equals(method) && "/api/v1/auth/login".equals(path)) {
            LoginRequest body = parseLogin(request.getBody());
            return json(200, authService.login(body.username(), body.password()));
        }

        if (!isAuthorized(request)) {
            return json(401, Map.of("message", "Missing or invalid Bearer token"));
        }

        if ("POST".equals(method) && "/api/v1/products".equals(path)) {
            CreateProductRequest body = parseCreate(request.getBody());
            Product created = service.create(body.name(), body.description(), body.gramsPerUnit(), body.calories(), body.carbohydrates(), body.proteins(), body.fats());
            return json(201, mapper.toResponse(created));
        }

        if ("POST".equals(method) && "/api/v1/proposed-week-menus".equals(path)) {
            CreateProposedWeekMenuRequest body = parseProposedWeekMenuCreate(request.getBody());
            return json(201, proposedWeekMenuMapper.toResponse(proposedWeekMenuService.create(body.startDate(), body.endDate())));
        }

        if ("GET".equals(method) && "/api/v1/stock".equals(path)) {
            return json(200, stockService.findStock(
                    parseOptionalDate(queryParam(request, "expiresBefore")),
                    parseProductIds(queryParam(request, "productIds"))
            ).stream().map(mapper::toResponse).toList());
        }

        if ("POST".equals(method) && "/api/v1/recipes".equals(path)) {
            CreateRecipeRequest body = parseRecipeCreate(request.getBody());
            return json(201, mapper.toResponse(recipeService.create(
                    body.name(),
                    body.description(),
                    body.instructions(),
                    toDomainIngredients(body.products())
            )));
        }

        if (path != null && path.startsWith("/api/v1/products/")) {
            if (path.endsWith("/stock")) {
                Long productId = parseId(path.substring(0, path.lastIndexOf('/')));
                if ("POST".equals(method)) {
                    CreateStockEntryRequest body = parseStockCreate(request.getBody());
                    return json(201, mapper.toResponse(stockService.create(
                            productId,
                            body.quantity(),
                            body.expirationDate(),
                            body.entryDate()
                    )));
                }
                if ("GET".equals(method)) {
                    return json(200, stockService.findStockByProduct(
                            productId,
                            parseOptionalDate(queryParam(request, "expiresBefore"))
                    ).stream().map(mapper::toResponse).toList());
                }
            }
            Long id = parseId(path);
            if ("PUT".equals(method)) {
                UpdateProductRequest body = parseUpdate(request.getBody());
                return json(200, mapper.toResponse(service.update(id, body.name(), body.description(), body.gramsPerUnit(), body.calories(), body.carbohydrates(), body.proteins(), body.fats())));
            }
            if ("DELETE".equals(method)) {
                service.delete(id);
                return new APIGatewayProxyResponseEvent().withStatusCode(204).withHeaders(defaultHeaders());
            }
        }

        if (path != null && path.startsWith("/api/v1/recipes/")) {
            if (path.endsWith("/derived-product")) {
                Long id = parseId(path.substring(0, path.lastIndexOf('/')));
                if ("POST".equals(method)) {
                    CreateRecipeDerivedProductRequest body = parseDerivedProductCreate(request.getBody());
                    return json(201, mapper.toResponse(recipeService.createDerivedProduct(id, body.producedGrams(), body.gramsPerUnit())));
                }
            }

            Long id = parseId(path);
            if ("PUT".equals(method)) {
                UpdateRecipeRequest body = parseRecipeUpdate(request.getBody());
                return json(200, mapper.toResponse(recipeService.update(
                        id,
                        body.name(),
                        body.description(),
                        body.instructions(),
                        toDomainIngredients(body.products())
                )));
            }
            if ("DELETE".equals(method)) {
                recipeService.delete(id);
                return new APIGatewayProxyResponseEvent().withStatusCode(204).withHeaders(defaultHeaders());
            }
        }

        if (path != null && path.startsWith("/api/v1/proposed-week-menus/")) {
            if (path.endsWith("/days")) {
                Long id = parseId(path.substring(0, path.lastIndexOf('/')));
                if ("PUT".equals(method)) {
                    UpsertProposedWeekMenuDayRequest body = parseProposedWeekMenuDayUpsert(request.getBody());
                    return json(200, proposedWeekMenuMapper.toResponse(proposedWeekMenuService.upsertDay(id, proposedWeekMenuMapper.toDomain(body))));
                }
            }
            Long id = parseId(path);
            if ("GET".equals(method)) {
                return json(200, proposedWeekMenuMapper.toResponse(proposedWeekMenuService.findById(id)));
            }
        }

        if (path != null && path.startsWith("/api/v1/stock/")) {
            if (path.endsWith("/add")) {
                Long id = parseId(path.substring(0, path.lastIndexOf('/')));
                if ("POST".equals(method)) {
                    AdjustStockQuantityRequest body = parseAdjustStockQuantity(request.getBody());
                    return json(200, mapper.toResponse(stockService.addQuantity(id, body.quantity())));
                }
            }
            if (path.endsWith("/remove")) {
                Long id = parseId(path.substring(0, path.lastIndexOf('/')));
                if ("POST".equals(method)) {
                    AdjustStockQuantityRequest body = parseAdjustStockQuantity(request.getBody());
                    stockService.removeQuantity(id, body.quantity());
                    return new APIGatewayProxyResponseEvent().withStatusCode(200).withHeaders(defaultHeaders());
                }
            }
        }

        return json(404, Map.of("message", "Not found"));
    }

    private Long parseId(String path) {
        String value = path.substring(path.lastIndexOf('/') + 1);
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid id");
        }
    }

    private CreateProductRequest parseCreate(String body) {
        if (!StringUtils.hasText(body)) {
            throw new IllegalArgumentException("Body is required");
        }
        try {
            return objectMapper.readValue(body, CreateProductRequest.class);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid JSON body");
        }
    }

    private UpdateProductRequest parseUpdate(String body) {
        if (!StringUtils.hasText(body)) {
            throw new IllegalArgumentException("Body is required");
        }
        try {
            return objectMapper.readValue(body, UpdateProductRequest.class);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid JSON body");
        }
    }

    private RegisterRequest parseRegister(String body) {
        if (!StringUtils.hasText(body)) {
            throw new IllegalArgumentException("Body is required");
        }
        try {
            return objectMapper.readValue(body, RegisterRequest.class);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid JSON body");
        }
    }

    private LoginRequest parseLogin(String body) {
        if (!StringUtils.hasText(body)) {
            throw new IllegalArgumentException("Body is required");
        }
        try {
            return objectMapper.readValue(body, LoginRequest.class);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid JSON body");
        }
    }

    private CreateRecipeRequest parseRecipeCreate(String body) {
        if (!StringUtils.hasText(body)) {
            throw new IllegalArgumentException("Body is required");
        }
        try {
            return objectMapper.readValue(body, CreateRecipeRequest.class);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid JSON body");
        }
    }

    private CreateProposedWeekMenuRequest parseProposedWeekMenuCreate(String body) {
        if (!StringUtils.hasText(body)) {
            throw new IllegalArgumentException("Body is required");
        }
        try {
            return objectMapper.readValue(body, CreateProposedWeekMenuRequest.class);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid JSON body");
        }
    }

    private UpsertProposedWeekMenuDayRequest parseProposedWeekMenuDayUpsert(String body) {
        if (!StringUtils.hasText(body)) {
            throw new IllegalArgumentException("Body is required");
        }
        try {
            return objectMapper.readValue(body, UpsertProposedWeekMenuDayRequest.class);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid JSON body");
        }
    }

    private UpdateRecipeRequest parseRecipeUpdate(String body) {
        if (!StringUtils.hasText(body)) {
            throw new IllegalArgumentException("Body is required");
        }
        try {
            return objectMapper.readValue(body, UpdateRecipeRequest.class);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid JSON body");
        }
    }

    private CreateRecipeDerivedProductRequest parseDerivedProductCreate(String body) {
        if (!StringUtils.hasText(body)) {
            throw new IllegalArgumentException("Body is required");
        }
        try {
            return objectMapper.readValue(body, CreateRecipeDerivedProductRequest.class);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid JSON body");
        }
    }

    private CreateStockEntryRequest parseStockCreate(String body) {
        if (!StringUtils.hasText(body)) {
            throw new IllegalArgumentException("Body is required");
        }
        try {
            return objectMapper.readValue(body, CreateStockEntryRequest.class);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid JSON body");
        }
    }

    private AdjustStockQuantityRequest parseAdjustStockQuantity(String body) {
        if (!StringUtils.hasText(body)) {
            throw new IllegalArgumentException("Body is required");
        }
        try {
            return objectMapper.readValue(body, AdjustStockQuantityRequest.class);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid JSON body");
        }
    }

    private List<RecipeIngredient> toDomainIngredients(List<RecipeIngredientAssignmentRequest> products) {
        return products.stream()
                .map(product -> RecipeIngredient.builder()
                        .productId(product.productId())
                        .grams(product.grams())
                        .build())
                .toList();
    }

    private APIGatewayProxyResponseEvent json(int status, Object body) {
        try {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(status)
                    .withHeaders(defaultHeaders())
                    .withBody(objectMapper.writeValueAsString(body));
        } catch (Exception ex) {
            return new APIGatewayProxyResponseEvent()
                    .withStatusCode(500)
                    .withHeaders(defaultHeaders())
                    .withBody("{\"message\":\"Internal server error\"}");
        }
    }

    private Map<String, String> defaultHeaders() {
        return Map.of("Content-Type", "application/json");
    }

    private boolean isAuthorized(APIGatewayProxyRequestEvent request) {
        String authorization = header(request, "Authorization");
        if (!StringUtils.hasText(authorization) || !authorization.startsWith("Bearer ")) {
            return false;
        }
        return jwtService.isValid(authorization.substring("Bearer ".length()));
    }

    private String header(APIGatewayProxyRequestEvent request, String name) {
        if (request.getHeaders() == null) {
            return null;
        }
        String value = request.getHeaders().get(name);
        if (StringUtils.hasText(value)) {
            return value;
        }
        return request.getHeaders().get(name.toLowerCase());
    }

    private String queryParam(APIGatewayProxyRequestEvent request, String name) {
        if (request.getQueryStringParameters() == null) {
            return null;
        }
        return request.getQueryStringParameters().get(name);
    }

    private LocalDate parseOptionalDate(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return LocalDate.parse(value);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid date");
        }
    }

    private List<Long> parseProductIds(String value) {
        if (!StringUtils.hasText(value)) {
            return List.of();
        }
        try {
            return Arrays.stream(value.split(","))
                    .map(String::trim)
                    .filter(StringUtils::hasText)
                    .map(Long::parseLong)
                    .toList();
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid productIds");
        }
    }
}
