package com.carebridge.backend.carenotes.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class CareNotes {
    private UUID id;
    private String content;
    private UUID patientId;
    private UUID nurseId;
    private LocalDateTime timestamp;

    public CareNotes(UUID id, String content, UUID patientId, UUID nurseId, LocalDateTime timestamp) {
        this.id = id;
        this.content = content;
        this.patientId = patientId;
        this.nurseId = nurseId;
        this.timestamp = timestamp;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getContent() {
        return content;
    }

    public void setContent(String content) {
        this.content = content;
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

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    public void setTimestamp(LocalDateTime timestamp) {
        this.timestamp = timestamp;
    }
}
