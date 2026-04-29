package com.carebridge.backend.clinicalLog.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import org.hibernate.validator.constraints.URL;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public record ClinicalLogDto(
    UUID id,

    @NotBlank(message = "Document title is mandatory")
    String documentTitle,

    @NotNull(message = "Document type is mandatory")
    DocumentType documentType,

    @NotNull(message = "Date performed is mandatory")
    @PastOrPresent(message = "Performance date cannot be in the future")
    LocalDate datePerformed,

    @NotBlank(message = "File URL is mandatory")
    @URL(message = "File URL must be a valid link")
    String fileUrl,

    @NotNull(message = "Patient ID is mandatory")
    UUID patientId,

    @NotNull(message = "Nurse ID is mandatory")
    UUID nurseId,

    @NotNull(message = "Upload timestamp is mandatory")
    LocalDateTime uploadTimestamp,

    @NotNull(message = "Status is mandatory")
    ClinicalLogStatus status
) {}
