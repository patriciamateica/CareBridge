package com.carebridge.backend.healthStatus.model;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class HealthStatus {
    private UUID id;
    private int painScale;
    private Mood mood;
    private List<String> symptoms;
    private String notes;
    private LocalDate timestamp;
    private UUID patientId;

    public HealthStatus(UUID id, int painScale, Mood mood, List<String> symptoms, String notes, LocalDate timestamp, UUID patientId) {
        this.id = id;
        this.painScale = painScale;
        this.mood = mood;
        this.symptoms = symptoms;
        this.notes = notes;
        this.timestamp = timestamp;
        this.patientId = patientId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public int getPainScale() {
        return painScale;
    }

    public void setPainScale(int painScale) {
        this.painScale = painScale;
    }

    public Mood getMood() {
        return mood;
    }

    public void setMood(Mood mood) {
        this.mood = mood;
    }

    public List<String> getSymptoms() {
        return symptoms;
    }

    public void setSymptoms(List<String> symptoms) {
        this.symptoms = symptoms;
    }

    public String getNotes() {
        return notes;
    }

    public void setNotes(String notes) {
        this.notes = notes;
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
}
