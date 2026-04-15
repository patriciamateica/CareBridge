package com.carebridge.backend.prescription.model;

import java.util.UUID;

public record PrescriptionDto(
    UUID id,
    String name,
    String dose,
    String timing,
    UUID patientId,
    UUID nurseId
) {}
