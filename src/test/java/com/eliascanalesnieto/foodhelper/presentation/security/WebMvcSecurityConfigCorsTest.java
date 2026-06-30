package com.eliascanalesnieto.foodhelper.presentation.security;

import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.options;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.header;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.eliascanalesnieto.foodhelper.application.JwtService;
import com.eliascanalesnieto.foodhelper.application.MediaUrlService;
import java.util.List;
import java.util.Map;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;
import org.springframework.http.HttpHeaders;
import org.springframework.mock.web.MockServletContext;
import org.springframework.core.env.MapPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.support.AnnotationConfigWebApplicationContext;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;

class WebMvcSecurityConfigCorsTest {

    private static final String ORIGIN = "http://localhost:5173";
    private static final String LAN_ORIGIN = "http://192.168.1.133";

    private AnnotationConfigWebApplicationContext context;
    private MockMvc mockMvc;
    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        context = new AnnotationConfigWebApplicationContext();
        context.setServletContext(new MockServletContext());
        context.getEnvironment().getPropertySources().addFirst(new MapPropertySource(
                "cors-test",
                Map.of("app.cors.allowed-origin-patterns", "http://localhost:*,http://127.0.0.1:*,http://192.168.1.133,https://food.example.com")
        ));
        context.register(TestConfig.class);
        context.refresh();

        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
        jwtService = context.getBean(JwtService.class);
    }

    @AfterEach
    void tearDown() {
        context.close();
    }

    @Test
    void browserRequestsFromLocalFrontendShouldPassCorsChecks() throws Exception {
        when(jwtService.isValid("test-token")).thenReturn(true);

        mockMvc.perform(options("/api/v1/users/1/weights")
                        .header(HttpHeaders.ORIGIN, ORIGIN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGIN))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, org.hamcrest.Matchers.containsString("GET")))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, org.hamcrest.Matchers.containsString("authorization")));

        mockMvc.perform(get("/api/v1/users/1/weights")
                        .header(HttpHeaders.ORIGIN, ORIGIN)
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGIN))
                .andExpect(content().json("[]"));

        mockMvc.perform(options("/api/v1/users/1/weights")
                        .header(HttpHeaders.ORIGIN, ORIGIN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "DELETE")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, ORIGIN))
                .andExpect(header().string(
                        HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS,
                        org.hamcrest.Matchers.containsString("DELETE")
                ));
    }

    @Test
    void browserRequestsFromConfiguredProductionOriginShouldPassCorsChecks() throws Exception {
        when(jwtService.isValid("test-token")).thenReturn(true);

        mockMvc.perform(get("/api/v1/products")
                        .header(HttpHeaders.ORIGIN, "https://food.example.com")
                        .header(HttpHeaders.AUTHORIZATION, "Bearer test-token"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, "https://food.example.com"));
    }

    @Test
    void browserPreflightFromLanOriginShouldPassCorsChecks() throws Exception {
        mockMvc.perform(options("/api/v1/users/1/weights")
                        .header(HttpHeaders.ORIGIN, LAN_ORIGIN)
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_METHOD, "GET")
                        .header(HttpHeaders.ACCESS_CONTROL_REQUEST_HEADERS, "authorization,content-type"))
                .andExpect(status().isOk())
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_ORIGIN, LAN_ORIGIN))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_METHODS, org.hamcrest.Matchers.containsString("GET")))
                .andExpect(header().string(HttpHeaders.ACCESS_CONTROL_ALLOW_HEADERS, org.hamcrest.Matchers.containsString("authorization")));
    }

    @Configuration
    @EnableWebMvc
    @Import({WebMvcSecurityConfig.class, JwtAuthenticationInterceptor.class, TestProductsController.class})
    static class TestConfig {
        @Bean
        JwtService jwtService() {
            return Mockito.mock(JwtService.class);
        }

        @Bean
        MediaUrlService mediaUrlService() {
            return Mockito.mock(MediaUrlService.class);
        }
    }

    @RestController
    static class TestProductsController {
        @GetMapping("/api/v1/products")
        List<String> listProducts() {
            return List.of();
        }

        @GetMapping("/api/v1/users/{userId}/weights")
        List<String> listWeights(@PathVariable Long userId) {
            return List.of();
        }

        @DeleteMapping("/api/v1/products")
        void deleteProducts() {
        }
    }
}
