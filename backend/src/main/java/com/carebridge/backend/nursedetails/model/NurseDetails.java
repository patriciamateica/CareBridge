package com.carebridge.backend.nursedetails.model;

import java.util.UUID;

public class NurseDetails {
    private UUID id;
    private UUID userId;
    private String specialization;
    private String hospitalAffiliation;
    private int experienceYears;
    private boolean hireMeStatus;

    public NurseDetails(
        UUID id,
        UUID userId,
        String specialization,
        String hospitalAffiliation,
        int experienceYears,
        boolean hireMeStatus
    ) {
        this.id = id;
        this.userId = userId;
        this.specialization = specialization;
        this.hospitalAffiliation = hospitalAffiliation;
        this.experienceYears = experienceYears;
        this.hireMeStatus = hireMeStatus;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getUserId() {
        return userId;
    }

    public void setUserId(UUID userId) {
        this.userId = userId;
    }

    public String getSpecialization() {
        return specialization;
    }

    public void setSpecialization(String specialization) {
        this.specialization = specialization;
    }

    public String getHospitalAffiliation() {
        return hospitalAffiliation;
    }

    public void setHospitalAffiliation(String hospitalAffiliation) {
        this.hospitalAffiliation = hospitalAffiliation;
    }

    public int getExperienceYears() {
        return experienceYears;
    }

    public void setExperienceYears(int experienceYears) {
        this.experienceYears = experienceYears;
    }

    public boolean isHireMeStatus() {
        return hireMeStatus;
    }

    public void setHireMeStatus(boolean hireMeStatus) {
        this.hireMeStatus = hireMeStatus;
    }
}
