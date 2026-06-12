package com.eliascanalesnieto.foodhelper.application;

import com.eliascanalesnieto.foodhelper.domain.AppUser;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import tools.jackson.databind.ObjectMapper;

@Service
public class JwtService {
    private static final String HMAC_ALGORITHM = "HmacSHA512";
    private static final String TOKEN_TYPE = "JWT";

    private final ObjectMapper objectMapper;
    private final Clock clock;
    private final String issuer;
    private final byte[] secret;
    private final long expirationSeconds;

    public JwtService(
            ObjectMapper objectMapper,
            @Value("${app.auth.jwt.secret}") String secret,
            @Value("${app.auth.jwt.issuer:foodhelper-api}") String issuer,
            @Value("${app.auth.jwt.expiration-seconds:3600}") long expirationSeconds
    ) {
        if (!StringUtils.hasText(secret) || secret.length() < 32) {
            throw new IllegalArgumentException("JWT secret must contain at least 32 characters");
        }
        this.objectMapper = objectMapper;
        this.clock = Clock.systemUTC();
        this.issuer = issuer;
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationSeconds = expirationSeconds;
    }

    public TokenIssue issue(AppUser user) {
        Instant expiresAt = Instant.now(clock).plusSeconds(expirationSeconds);
        String header = encodeJson(Map.of("alg", "HS512", "typ", TOKEN_TYPE));
        String payload = encodeJson(Map.of(
                "iss", issuer,
                "sub", user.getId().toString(),
                "username", user.getUsername(),
                "exp", expiresAt.getEpochSecond()
        ));
        String unsignedToken = header + "." + payload;
        return new TokenIssue(unsignedToken + "." + sign(unsignedToken), expiresAt);
    }

    public boolean isValid(String token) {
        try {
            Claims claims = validate(token);
            return claims.userId() != null;
        } catch (RuntimeException ex) {
            return false;
        }
    }

    public Claims validate(String token) {
        if (!StringUtils.hasText(token)) {
            throw new IllegalArgumentException("Token is required");
        }
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new IllegalArgumentException("Invalid token");
        }
        String unsignedToken = parts[0] + "." + parts[1];
        if (!constantTimeEquals(sign(unsignedToken), parts[2])) {
            throw new IllegalArgumentException("Invalid token signature");
        }
        Map<String, Object> claims = readPayload(parts[1]);
        if (!issuer.equals(claims.get("iss"))) {
            throw new IllegalArgumentException("Invalid token issuer");
        }
        long expiresAt = ((Number) claims.get("exp")).longValue();
        if (Instant.now(clock).getEpochSecond() >= expiresAt) {
            throw new IllegalArgumentException("Token expired");
        }
        return new Claims(Long.valueOf(claims.get("sub").toString()), claims.get("username").toString());
    }

    private String encodeJson(Map<String, Object> values) {
        try {
            return encode(objectMapper.writeValueAsBytes(values));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to encode token", ex);
        }
    }

    private Map<String, Object> readPayload(String payload) {
        try {
            return objectMapper.readValue(Base64.getUrlDecoder().decode(payload), Map.class);
        } catch (Exception ex) {
            throw new IllegalArgumentException("Invalid token payload", ex);
        }
    }

    private String sign(String value) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
            return encode(mac.doFinal(value.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to sign token", ex);
        }
    }

    private boolean constantTimeEquals(String expected, String actual) {
        byte[] expectedBytes = expected.getBytes(StandardCharsets.UTF_8);
        byte[] actualBytes = actual.getBytes(StandardCharsets.UTF_8);
        if (expectedBytes.length != actualBytes.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < expectedBytes.length; i++) {
            result |= expectedBytes[i] ^ actualBytes[i];
        }
        return result == 0;
    }

    private String encode(byte[] bytes) {
        return Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);
    }

    public record TokenIssue(String token, Instant expiresAt) {
    }

    public record Claims(Long userId, String username) {
    }
}
