package com.felipelima.clientmanager.security;

import static org.junit.jupiter.api.Assertions.*;

import java.util.Collections;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.User;
import org.springframework.test.util.ReflectionTestUtils;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider();
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret",
                "test-secret-key-que-deve-ser-longa-e-segura-2024");
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", 86400000L);
    }

    private Authentication createAuthentication(String username, String role) {
        User userDetails = new User(
                username,
                "password",
                Collections.singletonList(new SimpleGrantedAuthority(role)));
        return new UsernamePasswordAuthenticationToken(
                userDetails, null, userDetails.getAuthorities());
    }

    @Test
    @DisplayName("Should generate a non-null token")
    void generateToken() {
        Authentication auth = createAuthentication("admin", "ROLE_ADMIN");

        String token = jwtTokenProvider.generateToken(auth);

        assertNotNull(token);
        assertFalse(token.isEmpty());
    }

    @Test
    @DisplayName("Should extract username from token")
    void getUsernameFromToken() {
        Authentication auth = createAuthentication("admin", "ROLE_ADMIN");
        String token = jwtTokenProvider.generateToken(auth);

        String username = jwtTokenProvider.getUsernameFromToken(token);

        assertEquals("admin", username);
    }

    @Test
    @DisplayName("Should validate a valid token")
    void validateValidToken() {
        Authentication auth = createAuthentication("admin", "ROLE_ADMIN");
        String token = jwtTokenProvider.generateToken(auth);

        assertTrue(jwtTokenProvider.validateToken(token));
    }

    @Test
    @DisplayName("Should reject a malformed token")
    void validateMalformedToken() {
        assertFalse(jwtTokenProvider.validateToken("invalid-token"));
    }

    @Test
    @DisplayName("Should reject an expired token")
    void validateExpiredToken() {
        // Set expiration to 0ms (instant expiration)
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtExpiration", 0L);

        Authentication auth = createAuthentication("admin", "ROLE_ADMIN");
        String token = jwtTokenProvider.generateToken(auth);

        // Token is already expired
        assertFalse(jwtTokenProvider.validateToken(token));
    }

    @Test
    @DisplayName("Should reject token with wrong secret")
    void validateTokenWithWrongSecret() {
        Authentication auth = createAuthentication("admin", "ROLE_ADMIN");
        String token = jwtTokenProvider.generateToken(auth);

        // Change secret after generating
        ReflectionTestUtils.setField(jwtTokenProvider, "jwtSecret", "different-secret-key");

        assertFalse(jwtTokenProvider.validateToken(token));
    }

    @Test
    @DisplayName("Should reject empty token")
    void validateEmptyToken() {
        assertFalse(jwtTokenProvider.validateToken(""));
    }

    @Test
    @DisplayName("Should reject null token")
    void validateNullToken() {
        assertFalse(jwtTokenProvider.validateToken(null));
    }
}
