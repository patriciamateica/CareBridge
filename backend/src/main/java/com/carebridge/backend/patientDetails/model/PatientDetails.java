package com.carebridge.backend.patientDetails.model;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.carebridge.backend.user.model.User;
import jakarta.persistence.*;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "patient_details")
public class PatientDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @OneToOne(fetch = FetchType.EAGER)
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnore
    private User user;

    @Column(name = "primary_diagnosis")
    private String primaryDiagnosis;

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "patient_diagnostics", joinColumns = @JoinColumn(name = "patient_details_id", referencedColumnName = "id"))
    @Column(name = "diagnostic")
    private List<String> diagnostics = new java.util.ArrayList<>();

    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "patient_scans", joinColumns = @JoinColumn(name = "patient_details_id", referencedColumnName = "id"))
    @Column(name = "scan_url")
    private List<String> scans = new java.util.ArrayList<>();

    @Column(name = "emergency_contact")
    private String emergencyContact;

    @Column(name = "assigned_nurse_id")
    private UUID assignedNurseId;

    @Column(name = "status")
    private String status;

    public PatientDetails() {
    }

    public PatientDetails(
        User user,
        String primaryDiagnosis,
        List<String> diagnostics,
        List<String> scans,
        String emergencyContact,
        UUID assignedNurseId,
        String status
    ) {
        this.user = user;
        this.primaryDiagnosis = primaryDiagnosis;
        this.diagnostics = diagnostics != null ? diagnostics : new java.util.ArrayList<>();
        this.scans = scans != null ? scans : new java.util.ArrayList<>();
        this.emergencyContact = emergencyContact;
        this.assignedNurseId = assignedNurseId;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
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
        this.diagnostics = diagnostics != null ? diagnostics : new java.util.ArrayList<>();
    }

    public List<String> getScans() {
        return scans;
    }

    public void setScans(List<String> scans) {
        this.scans = scans != null ? scans : new java.util.ArrayList<>();
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

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}
