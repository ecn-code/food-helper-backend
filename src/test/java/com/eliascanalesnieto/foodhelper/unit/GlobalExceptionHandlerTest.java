package com.eliascanalesnieto.foodhelper.unit;

import static org.assertj.core.api.Assertions.assertThat;

import com.eliascanalesnieto.foodhelper.presentation.error.ApiError;
import com.eliascanalesnieto.foodhelper.presentation.error.GlobalExceptionHandler;
import jakarta.servlet.http.HttpServletRequest;
import java.lang.reflect.Method;
import org.junit.jupiter.api.Test;
import org.springframework.core.MethodParameter;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.web.bind.MissingServletRequestParameterException;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

class GlobalExceptionHandlerTest {
    private final GlobalExceptionHandler handler = new GlobalExceptionHandler();

    @Test
    void missingRequestParameterShouldReturnApiError() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/users/7/weights");

        var response = handler.handleMissingRequestParameter(
                new MissingServletRequestParameterException("from", "Instant"),
                request
        );

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        ApiError body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.status()).isEqualTo(400);
        assertThat(body.path()).isEqualTo("/api/v1/users/7/weights");
        assertThat(body.message()).contains("from");
    }

    @Test
    void typeMismatchShouldReturnApiError() throws Exception {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/users/7/weights");
        MethodParameter parameter = methodParameter("dummy", Long.class);

        var response = handler.handleTypeMismatch(
                new MethodArgumentTypeMismatchException("bad", Long.class, "from", parameter, new IllegalArgumentException("bad")),
                request
        );

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        ApiError body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.status()).isEqualTo(400);
        assertThat(body.path()).isEqualTo("/api/v1/users/7/weights");
        assertThat(body.message()).isEqualTo("Invalid from");
    }

    @Test
    void illegalArgumentShouldReturnApiError() {
        MockHttpServletRequest request = new MockHttpServletRequest("GET", "/api/v1/users/7/weights");

        var response = handler.handleIllegalArgument(new IllegalArgumentException("Period start must not be after period end"), request);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
        ApiError body = response.getBody();
        assertThat(body).isNotNull();
        assertThat(body.status()).isEqualTo(400);
        assertThat(body.path()).isEqualTo("/api/v1/users/7/weights");
        assertThat(body.message()).isEqualTo("Period start must not be after period end");
    }

    private MethodParameter methodParameter(String methodName, Class<?> parameterType) throws NoSuchMethodException {
        Method method = getClass().getDeclaredMethod(methodName, parameterType);
        return new MethodParameter(method, 0);
    }

    @SuppressWarnings("unused")
    private void dummy(Long value) {
    }
}
