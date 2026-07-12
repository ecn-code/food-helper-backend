package com.eliascanalesnieto.foodhelper.lambda;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.eliascanalesnieto.foodhelper.application.AuthService;
import com.eliascanalesnieto.foodhelper.application.JwtService;
import com.eliascanalesnieto.foodhelper.application.MediaService;
import com.eliascanalesnieto.foodhelper.application.MediaUrlService;
import com.eliascanalesnieto.foodhelper.application.NutritionalRulesService;
import com.eliascanalesnieto.foodhelper.application.PageResult;
import com.eliascanalesnieto.foodhelper.application.PaginationRequest;
import com.eliascanalesnieto.foodhelper.application.ProductService;
import com.eliascanalesnieto.foodhelper.application.CurrentWeekMenuService;
import com.eliascanalesnieto.foodhelper.application.ProposedWeekMenuService;
import com.eliascanalesnieto.foodhelper.application.PlanningCouponService;
import com.eliascanalesnieto.foodhelper.application.RecipeService;
import com.eliascanalesnieto.foodhelper.application.StatsService;
import com.eliascanalesnieto.foodhelper.application.StockService;
import com.eliascanalesnieto.foodhelper.application.SupermarketService;
import com.eliascanalesnieto.foodhelper.application.UserMoneyService;
import com.eliascanalesnieto.foodhelper.application.UserWeightService;
import com.eliascanalesnieto.foodhelper.domain.CurrentWeekMenuState;
import com.eliascanalesnieto.foodhelper.domain.Product;
import com.eliascanalesnieto.foodhelper.domain.RecipeIngredient;
import com.eliascanalesnieto.foodhelper.domain.ProductSearchCriteria;
import com.eliascanalesnieto.foodhelper.domain.RecipeSearchCriteria;
import com.eliascanalesnieto.foodhelper.presentation.AdjustStockQuantityRequest;
import com.eliascanalesnieto.foodhelper.presentation.CreateProductRequest;
import com.eliascanalesnieto.foodhelper.presentation.CreateMoneyBoxRequest;
import com.eliascanalesnieto.foodhelper.presentation.CreateProposedWeekMenuRequest;
import com.eliascanalesnieto.foodhelper.presentation.CreateMenuStockMovementRequest;
import com.eliascanalesnieto.foodhelper.presentation.CreateRecipeDerivedProductRequest;
import com.eliascanalesnieto.foodhelper.presentation.CreateRecipeRequest;
import com.eliascanalesnieto.foodhelper.presentation.CreateStockEntryRequest;
import com.eliascanalesnieto.foodhelper.presentation.CreateUserMoneyMovementRequest;
import com.eliascanalesnieto.foodhelper.presentation.CreateUserWeightRequest;
import com.eliascanalesnieto.foodhelper.presentation.CloseCurrentWeekMenuRequest;
import com.eliascanalesnieto.foodhelper.presentation.EstablishProposedWeekMenuRequest;
import com.eliascanalesnieto.foodhelper.presentation.LoginRequest;
import com.eliascanalesnieto.foodhelper.presentation.ProductApiMapper;
import com.eliascanalesnieto.foodhelper.presentation.ProductPageResponse;
import com.eliascanalesnieto.foodhelper.presentation.MenuPageResponse;
import com.eliascanalesnieto.foodhelper.presentation.ProposedWeekMenuApiMapper;
import com.eliascanalesnieto.foodhelper.presentation.RecipeIngredientAssignmentRequest;
import com.eliascanalesnieto.foodhelper.presentation.RecipePageResponse;
import com.eliascanalesnieto.foodhelper.presentation.RegisterRequest;
import com.eliascanalesnieto.foodhelper.presentation.SaveNutritionalRulesRequest;
import com.eliascanalesnieto.foodhelper.presentation.SupermarketRequest;
import com.eliascanalesnieto.foodhelper.presentation.SupermarketResponse;
import com.eliascanalesnieto.foodhelper.presentation.StockMovementPageResponse;
import com.eliascanalesnieto.foodhelper.presentation.ValidateProposedWeekMenuCouponsRequest;
import com.eliascanalesnieto.foodhelper.presentation.UpdateCurrentWeekMenuPayerRequest;
import com.eliascanalesnieto.foodhelper.presentation.UpdateCurrentWeekMenuStockRequest;
import com.eliascanalesnieto.foodhelper.presentation.UpdateStockEntryRequest;
import com.eliascanalesnieto.foodhelper.presentation.UpdateUserWeightRequest;
import com.eliascanalesnieto.foodhelper.presentation.UpsertProposedWeekMenuDayRequest;
import com.eliascanalesnieto.foodhelper.presentation.UpdateRecipeRequest;
import com.eliascanalesnieto.foodhelper.presentation.UpdateProductRequest;
import com.eliascanalesnieto.foodhelper.presentation.error.DuplicateResourceException;
import com.eliascanalesnieto.foodhelper.presentation.error.ResourceNotFoundException;
import jakarta.validation.Validator;
import java.time.LocalDate;
import java.time.Instant;
import java.util.Base64;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.math.BigDecimal;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

@Configuration
@RequiredArgsConstructor
@Slf4j
public class LambdaHttpRouter {

    private final ProductService service;
    private final RecipeService recipeService;
    private final StockService stockService;
    private final SupermarketService supermarketService;
    private final ProposedWeekMenuService proposedWeekMenuService;
    private final CurrentWeekMenuService currentWeekMenuService;
    private final PlanningCouponService planningCouponService;
    private final UserMoneyService userMoneyService;
    private final UserWeightService userWeightService;
    private final NutritionalRulesService nutritionalRulesService;
    private final StatsService statsService;
    private final AuthService authService;
    private final JwtService jwtService;
    private final MediaService mediaService;
    private final MediaUrlService mediaUrlService;
    private final ProductApiMapper mapper;
    private final ProposedWeekMenuApiMapper proposedWeekMenuMapper;
    private final ObjectMapper objectMapper;
    private final Validator validator;

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
                log.error("Lambda router error handling {} {}", request.getHttpMethod(), request.getPath(), ex);
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

        if (path != null && path.startsWith("/api/v1/media/")) {
            Long id = parseId(path);
            if ("GET".equals(method)) {
                if (!isAuthorized(request) && !isSignedMediaRequest(id, request)) {
                    return json(401, Map.of("message", "Missing or invalid Bearer token"));
                }
                return binary(mediaService.findById(id));
            }
        }

        if (!isAuthorized(request)) {
            return json(401, Map.of("message", "Missing or invalid Bearer token"));
        }

        if ("GET".equals(method) && "/api/v1/coupons".equals(path)) {
            Long payerUserId = parseRequiredLong(queryParam(request, "payerUserId"), "payerUserId");
            boolean onlyAvailable = Boolean.parseBoolean(queryParam(request, "onlyAvailable"));
            return json(200, planningCouponService.findGlobalCoupons(payerUserId, onlyAvailable));
        }

        if ("POST".equals(method) && "/api/v1/products".equals(path)) {
            CreateProductRequest body = parseCreate(request.getBody());
            Product created = service.create(
                    body.name(),
                    body.description(),
                    body.gramsPerUnit(),
                    body.calories(),
                    body.carbohydrates(),
                    body.proteins(),
                    body.fats(),
                    body.defaultPrice(),
                    body.photo() == null ? null : body.photo().toDomain(),
                    body.supermarketIds()
            );
            return json(201, mapper.toResponse(created));
        }

        if ("GET".equals(method) && "/api/v1/products".equals(path)) {
            return json(200, toProductPage(service.findPage(parsePagination(request), parseProductSearchCriteria(request))));
        }

        if ("GET".equals(method) && "/api/v1/supermarkets".equals(path)) {
            return json(200, supermarketService.findAll().stream()
                    .map(supermarket -> new SupermarketResponse(supermarket.getId(), supermarket.getName()))
                    .toList());
        }

        if ("POST".equals(method) && "/api/v1/supermarkets".equals(path)) {
            SupermarketRequest body = readBody(request.getBody(), SupermarketRequest.class);
            var supermarket = supermarketService.create(body.name());
            return json(201, new SupermarketResponse(supermarket.getId(), supermarket.getName()));
        }

        if (path != null && path.startsWith("/api/v1/supermarkets/")) {
            Long id = parseId(path);
            if ("GET".equals(method)) {
                var supermarket = supermarketService.findById(id);
                return json(200, new SupermarketResponse(supermarket.getId(), supermarket.getName()));
            }
            if ("PUT".equals(method)) {
                SupermarketRequest body = readBody(request.getBody(), SupermarketRequest.class);
                var supermarket = supermarketService.update(id, body.name());
                return json(200, new SupermarketResponse(supermarket.getId(), supermarket.getName()));
            }
            if ("DELETE".equals(method)) {
                supermarketService.delete(id);
                return new APIGatewayProxyResponseEvent().withStatusCode(204).withHeaders(defaultHeaders());
            }
        }

        if ("POST".equals(method) && "/api/v1/planning".equals(path)) {
            CreateProposedWeekMenuRequest body = parseProposedWeekMenuCreate(request.getBody());
            return json(201, proposedWeekMenuMapper.toResponse(proposedWeekMenuService.create(body.startDate(), body.endDate())));
        }

        if ("GET".equals(method) && "/api/v1/planning".equals(path)) {
            return json(200, proposedWeekMenuService.findAllSummaries());
        }

        if (path != null && path.startsWith("/api/v1/planning/")) {
            if (path.endsWith("/days")) {
                Long id = parseId(path.substring(0, path.lastIndexOf('/')));
                if ("PUT".equals(method)) {
                    UpsertProposedWeekMenuDayRequest body = parseProposedWeekMenuDayUpsert(request.getBody());
                    return json(200, proposedWeekMenuMapper.toResponse(proposedWeekMenuService.upsertDay(id, proposedWeekMenuMapper.toDomain(body))));
                }
            }
            if (path.endsWith("/coupons")) {
                Long id = parseId(path.substring(0, path.lastIndexOf('/')));
                if ("GET".equals(method)) {
                    return json(200, planningCouponService.findCoupons(id, parseRequiredLong(queryParam(request, "payerUserId"), "payerUserId")));
                }
            }
            if (path.endsWith("/coupons/validate")) {
                Long id = parseId(path.substring(0, path.indexOf("/coupons/validate")));
                if ("POST".equals(method)) {
                    ValidateProposedWeekMenuCouponsRequest body = parseValidateProposedWeekMenuCoupons(request.getBody());
                    return json(200, planningCouponService.validateCoupons(id, body.payerUserId(), body.couponCodes()));
                }
            }
            if (path.endsWith("/menu")) {
                Long id = parseId(path.substring(0, path.lastIndexOf('/')));
                if ("POST".equals(method)) {
                    EstablishProposedWeekMenuRequest body = parseEstablishProposedWeekMenu(request.getBody());
                    return json(201, currentWeekMenuService.establishFromProposed(
                            id,
                            body.payerUserId(),
                            body.stockAllocations(),
                            body.couponCodes(),
                            body.personIds()
                    ));
                }
            }
            Long id = parseId(path);
            if ("GET".equals(method)) {
                return json(200, proposedWeekMenuMapper.toResponse(proposedWeekMenuService.findById(id)));
            }
            if ("DELETE".equals(method)) {
                proposedWeekMenuService.delete(id);
                return new APIGatewayProxyResponseEvent().withStatusCode(204).withHeaders(defaultHeaders());
            }
        }

        if ("GET".equals(method) && "/api/v1/stock".equals(path)) {
            return json(200, stockService.findStock(
                    parseOptionalDate(queryParam(request, "expiresBefore")),
                    parseProductIds(queryParam(request, "productIds"))
            ).stream().map(mapper::toResponse).toList());
        }

        if ("GET".equals(method) && "/api/v1/stock/movements".equals(path)) {
            return json(200, toStockMovementPage(stockService.findMovements(
                    parsePagination(request),
                    parseOptionalDate(queryParam(request, "fromDate")),
                    parseOptionalDate(queryParam(request, "toDate")),
                    parseProductIds(queryParam(request, "productIds"))
            )));
        }

        if ("GET".equals(method) && "/api/v1/products/stats".equals(path)) {
            return json(200, statsService.getProductStats());
        }

        if ("GET".equals(method) && "/api/v1/recipes".equals(path)) {
            return json(200, toRecipePage(recipeService.findPage(
                    parsePagination(request),
                    parseRecipeSearchCriteria(request)
            )));
        }

        if ("GET".equals(method) && "/api/v1/recipes/stats".equals(path)) {
            return json(200, statsService.getRecipeStats());
        }

        if ("POST".equals(method) && "/api/v1/recipes".equals(path)) {
            CreateRecipeRequest body = parseRecipeCreate(request.getBody());
            return json(201, mapper.toResponse(recipeService.create(
                    body.name(),
                    body.description(),
                    body.instructions(),
                    body.defaultUnitsProduced(),
                    toDomainIngredients(body.products()),
                    body.photo() == null ? null : body.photo().toDomain()
            )));
        }

        if (path != null && path.startsWith("/api/v1/products/")) {
            if (path.endsWith("/stock/reconciliation")) {
                Long productId = Long.parseLong(path.split("/")[4]);
                if ("GET".equals(method)) {
                    return json(200, stockService.reconcileProduct(productId));
                }
            }
            if (path.endsWith("/stock")) {
                Long productId = Long.parseLong(path.split("/")[4]);
                if ("POST".equals(method)) {
                    CreateStockEntryRequest body = parseStockCreate(request.getBody());
                    return json(201, mapper.toResponse(stockService.create(
                            productId,
                            body.quantity(),
                            body.price(),
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
            if ("GET".equals(method)) {
                Long id = parseId(path);
                return json(200, mapper.toResponse(service.findById(id)));
            }
            Long id = parseId(path);
            if ("PUT".equals(method)) {
                UpdateProductRequest body = parseUpdate(request.getBody());
                return json(200, mapper.toResponse(service.update(
                        id,
                        body.name(),
                        body.description(),
                        body.gramsPerUnit(),
                        body.calories(),
                        body.carbohydrates(),
                        body.proteins(),
                        body.fats(),
                        body.defaultPrice(),
                        body.photo() == null ? null : body.photo().toDomain(),
                        body.supermarketIds()
                )));
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
                    return json(201, mapper.toResponse(recipeService.createDerivedProduct(id, body.name(), body.units(), body.stockFromComposition())));
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
                        body.defaultUnitsProduced(),
                        body.stockFromComposition(),
                        toDomainIngredients(body.products()),
                        body.photo() == null ? null : body.photo().toDomain()
                )));
            }
            if ("DELETE".equals(method)) {
                recipeService.delete(id);
                return new APIGatewayProxyResponseEvent().withStatusCode(204).withHeaders(defaultHeaders());
            }
        }

        if ("GET".equals(method) && "/api/v1/menus".equals(path)) {
            return json(200, toMenuPage(
                    parsePagination(request),
                    parseOptionalMenuState(queryParam(request, "state"))
            ));
        }

        if ("GET".equals(method) && "/api/v1/menus/stats".equals(path)) {
            return json(200, currentWeekMenuService.findStatsByRange(
                    parseOptionalDate(queryParam(request, "from")),
                    parseOptionalDate(queryParam(request, "to"))
            ));
        }

        if (path != null && path.startsWith("/api/v1/menus/")) {
            if (path.endsWith("/payer")) {
                Long id = parseId(path.substring(0, path.lastIndexOf('/')));
                if ("PUT".equals(method)) {
                    UpdateCurrentWeekMenuPayerRequest body = readBody(request.getBody(), UpdateCurrentWeekMenuPayerRequest.class);
                    return json(200, currentWeekMenuService.updateResponsible(id, body.userId()));
                }
            }
            if (path.endsWith("/stock-movements")) {
                Long id = parseId(path.substring(0, path.lastIndexOf('/')));
                if ("GET".equals(method)) {
                    return json(200, currentWeekMenuService.findStockMovements(id));
                }
                if ("POST".equals(method)) {
                    CreateMenuStockMovementRequest body = readBody(request.getBody(), CreateMenuStockMovementRequest.class);
                    return json(200, currentWeekMenuService.addStockMovement(id, body));
                }
            }
            if (path.endsWith("/week-stock")) {
                Long id = parseId(path.substring(0, path.lastIndexOf('/')));
                if ("GET".equals(method)) {
                    return json(200, currentWeekMenuService.findById(id).weekStock());
                }
                if ("PUT".equals(method)) {
                    UpdateCurrentWeekMenuStockRequest body = readBody(request.getBody(), UpdateCurrentWeekMenuStockRequest.class);
                    return json(200, currentWeekMenuService.updateWeekStock(id, body));
                }
            }
            if (path.matches("/api/v1/menus/\\d+/recipe-productions/\\d+/transfer")) {
                String[] segments = path.split("/");
                Long menuId = Long.parseLong(segments[4]);
                Long recipeProductionId = Long.parseLong(segments[6]);
                if ("POST".equals(method)) {
                    return json(200, currentWeekMenuService.transferRecipeProduction(menuId, recipeProductionId));
                }
            }
            if (path.endsWith("/close")) {
                Long id = parseId(path.substring(0, path.lastIndexOf('/')));
                if ("POST".equals(method)) {
                    CloseCurrentWeekMenuRequest body = readBody(request.getBody(), CloseCurrentWeekMenuRequest.class);
                    return json(200, currentWeekMenuService.close(id, body.personIds()));
                }
            }
            if (path.endsWith("/stats")) {
                Long id = parseId(path.substring(0, path.lastIndexOf('/')));
                if ("GET".equals(method)) {
                    return json(200, currentWeekMenuService.findStatsById(id));
                }
            }
            if (path.endsWith("/used-stock")) {
                Long id = parseId(path.substring(0, path.lastIndexOf('/')));
                if ("GET".equals(method)) {
                    return json(200, currentWeekMenuService.findById(id).usedStock());
                }
            }
            if (path.endsWith("/shopping-list")) {
                Long id = parseId(path.substring(0, path.lastIndexOf('/')));
                if ("GET".equals(method)) {
                    return json(200, currentWeekMenuService.findShoppingList(
                            id,
                            parseOptionalLong(queryParam(request, "supermarketId"), "supermarketId")
                    ));
                }
            }
            Long id = parseId(path);
            if ("GET".equals(method)) {
                return json(200, currentWeekMenuService.findById(id));
            }
            if ("DELETE".equals(method)) {
                currentWeekMenuService.undo(id);
                return new APIGatewayProxyResponseEvent().withStatusCode(204).withHeaders(defaultHeaders());
            }
        }

        if ("GET".equals(method) && "/api/v1/users".equals(path)) {
            return json(200, currentWeekMenuService.findPeople());
        }

        if ("GET".equals(method) && "/api/v1/money-boxes".equals(path)) {
            return json(200, userMoneyService.findAllMoneyBoxes());
        }

        if ("POST".equals(method) && "/api/v1/money-boxes".equals(path)) {
            CreateMoneyBoxRequest body = readBody(request.getBody(), CreateMoneyBoxRequest.class);
            return json(201, userMoneyService.createManualMoneyBox(body.name()));
        }

        if (path != null && path.matches("/api/v1/money-boxes/\\d+/movements/\\d+")) {
            String[] segments = path.split("/");
            Long moneyBoxId = Long.parseLong(segments[4]);
            Long movementId = Long.parseLong(segments[6]);
            if ("DELETE".equals(method)) {
                userMoneyService.deleteMoneyBoxMovement(moneyBoxId, movementId);
                return new APIGatewayProxyResponseEvent().withStatusCode(204).withHeaders(defaultHeaders());
            }
        }

        if (path != null && path.matches("/api/v1/money-boxes/\\d+/movements")) {
            Long moneyBoxId = Long.parseLong(path.split("/")[4]);
            if ("POST".equals(method)) {
                CreateUserMoneyMovementRequest body = parseCreateUserMoneyMovement(request.getBody());
                return json(201, userMoneyService.addMoneyBoxMovement(
                        moneyBoxId,
                        body.amount(),
                        body.description()
                ));
            }
        }

        if (path != null && path.matches("/api/v1/money-boxes/\\d+")) {
            Long moneyBoxId = parseId(path);
            if ("GET".equals(method)) {
                return json(200, userMoneyService.findMoneyBoxById(moneyBoxId));
            }
            if ("DELETE".equals(method)) {
                userMoneyService.deleteMoneyBox(moneyBoxId);
                return new APIGatewayProxyResponseEvent().withStatusCode(204).withHeaders(defaultHeaders());
            }
        }

        if ("GET".equals(method) && path != null && path.matches("/api/v1/users/\\d+/menu-history")) {
            Long personId = Long.parseLong(path.split("/")[4]);
            return json(200, currentWeekMenuService.findHistoryByRange(
                    personId,
                    parseRequiredInstant(queryParam(request, "from"), "from"),
                    parseRequiredInstant(queryParam(request, "to"), "to")
            ));
        }

        if ("GET".equals(method) && path != null && path.matches("/api/v1/users/\\d+/menu-history/monthly")) {
            Long personId = Long.parseLong(path.split("/")[4]);
            return json(200, currentWeekMenuService.findMonthlyHistory(
                    personId,
                    parseRequiredInt(queryParam(request, "year"), "year"),
                    parseRequiredInt(queryParam(request, "month"), "month")
            ));
        }

        if ("GET".equals(method) && path != null && path.matches("/api/v1/users/\\d+/menu-history/annual")) {
            Long personId = Long.parseLong(path.split("/")[4]);
            return json(200, currentWeekMenuService.findAnnualHistory(
                    personId,
                    parseRequiredInt(queryParam(request, "year"), "year")
            ));
        }

        if (path != null && path.matches("/api/v1/users/\\d+/weights/stats")) {
            Long userId = Long.parseLong(path.split("/")[4]);
            if ("GET".equals(method)) {
                return json(200, userWeightService.findStats(
                        userId,
                        parseRequiredInstant(queryParam(request, "from"), "from"),
                        parseRequiredInstant(queryParam(request, "to"), "to")
                ));
            }
        }

        if (path != null && path.matches("/api/v1/users/\\d+/weights/\\d+")) {
            String[] pathParts = path.split("/");
            Long userId = Long.parseLong(pathParts[4]);
            Long weightId = Long.parseLong(pathParts[6]);
            if ("PUT".equals(method)) {
                UpdateUserWeightRequest body = readBody(request.getBody(), UpdateUserWeightRequest.class);
                return json(200, userWeightService.update(userId, weightId, body.weight(), body.recordedAt(), body.notes()));
            }
            if ("DELETE".equals(method)) {
                userWeightService.delete(userId, weightId);
                return new APIGatewayProxyResponseEvent().withStatusCode(204).withHeaders(defaultHeaders());
            }
        }

        if (path != null && path.matches("/api/v1/users/\\d+/weights")) {
            Long userId = Long.parseLong(path.split("/")[4]);
            if ("POST".equals(method)) {
                CreateUserWeightRequest body = readBody(request.getBody(), CreateUserWeightRequest.class);
                return json(201, userWeightService.create(userId, body.weight(), body.recordedAt(), body.notes()));
            }
            if ("GET".equals(method)) {
                return json(200, userWeightService.findByPeriod(
                        userId,
                        parseRequiredInstant(queryParam(request, "from"), "from"),
                        parseRequiredInstant(queryParam(request, "to"), "to")
                ));
            }
        }

        if (path != null && path.startsWith("/api/v1/users/") && path.endsWith("/money-box")) {
            Long userId = parseId(path.substring(0, path.lastIndexOf('/')));
            if ("GET".equals(method)) {
                return json(200, userMoneyService.findMoneyBox(userId));
            }
        }

        if ("GET".equals(method) && "/api/v1/nutritional-rules".equals(path)) {
            return json(200, nutritionalRulesService.find());
        }

        if ("PUT".equals(method) && "/api/v1/nutritional-rules".equals(path)) {
            return json(200, nutritionalRulesService.save(readBody(request.getBody(), SaveNutritionalRulesRequest.class)));
        }

        if (path != null && path.startsWith("/api/v1/users/") && path.endsWith("/money-box/movements")) {
            String userMoneyBoxPath = path.substring(0, path.lastIndexOf('/'));
            Long userId = parseId(userMoneyBoxPath.substring(0, userMoneyBoxPath.lastIndexOf('/')));
            if ("POST".equals(method)) {
                CreateUserMoneyMovementRequest body = parseCreateUserMoneyMovement(request.getBody());
                return json(201, userMoneyService.addMovement(userId, body.amount(), body.description()));
            }
        }

        if (path != null && path.startsWith("/api/v1/stock/")) {
            if (!path.endsWith("/add") && !path.endsWith("/remove")) {
                Long id = parseId(path);
                if ("PUT".equals(method)) {
                    UpdateStockEntryRequest body = parseStockUpdate(request.getBody());
                    return json(200, mapper.toResponse(stockService.update(
                            id,
                            body.quantity(),
                            body.price(),
                            body.expirationDate(),
                            body.entryDate()
                    )));
                }
            }
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
        return readBody(body, CreateProductRequest.class);
    }

    private UpdateProductRequest parseUpdate(String body) {
        return readBody(body, UpdateProductRequest.class);
    }

    private RegisterRequest parseRegister(String body) {
        return readBody(body, RegisterRequest.class);
    }

    private LoginRequest parseLogin(String body) {
        return readBody(body, LoginRequest.class);
    }

    private CreateRecipeRequest parseRecipeCreate(String body) {
        return readBody(body, CreateRecipeRequest.class);
    }

    private CreateProposedWeekMenuRequest parseProposedWeekMenuCreate(String body) {
        return readBody(body, CreateProposedWeekMenuRequest.class);
    }

    private UpsertProposedWeekMenuDayRequest parseProposedWeekMenuDayUpsert(String body) {
        return readBody(body, UpsertProposedWeekMenuDayRequest.class);
    }

    private EstablishProposedWeekMenuRequest parseEstablishProposedWeekMenu(String body) {
        return readBody(body, EstablishProposedWeekMenuRequest.class);
    }

    private ValidateProposedWeekMenuCouponsRequest parseValidateProposedWeekMenuCoupons(String body) {
        return readBody(body, ValidateProposedWeekMenuCouponsRequest.class);
    }

    private CreateUserMoneyMovementRequest parseCreateUserMoneyMovement(String body) {
        return readBody(body, CreateUserMoneyMovementRequest.class);
    }

    private UpdateRecipeRequest parseRecipeUpdate(String body) {
        return readBody(body, UpdateRecipeRequest.class);
    }

    private CreateRecipeDerivedProductRequest parseDerivedProductCreate(String body) {
        return readBody(body, CreateRecipeDerivedProductRequest.class);
    }

    private CreateStockEntryRequest parseStockCreate(String body) {
        return readBody(body, CreateStockEntryRequest.class);
    }

    private UpdateStockEntryRequest parseStockUpdate(String body) {
        return readBody(body, UpdateStockEntryRequest.class);
    }

    private AdjustStockQuantityRequest parseAdjustStockQuantity(String body) {
        return readBody(body, AdjustStockQuantityRequest.class);
    }

    private PaginationRequest parsePagination(APIGatewayProxyRequestEvent request) {
        return PaginationRequest.of(
                parseOptionalInteger(queryParam(request, "page"), "page"),
                parseOptionalInteger(queryParam(request, "size"), "size")
        );
    }

    private ProductSearchCriteria parseProductSearchCriteria(APIGatewayProxyRequestEvent request) {
        return ProductSearchCriteria.of(
                queryParam(request, "search"),
                parseOptionalBigDecimal(queryParam(request, "caloriesMin"), "caloriesMin"),
                parseOptionalBigDecimal(queryParam(request, "caloriesMax"), "caloriesMax"),
                parseOptionalBigDecimal(queryParam(request, "carbohydratesMin"), "carbohydratesMin"),
                parseOptionalBigDecimal(queryParam(request, "carbohydratesMax"), "carbohydratesMax"),
                parseOptionalBigDecimal(queryParam(request, "proteinsMin"), "proteinsMin"),
                parseOptionalBigDecimal(queryParam(request, "proteinsMax"), "proteinsMax"),
                parseOptionalBigDecimal(queryParam(request, "fatsMin"), "fatsMin"),
                parseOptionalBigDecimal(queryParam(request, "fatsMax"), "fatsMax")
        );
    }

    private RecipeSearchCriteria parseRecipeSearchCriteria(APIGatewayProxyRequestEvent request) {
        return new RecipeSearchCriteria(
                queryParam(request, "search"),
                parseOptionalBigDecimal(queryParam(request, "caloriesMin"), "caloriesMin"),
                parseOptionalBigDecimal(queryParam(request, "caloriesMax"), "caloriesMax"),
                parseOptionalBigDecimal(queryParam(request, "carbohydratesMin"), "carbohydratesMin"),
                parseOptionalBigDecimal(queryParam(request, "carbohydratesMax"), "carbohydratesMax"),
                parseOptionalBigDecimal(queryParam(request, "proteinsMin"), "proteinsMin"),
                parseOptionalBigDecimal(queryParam(request, "proteinsMax"), "proteinsMax"),
                parseOptionalBigDecimal(queryParam(request, "fatsMin"), "fatsMin"),
                parseOptionalBigDecimal(queryParam(request, "fatsMax"), "fatsMax"),
                parseOptionalBoolean(queryParam(request, "hasDerivedProduct"), "hasDerivedProduct")
        );
    }

    private Integer parseOptionalInteger(String value, String name) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid " + name);
        }
    }

    private Long parseOptionalLong(String value, String name) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return Long.parseLong(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid " + name);
        }
    }

    private Long parseRequiredLong(String value, String name) {
        Long parsed = parseOptionalLong(value, name);
        if (parsed == null) {
            throw new IllegalArgumentException(name + " is required");
        }
        return parsed;
    }

    private BigDecimal parseOptionalBigDecimal(String value, String name) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return new BigDecimal(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid " + name);
        }
    }

    private Boolean parseOptionalBoolean(String value, String name) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        if ("true".equalsIgnoreCase(value)) return true;
        if ("false".equalsIgnoreCase(value)) return false;
        throw new IllegalArgumentException("Invalid " + name);
    }

    private <T> T readBody(String body, Class<T> type) {
        if (!StringUtils.hasText(body)) {
            throw new IllegalArgumentException("Body is required");
        }
        try {
            T request = objectMapper.readValue(body, type);
            validate(request);
            return request;
        } catch (IllegalArgumentException ex) {
            throw ex;
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid JSON body");
        }
    }

    private void validate(Object request) {
        validator.validate(request).stream()
                .findFirst()
                .ifPresent(violation -> {
                    throw new IllegalArgumentException(violation.getPropertyPath() + " " + violation.getMessage());
                });
    }

    private List<RecipeIngredient> toDomainIngredients(List<RecipeIngredientAssignmentRequest> products) {
        return products.stream()
                .map(product -> RecipeIngredient.builder()
                        .productId(product.productId())
                        .quantity(product.quantity())
                        .quantityType(product.quantityType())
                        .build())
                .toList();
    }

    private ProductPageResponse toProductPage(PageResult<Product> page) {
        return new ProductPageResponse(
                page.items().stream().map(mapper::toResponse).toList(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages()
        );
    }

    private RecipePageResponse toRecipePage(PageResult<com.eliascanalesnieto.foodhelper.domain.Recipe> page) {
        return new RecipePageResponse(
                page.items().stream().map(mapper::toResponse).toList(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages()
        );
    }

    private StockMovementPageResponse toStockMovementPage(PageResult<com.eliascanalesnieto.foodhelper.domain.StockMovement> page) {
        return new StockMovementPageResponse(
                page.items().stream().map(mapper::toResponse).toList(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages()
        );
    }

    private MenuPageResponse toMenuPage(PaginationRequest pagination, CurrentWeekMenuState state) {
        PageResult<com.eliascanalesnieto.foodhelper.presentation.CurrentWeekMenuResponse> page =
                currentWeekMenuService.findPage(pagination, state);
        return new MenuPageResponse(
                page.items(),
                page.page(),
                page.size(),
                page.totalElements(),
                page.totalPages()
        );
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

    private APIGatewayProxyResponseEvent binary(com.eliascanalesnieto.foodhelper.domain.Media media) {
        return new APIGatewayProxyResponseEvent()
                .withStatusCode(200)
                .withHeaders(Map.of(
                        "Content-Type", media.getContentType(),
                        "Content-Disposition", "inline; filename=\"" + media.getFileName() + "\""
                ))
                .withIsBase64Encoded(true)
                .withBody(Base64.getEncoder().encodeToString(media.getData()));
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

    private boolean isSignedMediaRequest(Long mediaId, APIGatewayProxyRequestEvent request) {
        return mediaUrlService.isValid(mediaId, queryParam(request, "expiresAt"), queryParam(request, "signature"));
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

    private CurrentWeekMenuState parseOptionalMenuState(String value) {
        if (!StringUtils.hasText(value)) {
            return null;
        }
        try {
            return CurrentWeekMenuState.valueOf(value);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid state");
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

    private int parseRequiredInt(String value, String name) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(name + " is required");
        }
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException ex) {
            throw new IllegalArgumentException("Invalid " + name);
        }
    }

    private Instant parseRequiredInstant(String value, String name) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalArgumentException(name + " is required");
        }
        try {
            return Instant.parse(value);
        } catch (java.time.format.DateTimeParseException ex) {
            throw new IllegalArgumentException("Invalid " + name);
        }
    }
}
