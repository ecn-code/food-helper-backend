package com.eliascanalesnieto.foodhelper.lambda;

import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyRequestEvent;
import com.amazonaws.services.lambda.runtime.events.APIGatewayProxyResponseEvent;
import com.eliascanalesnieto.foodhelper.application.ProductService;
import com.eliascanalesnieto.foodhelper.domain.Product;
import com.eliascanalesnieto.foodhelper.presentation.CreateProductRequest;
import com.eliascanalesnieto.foodhelper.presentation.ProductApiMapper;
import com.eliascanalesnieto.foodhelper.presentation.UpdateProductRequest;
import com.eliascanalesnieto.foodhelper.presentation.error.DuplicateResourceException;
import com.eliascanalesnieto.foodhelper.presentation.error.ResourceNotFoundException;
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
    private final ProductApiMapper mapper;
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

        if ("POST".equals(method) && "/api/v1/products".equals(path)) {
            CreateProductRequest body = parseCreate(request.getBody());
            Product created = service.create(body.name(), body.description(), body.calories(), body.carbohydrates(), body.proteins(), body.fats());
            return json(201, mapper.toResponse(created));
        }

        if (path != null && path.startsWith("/api/v1/products/")) {
            Long id = parseId(path);
            if ("PUT".equals(method)) {
                UpdateProductRequest body = parseUpdate(request.getBody());
                return json(200, mapper.toResponse(service.update(id, body.name(), body.description(), body.calories(), body.carbohydrates(), body.proteins(), body.fats())));
            }
            if ("DELETE".equals(method)) {
                service.delete(id);
                return new APIGatewayProxyResponseEvent().withStatusCode(204).withHeaders(defaultHeaders());
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
}
