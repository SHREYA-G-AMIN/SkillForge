package com.skillforge.security;

import com.skillforge.entity.Role;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Base64;

import static org.junit.jupiter.api.Assertions.*;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        String secret = Base64.getEncoder()
                .encodeToString("test-secret-key-test-secret-key-1234".getBytes());
        ReflectionTestUtils.setField(jwtService, "secret", secret);
        ReflectionTestUtils.setField(jwtService, "expirationMs", 60_000L);
    }

    @Test
    void generatedToken_containsEmailAndRole() {
        String token = jwtService.generateToken("a@b.com", Role.STUDENT);

        assertEquals("a@b.com", jwtService.extractEmail(token));
        assertEquals("STUDENT", jwtService.extractRole(token));
    }

    @Test
    void tamperedToken_isRejected() {
        String token = jwtService.generateToken("a@b.com", Role.STUDENT);
        String tampered = token.substring(0, token.length() - 3) + "abc";

        assertThrows(JwtException.class, () -> jwtService.extractEmail(tampered));
    }

    @Test
    void expiredToken_isRejected() {
        ReflectionTestUtils.setField(jwtService, "expirationMs", -1_000L);
        String token = jwtService.generateToken("a@b.com", Role.STUDENT);

        assertThrows(ExpiredJwtException.class, () -> jwtService.extractEmail(token));
    }

    @Test
    void everyToken_hasUniqueId() {
        String t1 = jwtService.generateToken("a@b.com", Role.STUDENT);
        String t2 = jwtService.generateToken("a@b.com", Role.STUDENT);

        assertNotNull(jwtService.parseClaims(t1).getId());
        assertNotEquals(jwtService.parseClaims(t1).getId(), jwtService.parseClaims(t2).getId());
    }

    @Test
    void weakSecret_failsStartupValidation() {
        ReflectionTestUtils.setField(jwtService, "secret", Base64.getEncoder().encodeToString("short".getBytes()));

        assertThrows(IllegalStateException.class, () -> ReflectionTestUtils.invokeMethod(jwtService, "validateSecret"));
    }
}
