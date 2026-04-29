package com.carebridge.backend.vitals.model;

import com.carebridge.backend.user.model.User;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.UUID;

@Entity
@Table(name = "vitals")
public class Vitals {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "heart_rate", nullable = false)
    private int heartRate;

    @Column(name = "blood_pressure", nullable = false)
    private int bloodPressure;

    @Column(name = "respiratory_rate", nullable = false)
    private int respiratoryRate;

    @Column(nullable = false)
    private int spO2;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @Column(nullable = false)
    private LocalDate timestamp;

    public Vitals() {
    }

    public Vitals(
        UUID id,
        LocalDate timestamp,
        int heartRate,
        int bloodPressure,
        int respiratoryRate,
        int spO2,
        User patient
    ) {
        this.id = id;
        this.timestamp = timestamp;
        this.heartRate = heartRate;
        this.bloodPressure = bloodPressure;
        this.respiratoryRate = respiratoryRate;
        this.spO2 = spO2;
        this.patient = patient;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public LocalDate getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDate timestamp) {
        this.timestamp = timestamp;
    }

    public User getPatient() {
        return patient;
    }

    public void setPatient(User patient) {
        this.patient = patient;
    }

    public int getRespiratoryRate() {
        return respiratoryRate;
    }

    public void setRespiratoryRate(int respiratoryRate) {
        this.respiratoryRate = respiratoryRate;
    }

    public int getSpO2() {
        return spO2;
    }

    public void setSpO2(int spO2) {
        this.spO2 = spO2;
    }

    public int getBloodPressure() {
        return bloodPressure;
    }

    public void setBloodPressure(int bloodPressure) {
        this.bloodPressure = bloodPressure;
    }

    public int getHeartRate() {
        return heartRate;
    }

    public void setHeartRate(int heartRate) {
        this.heartRate = heartRate;
    }
}
