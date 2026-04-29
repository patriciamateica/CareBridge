package com.carebridge.backend.healthStatus.model;

import com.carebridge.backend.user.model.User;
import jakarta.persistence.*;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "healthstatus")
public class HealthStatus {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "pain_scale", nullable = false)
    private int painScale;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Mood mood;

    @ElementCollection
    @CollectionTable(name = "health_status_symptoms", joinColumns = @JoinColumn(name = "health_status_id"))
    @Column(name = "symptom")
    private List<String> symptoms;

    @Column(length = 1000)
    private String notes;

    @Column(nullable = false)
    private LocalDate timestamp;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    public HealthStatus() {
    }

    public HealthStatus(UUID id, int painScale, Mood mood, List<String> symptoms, String notes, LocalDate timestamp, User patient) {
        this.id = id;
        this.painScale = painScale;
        this.mood = mood;
        this.symptoms = symptoms;
        this.notes = notes;
        this.timestamp = timestamp;
        this.patient = patient;
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

    public User getPatient() {
        return patient;
    }

    public void setPatient(User patient) {
        this.patient = patient;
    }
}
