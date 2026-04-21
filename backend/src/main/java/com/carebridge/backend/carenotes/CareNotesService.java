package com.carebridge.backend.carenotes;

import com.carebridge.backend.carenotes.model.CareNotes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.UUID;

@Service
public class CareNotesService {
    private final CareNotesRepository repository;
    private final Sinks.Many<CareNotes> noteSink = Sinks.many().multicast().onBackpressureBuffer();

    public CareNotesService(CareNotesRepository repository) {
        this.repository = repository;
    }

    public Page<CareNotes> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Page<CareNotes> findByPatientId(UUID patientId, Pageable pageable) {
        return repository.findByPatientId(patientId, pageable);
    }

    public CareNotes getById(UUID id) {
        return repository.findById(id).orElseThrow();
    }

    public CareNotes create(CareNotes careNotes) {
        CareNotes saved = repository.save(careNotes);
        emitIfPresent(saved);
        return saved;
    }

    public CareNotes update(UUID id, CareNotes updatedNotes) {
        CareNotes oldNotes = repository.findById(id).orElseThrow();

        oldNotes.setContent(updatedNotes.getContent());
        oldNotes.setPatientId(updatedNotes.getPatientId());
        oldNotes.setNurseId(updatedNotes.getNurseId());
        oldNotes.setTimestamp(updatedNotes.getTimestamp());

        CareNotes saved = repository.save(oldNotes);
        emitIfPresent(saved);
        return saved;
    }

    public boolean delete(UUID id) {
        repository.findById(id).orElseThrow();
        repository.deleteById(id);
        return true;
    }

    public void deleteAll() {
        repository.deleteAll();
    }

    public List<CareNotes> getByPatientId(UUID patientId) {
        return repository.findByPatientId(patientId);
    }

    public long countByPatientId(UUID patientId) {
        return repository.countByPatientId(patientId);
    }

    public Flux<CareNotes> getNoteStream(UUID patientId) {
        return noteSink.asFlux()
                .filter(n -> patientId == null || n.getPatientId().equals(patientId));
    }

    private void emitIfPresent(CareNotes notes) {
        if (notes == null) {
            return;
        }
        noteSink.tryEmitNext(notes);
    }
}
