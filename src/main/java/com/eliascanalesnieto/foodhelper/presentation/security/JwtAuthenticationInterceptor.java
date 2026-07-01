package com.eliascanalesnieto.foodhelper.presentation.security;

import com.eliascanalesnieto.foodhelper.application.MediaUrlService;
import com.eliascanalesnieto.foodhelper.application.JwtService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.time.Instant;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.cors.CorsUtils;
import org.springframework.web.servlet.HandlerInterceptor;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationInterceptor implements HandlerInterceptor {
    private final JwtService jwtService;
    private final MediaUrlService mediaUrlService;

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if ("OPTIONS".equalsIgnoreCase(request.getMethod())) {
            return true;
        }
        if (CorsUtils.isPreFlightRequest(request)) {
            return true;
        }
        if (isPublic(request)) {
            return true;
        }
        String token = bearerToken(request.getHeader("Authorization"));
        if (jwtService.isValid(token)) {
            return true;
        }
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.getWriter().write("""
                {"timestamp":"%s","status":401,"error":"Unauthorized","message":"Missing or invalid Bearer token","path":"%s"}
                """.formatted(Instant.now(), request.getRequestURI()).trim());
        return false;
    }

    private boolean isPublic(HttpServletRequest request) {
        String path = request.getRequestURI();
        return path.equals("/api/v1/health")
                || path.startsWith("/api/v1/auth/")
                || path.equals("/swagger")
                || path.startsWith("/swagger-ui")
                || path.startsWith("/v3/api-docs")
                || path.startsWith("/actuator/health")
                || isValidSignedMediaRequest(path, request);
    }

    private boolean isValidSignedMediaRequest(String path, HttpServletRequest request) {
        if (!path.startsWith("/api/v1/media/")) {
            return false;
        }
        try {
            Long mediaId = Long.valueOf(path.substring(path.lastIndexOf('/') + 1));
            return mediaUrlService.isValid(mediaId, request.getParameter("expiresAt"), request.getParameter("signature"));
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private String bearerToken(String authorizationHeader) {
        if (!StringUtils.hasText(authorizationHeader) || !authorizationHeader.startsWith("Bearer ")) {
            return null;
        }
        return authorizationHeader.substring("Bearer ".length());
    }
}
