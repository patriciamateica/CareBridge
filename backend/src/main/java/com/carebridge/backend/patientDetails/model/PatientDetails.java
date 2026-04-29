package com.carebridge.backend.patientDetails.model;

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

    @OneToOne(fetch = FetchType.LAZY)
    @MapsId
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(name = "primary_diagnosis")
    private String primaryDiagnosis;

    @ElementCollection
    @CollectionTable(name = "patient_diagnostics", joinColumns = @JoinColumn(name = "patient_details_id"))
    @Column(name = "diagnostic")
    private List<String> diagnostics;

    @ElementCollection
    @CollectionTable(name = "patient_scans", joinColumns = @JoinColumn(name = "patient_details_id"))
    @Column(name = "scan_url")
    private List<String> scans;

    @Column(name = "emergency_contact")
    private String emergencyContact;

    @Column(name = "assigned_nurse_id")
    private UUID assignedNurseId;

    public PatientDetails() {
    }

    public PatientDetails(
        User user,
        String primaryDiagnosis,
        List<String> diagnostics,
        List<String> scans,
        String emergencyContact,
        UUID assignedNurseId
    ) {
        this.id = user.getId();
        this.user = user;
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
