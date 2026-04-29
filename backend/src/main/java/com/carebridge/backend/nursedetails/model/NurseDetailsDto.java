package com.carebridge.backend.nursedetails.model;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record NurseDetailsDto(
    UUID id,

    @NotNull(message = "User ID linkage is mandatory")
    UUID userId,

    @NotBlank(message = "Specialization is mandatory")
    String specialization,

    @NotBlank(message = "Hospital affiliation is mandatory")
    String hospitalAffiliation,

    @Min(value = 0, message = "Experience years cannot be negative")
    int experienceYears,

    boolean hireMeStatus
) {}
