package com.carebridge.backend.security;

public record LoginRequest(
    String email,
    String password
) {}
