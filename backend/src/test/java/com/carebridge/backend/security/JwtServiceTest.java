package com.carebridge.backend.security;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

import java.security.NoSuchAlgorithmException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class JwtServiceTest {

    private JwtService jwtService;

    @BeforeEach
    void setUp() throws NoSuchAlgorithmException {
        jwtService = new JwtService();
    }

    @Test
    void generateToken_ShouldProduceValidTokenForUser() {
        String token = jwtService.generateToken(
            "nurse@carebridge.local",
            List.of(new SimpleGrantedAuthority("NURSE"))
        );

        assertNotNull(token);
        assertTrue(jwtService.validate(token));
        assertEquals("nurse@carebridge.local", jwtService.getEmailFromToken(token));
    }

    @Test
    void generateToken_ShouldWorkWithEmptyAuthorities() {
        String token = jwtService.generateToken("patient@carebridge.local", List.of());

        assertNotNull(token);
        assertTrue(jwtService.validate(token));
        assertEquals("patient@carebridge.local", jwtService.getEmailFromToken(token));
    }

    @Test
    void validate_ShouldReturnFalseForInvalidToken() {
        assertFalse(jwtService.validate("this.is.not.a.valid.jwt"));
    }
}

