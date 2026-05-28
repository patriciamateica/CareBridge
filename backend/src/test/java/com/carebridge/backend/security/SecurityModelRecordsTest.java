package com.carebridge.backend.security;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SecurityModelRecordsTest {


    @Test
    void cookieProperties_DefaultsShouldBeSecure() {
        CookieProperties props = new CookieProperties();

        assertTrue(props.isHttpOnly(),  "httpOnly must default to true");
        assertTrue(props.isSecure(),    "secure must default to true (HTTPS)");
        assertEquals("Strict", props.getSameSite());
        assertEquals("/",  props.getPath());
        assertEquals(24L,  props.getMaxAgeHours());
    }

    @Test
    void cookieProperties_SettersShouldUpdateAllFields() {
        CookieProperties props = new CookieProperties();

        props.setHttpOnly(false);
        props.setSecure(false);
        props.setSameSite("Lax");
        props.setPath("/api");
        props.setMaxAgeHours(12);

        assertFalse(props.isHttpOnly());
        assertFalse(props.isSecure());
        assertEquals("Lax", props.getSameSite());
        assertEquals("/api", props.getPath());
        assertEquals(12L, props.getMaxAgeHours());
    }


    @Test
    void loginRequest_ShouldExposeRecordComponents() {
        LoginRequest request = new LoginRequest("nurse@carebridge.local", "secret");

        assertEquals("nurse@carebridge.local", request.email());
        assertEquals("secret", request.password());
    }

    @Test
    void registerRequest_ShouldExposeRecordComponents() {
        RegisterRequest request = new RegisterRequest(
            "Ana", "Pop", "ana@carebridge.local", "pw", "0712345678");

        assertEquals("Ana",  request.firstName());
        assertEquals("Pop",  request.lastName());
        assertEquals("ana@carebridge.local", request.email());
        assertEquals("pw",   request.password());
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
        assertEquals("Ana",   response.firstName());
        assertEquals("Pop",   response.lastName());
    }
}
