package com.carebridge.backend.roster.model;

import java.util.UUID;

public class Roster {
    private UUID id;
    private UUID patientId;
    private UUID nurseId;
    private RosterStatus status;

    public Roster(UUID id, UUID patientId, UUID nurseId, RosterStatus status) {
        this.id = id;
        this.patientId = patientId;
        this.nurseId = nurseId;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
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

    public RosterStatus getStatus() {
        return status;
    }

    public void setStatus(RosterStatus status) {
        this.status = status;
    }
}
