package com.carebridge.backend.roster.model;

import com.carebridge.backend.user.model.User;
import jakarta.persistence.*;
import java.util.UUID;

@Entity
@Table(name = "roster")
public class Roster {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nurse_id", nullable = false)
    private User nurse;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private RosterStatus status;

    public Roster() {
    }

    public Roster(UUID id, User patient, User nurse, RosterStatus status) {
        this.id = id;
        this.patient = patient;
        this.nurse = nurse;
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

    public RosterStatus getStatus() {
        return status;
    }

    public void setStatus(RosterStatus status) {
        this.status = status;
    }
}
