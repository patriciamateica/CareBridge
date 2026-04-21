package com.carebridge.backend.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SecurityModelRecordsTest {

    @Test
    void cookieProperties_ShouldReadAndWriteAllFields() {
        CookieProperties properties = new CookieProperties();

        properties.setHttpOnly(true);
        properties.setSecure(false);
        properties.setSameSite("Lax");
        properties.setPath("/graphql");
        properties.setMaxAgeHours(12);

        assertTrue(properties.isHttpOnly());
        assertFalse(properties.isSecure());
        assertEquals("Lax", properties.getSameSite());
        assertEquals("/graphql", properties.getPath());
        assertEquals(12, properties.getMaxAgeHours());
    }

    @Test
    void loginRequest_ShouldExposeRecordComponents() {
        LoginRequest request = new LoginRequest("nurse@carebridge.local", "secret");

        assertEquals("nurse@carebridge.local", request.email());
        assertEquals("secret", request.password());
    }

    @Test
    void registerRequest_ShouldExposeRecordComponents() {
        RegisterRequest request = new RegisterRequest("Ana", "Pop", "ana@carebridge.local", "pw", "0712345678");

        assertEquals("Ana", request.firstName());
        assertEquals("Pop", request.lastName());
        assertEquals("ana@carebridge.local", request.email());
        assertEquals("pw", request.password());
        assertEquals("0712345678", request.phoneNumber());
    }

    @Test
    void activateRequest_ShouldExposeRecordComponents() {
        ActivateRequest request = new ActivateRequest("ana@carebridge.local", "123456");

        assertEquals("ana@carebridge.local", request.email());
        assertEquals("123456", request.activationNumber());
    }

    @Test
    void authResponse_ShouldExposeRecordComponents() {
        AuthResponse response = new AuthResponse("token", "NURSE", "Ana", "Pop");

        assertEquals("token", response.token());
        assertEquals("NURSE", response.role());
        assertEquals("Ana", response.firstName());
        assertEquals("Pop", response.lastName());
    }
}

