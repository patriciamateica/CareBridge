package com.carebridge.backend.clinicalLog;

import com.carebridge.backend.clinicalLog.model.ClinicalLog;
import com.carebridge.backend.clinicalLog.model.ClinicalLogStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.UUID;

@Service
public class ClinicalLogService {
    private final ClinicalLogRepository repository;
    private final Sinks.Many<ClinicalLog> logSink = Sinks.many().multicast().onBackpressureBuffer();

    public ClinicalLogService(ClinicalLogRepository repository) {
        this.repository = repository;
    }

    public Page<ClinicalLog> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public ClinicalLog getById(UUID id) {
        return repository.findById(id).orElseThrow();
    }

    public ClinicalLog create(ClinicalLog clinicalLog) {
        if (clinicalLog.getStatus() == null) {
            clinicalLog.setStatus(ClinicalLogStatus.ACTIVE);
        }
        ClinicalLog saved = repository.save(clinicalLog);
        logSink.tryEmitNext(saved);
        return saved;
    }

    public ClinicalLog update(UUID id, ClinicalLog updatedLog) {
        ClinicalLog oldLog = repository.findById(id).orElseThrow();

        oldLog.setDocumentTitle(updatedLog.getDocumentTitle());
        oldLog.setDocumentType(updatedLog.getDocumentType());
        oldLog.setDatePerformed(updatedLog.getDatePerformed());
        oldLog.setFileUrl(updatedLog.getFileUrl());
        oldLog.setPatientId(updatedLog.getPatientId());
        oldLog.setNurseId(updatedLog.getNurseId());
        oldLog.setUploadTimestamp(updatedLog.getUploadTimestamp());
        oldLog.setStatus(updatedLog.getStatus());

        return oldLog;
    }

    // SOFT DELETE COMPLIANCE
    public boolean delete(UUID id) {
        ClinicalLog log = repository.findById(id).orElseThrow();
        log.setStatus(ClinicalLogStatus.DELETED);
        // We DO NOT call repository.deleteById(id) to preserve the record
        return true;
    }

    public List<ClinicalLog> getByPatientId(UUID patientId) {
        return repository.findByPatientId(patientId);
    }

    public Flux<ClinicalLog> getLogStream(UUID patientId) {
        return logSink.asFlux()
                .filter(l -> patientId == null || l.getPatientId().equals(patientId));
    }
}
