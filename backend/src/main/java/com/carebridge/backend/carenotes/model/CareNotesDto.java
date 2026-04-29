package com.carebridge.backend.carenotes.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

public record CareNotesDto(
    UUID id,

    @NotBlank(message = "Note content cannot be empty")
    @Size(min = 10, max = 2000, message = "Note must be between 10 and 2000 characters")
    String content,

    @NotNull(message = "Patient ID is mandatory")
    UUID patientId,

    @NotNull(message = "Nurse ID is mandatory")
    UUID nurseId,

    @NotNull(message = "Timestamp is mandatory")
    @PastOrPresent(message = "Note timestamp cannot be in the future")
    LocalDateTime timestamp
) {}
