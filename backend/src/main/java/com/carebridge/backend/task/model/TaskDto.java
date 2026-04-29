package com.carebridge.backend.task.model;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

public record TaskDto(
    UUID id,

    @NotBlank(message = "Task title is mandatory")
    @Size(max = 100)
    String title,

    @Size(max = 1000)
    String description,

    @NotNull(message = "Task type is mandatory")
    TaskType taskType,

    @FutureOrPresent(message = "Task deadline cannot be in the past")
    LocalDateTime neededBy,

    @NotNull(message = "Task status is mandatory")
    TaskStatus status,

    @NotNull(message = "Patient ID is mandatory")
    UUID patientId,

    @NotNull(message = "Claimer ID is mandatory")
    UUID claimerId
) {}
