package com.carebridge.backend.clinicalLog;

import com.carebridge.backend.clinicalLog.model.ClinicalLog;
import com.carebridge.backend.clinicalLog.model.ClinicalLogStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class ClinicalLogService {
    private final ClinicalLogRepository repository;
    private final Sinks.Many<ClinicalLog> logSink = Sinks.many().multicast().onBackpressureBuffer();

    public ClinicalLogService(ClinicalLogRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<ClinicalLog> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public ClinicalLog getById(UUID id) {
        return repository.findById(id).orElseThrow();
    }

    @Transactional
    public ClinicalLog create(ClinicalLog clinicalLog) {
        if (clinicalLog.getStatus() == null) {
            clinicalLog.setStatus(ClinicalLogStatus.ACTIVE);
        }
        ClinicalLog saved = repository.save(clinicalLog);
        logSink.tryEmitNext(saved);
        return saved;
    }

    @Transactional
    public ClinicalLog update(UUID id, ClinicalLog updatedLog) {
        ClinicalLog oldLog = repository.findById(id).orElseThrow();

        oldLog.setDocumentTitle(updatedLog.getDocumentTitle());
        oldLog.setDocumentType(updatedLog.getDocumentType());
        oldLog.setDatePerformed(updatedLog.getDatePerformed());
        oldLog.setFileUrl(updatedLog.getFileUrl());
        oldLog.setPatient(updatedLog.getPatient());
        oldLog.setNurse(updatedLog.getNurse());
        oldLog.setUploadTimestamp(updatedLog.getUploadTimestamp());
        oldLog.setStatus(updatedLog.getStatus());

        ClinicalLog saved = repository.save(oldLog);
        logSink.tryEmitNext(saved);
        return saved;
    }

    @Transactional
    public boolean delete(UUID id) {
        ClinicalLog log = repository.findById(id).orElseThrow();
        log.setStatus(ClinicalLogStatus.DELETED);

        ClinicalLog saved = repository.save(log);
        logSink.tryEmitNext(saved);
        return true;
    }

    @Transactional
    public void deleteAll() {
        repository.deleteAll();
    }

    @Transactional(readOnly = true)
    public List<ClinicalLog> getByPatientId(UUID patientId) {
        return repository.findByPatientId(patientId);
    }

    @Transactional
    public Flux<ClinicalLog> getLogStream(UUID patientId) {
        return logSink.asFlux()
            .filter(l -> patientId == null || l.getPatient().getId().equals(patientId));
    }
}
