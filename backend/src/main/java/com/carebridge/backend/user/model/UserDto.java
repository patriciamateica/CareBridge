package com.carebridge.backend.user.model;

import com.carebridge.backend.user.Role;
import com.carebridge.backend.user.UserStatus;
import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.UUID;

public record UserDto(
    UUID id,

    String activationNumber,

    @NotBlank(message = "First name is mandatory")
    String firstName,

    @NotBlank(message = "Last name is mandatory")
    String lastName,

    @Email(message = "Invalid email format")
    @NotBlank(message = "Email is mandatory")
    String email,

    @Digits(integer = 15, fraction = 0, message = "Invalid phone number format")
    int phoneNumber,

    @Past(message = "Date of birth must be in the past")
    LocalDate dateOfBirth,

    String residentialAddress,

    String nationality,

    @NotNull(message = "Role is mandatory")
    Role role,

    UserStatus userStatus
) {}
