package com.carebridge.backend.carenotes;

import com.carebridge.backend.carenotes.model.CareNotes;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CareNotesService {
    private final CareNotesRepository repository;

    public CareNotesService(CareNotesRepository repository) {
        this.repository = repository;
    }

    public Page<CareNotes> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public CareNotes getById(UUID id) {
        return repository.findById(id).orElseThrow();
    }

    public CareNotes create(CareNotes careNotes) {
        return repository.save(careNotes);
    }

    public CareNotes update(UUID id, CareNotes updatedNotes) {
        CareNotes oldNotes = repository.findById(id).orElseThrow();

        oldNotes.setContent(updatedNotes.getContent());
        oldNotes.setPatientId(updatedNotes.getPatientId());
        oldNotes.setNurseId(updatedNotes.getNurseId());
        oldNotes.setTimestamp(updatedNotes.getTimestamp());

        return oldNotes;
    }

    public boolean delete(UUID id) {
        repository.findById(id).orElseThrow();
        repository.deleteById(id);
        return true;
    }
}
