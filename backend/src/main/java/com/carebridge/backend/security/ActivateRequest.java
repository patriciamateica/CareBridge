package com.carebridge.backend.security;

public record ActivateRequest(
    String email,
    String activationNumber
) {}
