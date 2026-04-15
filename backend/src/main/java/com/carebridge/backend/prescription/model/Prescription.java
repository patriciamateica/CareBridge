package com.carebridge.backend.prescription.model;

import java.util.UUID;

public class Prescription {
    private UUID id;
    private String name;
    private String dose;
    private String timing;
    private UUID patientId;
    private UUID nurseId;

    public Prescription(UUID id, String name, String dose, String timing, UUID patientId, UUID nurseId) {
        this.id = id;
        this.name = name;
        this.dose = dose;
        this.timing = timing;
        this.patientId = patientId;
        this.nurseId = nurseId;
    }

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
}
