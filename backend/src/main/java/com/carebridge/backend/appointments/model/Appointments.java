package com.carebridge.backend.appointments.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Appointments {
    private UUID id;
    private UUID patientId;
    private UUID nurseId;
    private String description;
    private LocalDateTime timeSlot;
    private AppointmentsStatus status;

    public Appointments(
        UUID id,
        UUID patientId,
        UUID nurseId,
        String description,
        LocalDateTime timeSlot,
        AppointmentsStatus status
    ) {
        this.id = id;
        this.patientId = patientId;
        this.nurseId = nurseId;
        this.description = description;
        this.timeSlot = timeSlot;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public UUID getNurseId() {
        return nurseId;
    }

    public void setNurseId(UUID nurseId) {
        this.nurseId = nurseId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public LocalDateTime getTimeSlot() {
        return timeSlot;
    }

    public void setTimeSlot(LocalDateTime timeSlot) {
        this.timeSlot = timeSlot;
    }

    public AppointmentsStatus getStatus() {
        return status;
    }

    public void setStatus(AppointmentsStatus status) {
        this.status = status;
    }
}
