package com.carebridge.backend.roster.model;

import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record RosterDto(
    UUID id,

    @NotNull(message = "Patient ID is mandatory")
    UUID patientId,

    @NotNull(message = "Nurse ID is mandatory")
    UUID nurseId,

    @NotNull(message = "Roster status is mandatory")
    RosterStatus status
) {}
