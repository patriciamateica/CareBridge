package com.carebridge.backend.appointments.model;

import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentsDto(
    UUID id,
    UUID patientId,
    UUID nurseId,
    String description,
    LocalDateTime timeSlot,
    AppointmentsStatus status
) {}
