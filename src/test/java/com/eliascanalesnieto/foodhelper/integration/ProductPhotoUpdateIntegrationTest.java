package com.eliascanalesnieto.foodhelper.integration;

import static org.assertj.core.api.Assertions.assertThat;

import com.eliascanalesnieto.foodhelper.presentation.AuthResponse;
import com.eliascanalesnieto.foodhelper.presentation.CreateProductRequest;
import com.eliascanalesnieto.foodhelper.presentation.PhotoUploadRequest;
import com.eliascanalesnieto.foodhelper.presentation.ProductResponse;
import com.eliascanalesnieto.foodhelper.presentation.RegisterRequest;
import com.eliascanalesnieto.foodhelper.presentation.UpdateProductRequest;
import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.web.client.DefaultResponseErrorHandler;
import org.springframework.web.client.RestTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test-integration")
class ProductPhotoUpdateIntegrationTest {
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

    RestTemplate restTemplate = new RestTemplate();
    private String accessToken;

    ProductPhotoUpdateIntegrationTest() {
        restTemplate.setErrorHandler(new DefaultResponseErrorHandler() {
            @Override
            protected boolean hasError(org.springframework.http.HttpStatusCode statusCode) {
                return false;
            }
        });
    }

    @Test
    void shouldAcceptRawBase64PhotoOnCreateAndUpdate() {
        String productsUrl = "http://localhost:" + port + "/api/v1/products";
        PhotoUploadRequest photo = minimalWebpPhoto("minimal-update");

        ResponseEntity<ProductResponse> created = restTemplate.exchange(
                productsUrl,
                HttpMethod.POST,
                authorizedEntity(new CreateProductRequest(
                        "Minimal WebP Apple",
                        "Apple with minimal WebP",
                        new BigDecimal("150"),
                        new BigDecimal("52"),
                        new BigDecimal("14"),
                        new BigDecimal("0.3"),
                        new BigDecimal("0.2"),
                        photo
                )),
                ProductResponse.class
        );

        assertThat(created.getStatusCode()).isEqualTo(HttpStatus.CREATED);
        assertThat(created.getBody()).isNotNull();
        assertThat(created.getBody().photo()).isNotNull();
        assertThat(created.getBody().photo()).startsWith("/api/v1/media/");
        assertThat(created.getBody().photo()).contains("signature=");

        ResponseEntity<ProductResponse> updated = restTemplate.exchange(
                productsUrl + "/" + created.getBody().id(),
                HttpMethod.PUT,
                authorizedEntity(new UpdateProductRequest(
                        "Minimal WebP Apple Updated",
                        "Apple with minimal WebP update",
                        new BigDecimal("140"),
                        new BigDecimal("48"),
                        new BigDecimal("13"),
                        new BigDecimal("0.4"),
                        new BigDecimal("0.1"),
                        photo
                )),
                ProductResponse.class
        );

        assertThat(updated.getStatusCode()).isEqualTo(HttpStatus.OK);
        assertThat(updated.getBody()).isNotNull();
        assertThat(updated.getBody().name()).isEqualTo("Minimal WebP Apple Updated");
        assertThat(updated.getBody().photo()).isNotNull();
        assertThat(updated.getBody().photo()).startsWith("/api/v1/media/");
        assertThat(updated.getBody().photo()).contains("signature=");
    }

    private <T> HttpEntity<T> authorizedEntity(T body) {
        HttpHeaders headers = authHeaders();
        return new HttpEntity<>(body, headers);
    }

    private HttpHeaders authHeaders() {
        if (accessToken == null) {
            ResponseEntity<AuthResponse> registered = restTemplate.postForEntity(
                    "http://localhost:" + port + "/api/v1/auth/register",
                    new RegisterRequest("photo-user-" + System.nanoTime(), "secret-password", REGISTRATION_CODE),
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

    private PhotoUploadRequest minimalWebpPhoto(String fileNameBase) {
        return new PhotoUploadRequest(
                fileNameBase + ".webp",
                "image/webp",
                "UklGRjwAAABXRUJQVlA4IDAAAADQAQCdASoBAAEAAgA0JaACdLoB+AADsAD+8MQL/yC5YXXI1/8gP+QH/ID/+PIAAAA="
        );
    }
}
