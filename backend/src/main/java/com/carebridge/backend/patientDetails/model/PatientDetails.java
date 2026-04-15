package com.carebridge.backend.patientDetails.model;

import java.util.List;
import java.util.UUID;

public class PatientDetails {
    private UUID id;
    private UUID userId; // Linked to Base User
    private String primaryDiagnosis;
    private List<String> diagnostics;
    private List<String> scans; // URLs or file paths
    private String emergencyContact;
    private UUID assignedNurseId;

    public PatientDetails(
        UUID id,
        UUID userId,
        String primaryDiagnosis,
        List<String> diagnostics,
        List<String> scans,
        String emergencyContact,
        UUID assignedNurseId
    ) {
        this.id = id;
        this.userId = userId;
        this.primaryDiagnosis = primaryDiagnosis;
        this.diagnostics = diagnostics;
        this.scans = scans;
        this.emergencyContact = emergencyContact;
        this.assignedNurseId = assignedNurseId;
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

    public String getPrimaryDiagnosis() {
        return primaryDiagnosis;
    }

    public void setPrimaryDiagnosis(String primaryDiagnosis) {
        this.primaryDiagnosis = primaryDiagnosis;
    }

    public List<String> getDiagnostics() {
        return diagnostics;
    }

    public void setDiagnostics(List<String> diagnostics) {
        this.diagnostics = diagnostics;
    }

    public List<String> getScans() {
        return scans;
    }

    public void setScans(List<String> scans) {
        this.scans = scans;
    }

    public String getEmergencyContact() {
        return emergencyContact;
    }

    public void setEmergencyContact(String emergencyContact) {
        this.emergencyContact = emergencyContact;
    }

    public UUID getAssignedNurseId() {
        return assignedNurseId;
    }

    public void setAssignedNurseId(UUID assignedNurseId) {
        this.assignedNurseId = assignedNurseId;
    }
}
