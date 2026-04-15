package com.carebridge.backend.clinicalLog.model;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

public class ClinicalLog {
    private UUID id;
    private String documentTitle;
    private DocumentType documentType;
    private LocalDate datePerformed;
    private String fileUrl;
    private UUID patientId;
    private UUID nurseId;
    private LocalDateTime uploadTimestamp;
    private ClinicalLogStatus status;

    public ClinicalLog(
        UUID id,
        String documentTitle,
        DocumentType documentType,
        LocalDate datePerformed,
        String fileUrl,
        UUID patientId,
        UUID nurseId,
        LocalDateTime uploadTimestamp,
        ClinicalLogStatus status
    ) {
        this.id = id;
        this.documentTitle = documentTitle;
        this.documentType = documentType;
        this.datePerformed = datePerformed;
        this.fileUrl = fileUrl;
        this.patientId = patientId;
        this.nurseId = nurseId;
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
