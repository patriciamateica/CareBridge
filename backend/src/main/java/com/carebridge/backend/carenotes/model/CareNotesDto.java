package com.carebridge.backend.carenotes.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record CareNotesDto(
    UUID id,
    String content,
    UUID patientId,
    UUID nurseId,
    LocalDateTime timestamp
) {}
