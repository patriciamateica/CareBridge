package com.carebridge.backend.clinicalLog;

import com.carebridge.backend.clinicalLog.model.ClinicalLog;
import com.carebridge.backend.clinicalLog.model.ClinicalLogDto;
import com.carebridge.backend.user.model.User;
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
            log.getPatient().getId(),
            log.getNurse().getId(),
            log.getUploadTimestamp(),
            log.getStatus()
        );
    }

    public ClinicalLog toEntity(ClinicalLogDto dto, User nurse, User patient) {
        return new ClinicalLog(
            dto.id(),
            dto.documentTitle(),
            dto.documentType(),
            dto.datePerformed(),
            dto.fileUrl(),
            patient,
            nurse,
            dto.uploadTimestamp(),
            dto.status()
        );
    }
}
