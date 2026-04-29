package com.carebridge.backend.appointments.model;

import jakarta.validation.constraints.Future;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.time.LocalDateTime;
import java.util.UUID;

public record AppointmentsDto(
    UUID id,

    @NotNull(message = "Patient ID is mandatory")
    UUID patientId,

    @NotNull(message = "Nurse ID is mandatory")
    UUID nurseId,

    @NotBlank(message = "Description cannot be empty")
    @Size(max = 500, message = "Description must be under 500 characters")
    String description,

    @NotNull(message = "Time slot is mandatory")
    @Future(message = "Appointment must be in the future")
    LocalDateTime timeSlot,

    @NotNull(message = "Status is mandatory")
    AppointmentsStatus status
) {}
