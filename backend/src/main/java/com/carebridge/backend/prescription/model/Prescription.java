package com.carebridge.backend.prescription.model;

import com.carebridge.backend.user.model.User;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "prescriptions")
public class Prescription {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false)
    private String dose;

    @Column(nullable = false)
    private String timing;

    @Column(name = "refills_left")
    private int refillsLeft;

    @Column(name = "next_refill_date")
    private LocalDate nextRefillDate;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nurse_id", nullable = false)
    private User nurse;

    public Prescription() {
    }

    public Prescription(UUID id, String name, String dose, String timing, User patient, User nurse) {
        this.id = id;
        this.name = name;
        this.dose = dose;
        this.timing = timing;
        this.patient = patient;
        this.nurse = nurse;
    }

    public Prescription(UUID id, String name, String dose, String timing, int refillsLeft, LocalDate nextRefillDate, User patient, User nurse) {
        this.id = id;
        this.name = name;
        this.dose = dose;
        this.timing = timing;
        this.refillsLeft = refillsLeft;
        this.nextRefillDate = nextRefillDate;
        this.patient = patient;
        this.nurse = nurse;
    }

    public int getRefillsLeft() { return refillsLeft; }
    public void setRefillsLeft(int refillsLeft) { this.refillsLeft = refillsLeft; }
    public LocalDate getNextRefillDate() { return nextRefillDate; }
    public void setNextRefillDate(LocalDate nextRefillDate) { this.nextRefillDate = nextRefillDate; }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDose() {
        return dose;
    }

    public void setDose(String dose) {
        this.dose = dose;
    }

    public String getTiming() {
        return timing;
    }

    public void setTiming(String timing) {
        this.timing = timing;
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
}
