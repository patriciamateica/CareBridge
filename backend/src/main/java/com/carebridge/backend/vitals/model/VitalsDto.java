package com.carebridge.backend.vitals.model;

import java.time.LocalDate;
import java.util.UUID;

public record VitalsDto
    (UUID id,
     LocalDate timestamp,
     int heartRate,
     int bloodPressure,
     int respiratoryRate,
     int spO2,
     UUID patientId) {
}
