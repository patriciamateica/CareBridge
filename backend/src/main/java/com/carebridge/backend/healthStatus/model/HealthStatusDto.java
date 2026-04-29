package com.carebridge.backend.healthStatus.model;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record HealthStatusDto(
    UUID id,

    @Min(value = 0, message = "Pain scale cannot be less than 0")
    @Max(value = 10, message = "Pain scale cannot exceed 10")
    int painScale,

    @NotNull(message = "Mood must be specified")
    Mood mood,

    @NotEmpty(message = "At least one symptom must be listed")
    List<String> symptoms,

    @Size(max = 1000, message = "Notes cannot exceed 1000 characters")
    String notes,

    @NotNull(message = "Timestamp is mandatory")
    @PastOrPresent(message = "Timestamp cannot be in the future")
    LocalDate timestamp,

    @NotNull(message = "Patient ID is mandatory")
    UUID patientId
) {}
