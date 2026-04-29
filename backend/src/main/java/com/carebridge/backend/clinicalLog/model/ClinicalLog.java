package com.carebridge.backend.clinicalLog.model;

import com.carebridge.backend.user.model.User;
import jakarta.persistence.*;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "clinical_logs")
public class ClinicalLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(length = 255)
    private String documentTitle;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private DocumentType documentType;

    @Column(name = "date_performed", nullable = false)
    private LocalDate datePerformed;

    @Column(name = "file_url", nullable = false)
    private String fileUrl;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "patient_id", nullable = false)
    private User patient;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "nurse_id", nullable = false)
    private User nurse;

    @Column(name = "upload_timestamp", nullable = false)
    private LocalDateTime uploadTimestamp;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private ClinicalLogStatus status;

    public ClinicalLog() {
    }

    public ClinicalLog(
        UUID id,
        String documentTitle,
        DocumentType documentType,
        LocalDate datePerformed,
        String fileUrl,
        User patient,
        User nurse,
        LocalDateTime uploadTimestamp,
        ClinicalLogStatus status
    ) {
        this.id = id;
        this.documentTitle = documentTitle;
        this.documentType = documentType;
        this.datePerformed = datePerformed;
        this.fileUrl = fileUrl;
        this.patient = patient;
        this.nurse = nurse;
        this.uploadTimestamp = uploadTimestamp;
        this.status = status;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getDocumentTitle() {
        return documentTitle;
    }

    public void setDocumentTitle(String documentTitle) {
        this.documentTitle = documentTitle;
    }

    public DocumentType getDocumentType() {
        return documentType;
    }

    public void setDocumentType(DocumentType documentType) {
        this.documentType = documentType;
    }

    public LocalDate getDatePerformed() {
        return datePerformed;
    }

    public void setDatePerformed(LocalDate datePerformed) {
        this.datePerformed = datePerformed;
    }

    public String getFileUrl() {
        return fileUrl;
    }

    public void setFileUrl(String fileUrl) {
        this.fileUrl = fileUrl;
    }

    public User getPatient() {
        return patient;
    }

    public void setPatient(User patient) {
        this.patient = patient;
    }

    public User getNurse() {
        return nurse;
    }

    public void setNurse(User nurse) {
        this.nurse = nurse;
    }

    public LocalDateTime getUploadTimestamp() {
        return uploadTimestamp;
    }

    public void setUploadTimestamp(LocalDateTime uploadTimestamp) {
        this.uploadTimestamp = uploadTimestamp;
    }

    public ClinicalLogStatus getStatus() {
        return status;
    }

    public void setStatus(ClinicalLogStatus status) {
        this.status = status;
    }
}
