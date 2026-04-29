package com.carebridge.backend.appointments.model;

import com.carebridge.backend.user.model.User;
import jakarta.persistence.*;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "appointments")
public class Appointments {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nurse_id", nullable = false)
    private User nurse;

    @Column(length = 500)
    private String description;

    @Column(name = "time_slot", nullable = false)
    private LocalDateTime timeSlot;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private AppointmentsStatus status;

    public Appointments() {
    }

    public Appointments(
        UUID id,
        User patient,
        User nurse,
        String description,
        LocalDateTime timeSlot,
        AppointmentsStatus status
    ) {
        this.id = id;
        this.patient = patient;
        this.nurse = nurse;
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

    public User getPatient() {
        return patient;
    }

    public void setPatient(User patient) {
        this.patient = patient;
    }

    public User getNurse() {
        return nurse;
    }

    public void setNurse(User nurse) {
        this.nurse = nurse;
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
