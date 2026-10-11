package com.skillforge.security;

import com.skillforge.entity.Role;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Service
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration-ms}")
    private long expirationMs;

    /** Fail fast at startup if JWT_SECRET is missing/weak instead of failing on the first login. */
    @PostConstruct
    void validateSecret() {
        byte[] key;
        try {
            key = Decoders.BASE64.decode(secret);
        } catch (RuntimeException e) {
            throw new IllegalStateException("JWT_SECRET must be Base64 encoded", e);
        }
        if (key.length < 32) {
            throw new IllegalStateException("JWT_SECRET must decode to at least 32 bytes. Generate one with: openssl rand -base64 48");
        }
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(secret));
    }

    public String generateToken(String email, Role role) {
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .id(UUID.randomUUID().toString())          // jti: lets us revoke this exact token on logout
                .subject(email)
                .claim("role", role.name())
                .issuedAt(new Date(now))
                .expiration(new Date(now + expirationMs))
                .signWith(signingKey())
                .compact();
    }

    /** Verifies signature + expiry. Throws JwtException if the token is invalid. */
    public Claims parseClaims(String token) {
        return Jwts.parser().verifyWith(signingKey()).build().parseSignedClaims(token).getPayload();
    }

    public String extractEmail(String token) {
        return parseClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return parseClaims(token).get("role", String.class);
    }
}
