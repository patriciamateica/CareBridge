package com.carebridge.backend.prescription.model;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.util.UUID;

public record PrescriptionDto(
    UUID id,

    @NotBlank(message = "Medication name is mandatory")
    String name,

    @NotBlank(message = "Dosage instructions are mandatory")
    String dose,

    @NotBlank(message = "Timing/Frequency is mandatory")
    String timing,

    @NotNull(message = "Patient ID is mandatory")
    UUID patientId,

    @NotNull(message = "Nurse ID (Prescriber) is mandatory")
    UUID nurseId
) {}
