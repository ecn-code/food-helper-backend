package com.eliascanalesnieto.foodhelper.application;

import com.eliascanalesnieto.foodhelper.domain.Media;
import java.nio.charset.StandardCharsets;
import java.time.Clock;
import java.time.Instant;
import java.util.HexFormat;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
public class MediaUrlService {
    private static final String HMAC_ALGORITHM = "HmacSHA256";
    private static final String MEDIA_PATH_PREFIX = "/api/v1/media/";

    private final Clock clock;
    private final byte[] secret;
    private final long expirationSeconds;

    public MediaUrlService(
            @Value("${app.auth.jwt.secret}") String secret,
            @Value("${app.auth.jwt.expiration-seconds:3600}") long expirationSeconds
    ) {
        if (!StringUtils.hasText(secret) || secret.length() < 32) {
            throw new IllegalArgumentException("Media URL signing secret must contain at least 32 characters");
        }
        this.clock = Clock.systemUTC();
        this.secret = secret.getBytes(StandardCharsets.UTF_8);
        this.expirationSeconds = expirationSeconds;
    }

    public String signedUrl(Media media) {
        if (media == null) {
            return null;
        }
        long expiresAt = Instant.now(clock).plusSeconds(expirationSeconds).getEpochSecond();
        String path = MEDIA_PATH_PREFIX + media.getId();
        return path + "?expiresAt=" + expiresAt + "&signature=" + signature(path, expiresAt);
    }

    public boolean isValid(Long mediaId, String expiresAtValue, String signature) {
        if (mediaId == null || !StringUtils.hasText(expiresAtValue) || !StringUtils.hasText(signature)) {
            return false;
        }
        try {
            long expiresAt = Long.parseLong(expiresAtValue);
            if (Instant.now(clock).getEpochSecond() >= expiresAt) {
                return false;
            }
            return constantTimeEquals(signature(MEDIA_PATH_PREFIX + mediaId, expiresAt), signature);
        } catch (NumberFormatException ex) {
            return false;
        }
    }

    private String signature(String path, long expiresAt) {
        try {
            Mac mac = Mac.getInstance(HMAC_ALGORITHM);
            mac.init(new SecretKeySpec(secret, HMAC_ALGORITHM));
            return HexFormat.of().formatHex(mac.doFinal((path + ":" + expiresAt).getBytes(StandardCharsets.UTF_8)));
        } catch (Exception ex) {
            throw new IllegalStateException("Unable to sign media URL", ex);
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
}
