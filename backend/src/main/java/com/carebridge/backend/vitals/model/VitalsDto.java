package com.carebridge.backend.vitals.model;

import jakarta.validation.constraints.*;
import java.time.LocalDate;
import java.util.UUID;

public record VitalsDto(
    UUID id,

    @NotNull(message = "Timestamp is mandatory")
    @PastOrPresent(message = "Vitals cannot be recorded for the future")
    LocalDate timestamp,

    @Positive(message = "Heart rate must be positive")
    @Max(value = 300, message = "Invalid heart rate value")
    int heartRate,

    @Positive(message = "Blood pressure must be positive")
    int bloodPressure,

    @Positive(message = "Respiratory rate must be positive")
    int respiratoryRate,

    @Min(value = 0)
    @Max(value = 100, message = "SpO2 cannot exceed 100%")
    int spO2,

    @NotNull(message = "Patient ID is mandatory")
    UUID patientId
) {}
