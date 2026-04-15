package com.carebridge.backend.security;

public record AuthResponse(
    String token,
    String role,
    String firstName,
    String lastName
) {}

