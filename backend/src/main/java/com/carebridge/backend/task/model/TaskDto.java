package com.carebridge.backend.task.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record TaskDto(
    UUID id,
    String title,
    String description,
    TaskType taskType,
    LocalDateTime neededBy,
    TaskStatus status,
    UUID patientId,
    UUID claimerId
) {}
