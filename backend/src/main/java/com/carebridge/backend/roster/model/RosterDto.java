package com.carebridge.backend.roster.model;

import java.util.UUID;

public record RosterDto(
    UUID id,
    UUID patientId,
    UUID nurseId,
    RosterStatus status
) {}
