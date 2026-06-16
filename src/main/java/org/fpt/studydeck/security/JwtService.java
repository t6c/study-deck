package org.fpt.studydeck.security;

import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.Base64;
import java.util.Map;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
public class JwtService {

    private static final Base64.Encoder BASE64_URL_ENCODER = Base64.getUrlEncoder().withoutPadding();
    private static final Base64.Decoder BASE64_URL_DECODER = Base64.getUrlDecoder();
    private static final ObjectMapper OBJECT_MAPPER = new ObjectMapper();

    private final String secret;
    private final long expirationSeconds;

    public JwtService(
        @Value("${app.security.jwt.secret}") String secret,
        @Value("${app.security.jwt.expiration-seconds}") long expirationSeconds
    ) {
        this.secret = secret;
        this.expirationSeconds = expirationSeconds;
    }

    public String generateToken(String subject) {
        Instant now = Instant.now();
        String header = encodeJson(Map.of("alg", "HS256", "typ", "JWT"));
        String payload = encodeJson(Map.of(
            "sub", subject,
            "iat", now.getEpochSecond(),
            "exp", now.plusSeconds(expirationSeconds).getEpochSecond()
        ));
        String unsignedToken = header + "." + payload;
        return unsignedToken + "." + sign(unsignedToken);
    }

    public String extractSubject(String token) {
        Map<String, Object> claims = parseAndValidate(token);
        return String.valueOf(claims.get("sub"));
    }

    public boolean isValid(String token, String expectedSubject) {
        Map<String, Object> claims = parseAndValidate(token);
        return expectedSubject.equals(String.valueOf(claims.get("sub")));
    }

    public long getExpirationSeconds() {
        return expirationSeconds;
    }

    private Map<String, Object> parseAndValidate(String token) {
        String[] parts = token.split("\\.");
        if (parts.length != 3) {
            throw new InvalidJwtException("Invalid JWT.");
        }

        String unsignedToken = parts[0] + "." + parts[1];
        String expectedSignature = sign(unsignedToken);
        if (!constantTimeEquals(expectedSignature, parts[2])) {
            throw new InvalidJwtException("Invalid JWT signature.");
        }

        try {
            Map<String, Object> claims = OBJECT_MAPPER.readValue(
                BASE64_URL_DECODER.decode(parts[1]),
                new TypeReference<>() {
                }
            );
            long expiresAt = ((Number) claims.get("exp")).longValue();
            if (Instant.now().getEpochSecond() >= expiresAt) {
                throw new InvalidJwtException("JWT has expired.");
            }
            return claims;
        } catch (InvalidJwtException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new InvalidJwtException("Invalid JWT payload.", exception);
        }
    }

    private String encodeJson(Map<String, Object> value) {
        try {
            return BASE64_URL_ENCODER.encodeToString(OBJECT_MAPPER.writeValueAsBytes(value));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to encode JWT.", exception);
        }
    }

    private String sign(String unsignedToken) {
        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(secret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            return BASE64_URL_ENCODER.encodeToString(mac.doFinal(unsignedToken.getBytes(StandardCharsets.UTF_8)));
        } catch (Exception exception) {
            throw new IllegalStateException("Unable to sign JWT.", exception);
        }
    }

    private boolean constantTimeEquals(String left, String right) {
        byte[] leftBytes = left.getBytes(StandardCharsets.UTF_8);
        byte[] rightBytes = right.getBytes(StandardCharsets.UTF_8);
        if (leftBytes.length != rightBytes.length) {
            return false;
        }
        int result = 0;
        for (int i = 0; i < leftBytes.length; i++) {
            result |= leftBytes[i] ^ rightBytes[i];
        }
        return result == 0;
    }
}
