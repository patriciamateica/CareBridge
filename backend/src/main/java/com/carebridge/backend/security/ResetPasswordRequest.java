package com.carebridge.backend.security;

public record ResetPasswordRequest(
    String email,
    String otp,
    String newPassword
) {}
