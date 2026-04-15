package com.carebridge.backend.clinicalLog;

import com.carebridge.backend.clinicalLog.model.ClinicalLog;
import com.carebridge.backend.clinicalLog.model.ClinicalLogDto;
import org.springframework.stereotype.Component;

@Component
public class ClinicalLogMapper {

    public ClinicalLogDto toDto(ClinicalLog log) {
        return new ClinicalLogDto(
            log.getId(),
            log.getDocumentTitle(),
            log.getDocumentType(),
            log.getDatePerformed(),
            log.getFileUrl(),
            log.getPatientId(),
            log.getNurseId(),
            log.getUploadTimestamp(),
            log.getStatus()
        );
    }

    public ClinicalLog toEntity(ClinicalLogDto dto) {
        return new ClinicalLog(
            dto.id(),
            dto.documentTitle(),
            dto.documentType(),
            dto.datePerformed(),
            dto.fileUrl(),
            dto.patientId(),
            dto.nurseId(),
            dto.uploadTimestamp(),
            dto.status()
        );
    }
}
