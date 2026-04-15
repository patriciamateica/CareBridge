package com.carebridge.backend.clinicalLog.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ClinicalLogDto(
    UUID id,
    String documentTitle,
    DocumentType documentType,
    LocalDate datePerformed,
    String fileUrl,
    UUID patientId,
    UUID nurseId,
    LocalDateTime uploadTimestamp,
    ClinicalLogStatus status
) {}
