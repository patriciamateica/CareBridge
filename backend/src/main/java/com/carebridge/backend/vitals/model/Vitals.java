package com.carebridge.backend.vitals.model;

import java.time.LocalDate;
import java.util.UUID;

public class Vitals {
    private UUID id;
    private int heartRate;
    private int bloodPressure;
    private int respiratoryRate;
    private int spO2;

    private UUID patientId;
    private LocalDate timestamp;

    public Vitals(
        UUID id,
        LocalDate timestamp,
        int heartRate,
        int bloodPressure,
        int respiratoryRate,
        int spO2,
        UUID patientId
    ) {
        this.id = id;
        this.timestamp = timestamp;
        this.heartRate = heartRate;
        this.bloodPressure = bloodPressure;
        this.respiratoryRate = respiratoryRate;
        this.spO2 = spO2;
        this.patientId = patientId;
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

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
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
