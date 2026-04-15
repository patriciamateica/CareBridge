package com.carebridge.backend.security;

public record RegisterRequest(
    String firstName,
    String lastName,
    String email,
    String password,
    String phoneNumber
) {}
