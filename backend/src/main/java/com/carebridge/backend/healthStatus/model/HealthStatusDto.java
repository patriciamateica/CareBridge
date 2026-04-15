package com.carebridge.backend.healthStatus.model;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record HealthStatusDto(
    UUID id,
    int painScale,
    Mood mood,
    List<String> symptoms,
    String notes,
    LocalDate timestamp,
    UUID patientId
) {}
