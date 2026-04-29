package com.carebridge.backend.carenotes;

import com.carebridge.backend.carenotes.model.CareNotes;
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
public class CareNotesService {
    private final CareNotesRepository repository;
    private final Sinks.Many<CareNotes> noteSink = Sinks.many().multicast().onBackpressureBuffer();

    public CareNotesService(CareNotesRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<CareNotes> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<CareNotes> findByPatientId(UUID patientId, Pageable pageable) {
        return repository.findByPatientIdOrderByTimestampDesc(patientId, pageable);
    }

    @Transactional(readOnly = true)
    public CareNotes getById(UUID id) {
        return repository.findById(id).orElseThrow();
    }

    @Transactional
    public CareNotes create(CareNotes careNotes) {
        CareNotes saved = repository.save(careNotes);
        emitIfPresent(saved);
        return saved;
    }

    @Transactional
    public CareNotes update(UUID id, CareNotes updatedNotes) {
        CareNotes oldNotes = repository.findById(id).orElseThrow();

        oldNotes.setContent(updatedNotes.getContent());
        oldNotes.setPatient(updatedNotes.getPatient());
        oldNotes.setNurse(updatedNotes.getNurse());
        oldNotes.setTimestamp(updatedNotes.getTimestamp());

        CareNotes saved = repository.save(oldNotes);
        emitIfPresent(saved);
        return saved;
    }

    @Transactional
    public boolean delete(UUID id) {
        repository.findById(id).orElseThrow();
        repository.deleteById(id);
        return true;
    }

    @Transactional
    public void deleteAll() {
        repository.deleteAll();
    }

    @Transactional(readOnly = true)
    public List<CareNotes> getByPatientId(UUID patientId) {
        return repository.findByPatientIdOrderByTimestampDesc(patientId);
    }

    @Transactional(readOnly = true)
    public long countByPatientId(UUID patientId) {
        return repository.countByPatientId(patientId);
    }

    @Transactional
    public Flux<CareNotes> getNoteStream(UUID patientId) {
        return noteSink.asFlux()
            .filter(n -> patientId == null || n.getPatient().getId().equals(patientId));
    }

    @Transactional
    public void emitIfPresent(CareNotes notes) {
        if (notes == null) {
            return;
        }
        noteSink.tryEmitNext(notes);
    }
}
