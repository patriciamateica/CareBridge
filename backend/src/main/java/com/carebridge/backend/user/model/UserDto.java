package com.carebridge.backend.user.model;

import com.carebridge.backend.user.Role;
import com.carebridge.backend.user.UserStatus;

import java.time.LocalDate;
import java.util.UUID;

public record UserDto(UUID id, String activationNumber, String firstName, String lastName, String email, int phoneNumber, LocalDate dateOfBirth, String residentialAddress, String nationality, Role role, UserStatus userStatus) {
}
