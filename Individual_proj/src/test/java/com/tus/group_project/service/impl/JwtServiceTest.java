package com.tus.group_project.service.impl;

import com.tus.group_project.service.IJwtService;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.SignatureAlgorithm;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.userdetails.UserDetails;

import java.security.Key;
import java.util.Base64;
import java.util.Date;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class JwtServiceTest {

    private JwtService jwtService;
    private final String testSecret = Base64.getEncoder().encodeToString("mysupersecretkeymysupersecretkey".getBytes());

    @BeforeEach
    void setUp() {
        jwtService = new JwtService();
        // inject the secret key manually since @Value doesn't work in unit tests
        var secretKeyField = getField(JwtService.class, "secretKey");
        secretKeyField.setAccessible(true);
        try {
            secretKeyField.set(jwtService, testSecret);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }
    }

    // Helper to set private fields via reflection
    private java.lang.reflect.Field getField(Class<?> clazz, String field) {
        try {
            return clazz.getDeclaredField(field);
        } catch (NoSuchFieldException e) {
            throw new RuntimeException(e);
        }
    }

    @Test
    void testGenerateAndValidateToken() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("testuser");

        String token = jwtService.generateToken(userDetails, "USER");

        assertNotNull(token, "Expected token to be generated, but got null");

        boolean isValid = jwtService.isTokenValid(token, userDetails);
        assertTrue(isValid, () -> "Expected token to be valid, but it was not");
    }

    @Test
    void testExtractEmail() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("test@email.com");

        String token = jwtService.generateToken(userDetails, "USER");

        String extractedEmail = jwtService.extractEmail(token);

        assertEquals("test@email.com", extractedEmail,
                () -> "Expected email: test@email.com, but got: " + extractedEmail);
    }

    @Test
    void testExtractRole() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("tester");

        String token = jwtService.generateToken(userDetails, "ADMIN");

        String role = jwtService.extractRole(token);

        assertEquals("ROLE_ADMIN", role,
                () -> "Expected role: ROLE_ADMIN, but got: " + role);
    }

    @Test
    void testTokenInvalidWhenEmailMismatch() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("actualuser");

        String token = jwtService.generateToken(userDetails, "USER");

        UserDetails otherUser = mock(UserDetails.class);
        when(otherUser.getUsername()).thenReturn("wronguser");

        boolean result = jwtService.isTokenValid(token, otherUser);

        assertFalse(result, () -> "Expected token to be invalid due to email mismatch");
    }

    @Test
    void testTokenInvalidWhenExpired() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("expireduser");

        // Manually inject secret key
        JwtService customJwtService = new JwtService();
        var secretField = getField(JwtService.class, "secretKey");
        secretField.setAccessible(true);
        try {
            secretField.set(customJwtService, testSecret);
        } catch (IllegalAccessException e) {
            throw new RuntimeException(e);
        }

        // Use getSigningKey via reflection
        Key signingKey;
        try {
            var getSigningKeyMethod = JwtService.class.getDeclaredMethod("getSigningKey");
            getSigningKeyMethod.setAccessible(true);
            signingKey = (Key) getSigningKeyMethod.invoke(customJwtService);
        } catch (Exception e) {
            throw new RuntimeException("Failed to access getSigningKey()", e);
        }

        // Create token expired 1 second ago
        String expiredToken = io.jsonwebtoken.Jwts.builder()
                .setSubject("expireduser")
                .setIssuedAt(new Date(System.currentTimeMillis() - 3600000))
                .setExpiration(new Date(System.currentTimeMillis() - 1000)) // expired
                .signWith(signingKey, SignatureAlgorithm.HS256)
                .compact();

        boolean result = customJwtService.isTokenValid(expiredToken, userDetails);

        assertFalse(result, "Expected expired token to be invalid");
    }


    @Test
    void testExtractAllClaimsContainsExpectedFields() {
        UserDetails userDetails = mock(UserDetails.class);
        when(userDetails.getUsername()).thenReturn("checkclaims");

        String token = jwtService.generateToken(userDetails, "USER");

        Claims claims = jwtService.extractAllClaims(token);

        assertEquals("checkclaims", claims.getSubject(), () ->
                "Expected subject: checkclaims, but got: " + claims.getSubject());
        assertEquals("ROLE_USER", claims.get("role"), () ->
                "Expected role claim: ROLE_USER, but got: " + claims.get("role"));
        assertNotNull(claims.getExpiration(), "Expected expiration date, but got null");
    }
}
