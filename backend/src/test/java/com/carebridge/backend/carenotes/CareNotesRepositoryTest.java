package com.carebridge.backend.carenotes;

import com.carebridge.backend.carenotes.model.CareNotes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CareNotesRepositoryTest {

    private CareNotesRepository repository;
    private CareNotes sampleNotes;

    @BeforeEach
    void setUp() {
        repository = new CareNotesRepository();
        sampleNotes = new CareNotes(
            null,
            "Patient responded well to medication.",
            UUID.randomUUID(),
            UUID.randomUUID(),
            LocalDateTime.now()
        );
    }

    @Test
    void save_ShouldGenerateIdAndStoreCareNotes() {
        CareNotes saved = repository.save(sampleNotes);

        assertNotNull(saved.getId());
        assertEquals("Patient responded well to medication.", saved.getContent());
        assertTrue(repository.findById(saved.getId()).isPresent());
    }

    @Test
    void findById_ShouldReturnCareNotesWhenExists() {
        CareNotes saved = repository.save(sampleNotes);

        Optional<CareNotes> found = repository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    void findById_ShouldReturnEmptyWhenNotExists() {
        Optional<CareNotes> found = repository.findById(UUID.randomUUID());

        assertTrue(found.isEmpty());
    }

    @Test
    void findAll_ShouldReturnCorrectPage() {
        for (int i = 0; i < 5; i++) {
            repository.save(new CareNotes(null, "Note " + i, UUID.randomUUID(), UUID.randomUUID(), LocalDateTime.now()));
        }

        Page<CareNotes> page = repository.findAll(PageRequest.of(0, 2));

        assertEquals(2, page.getContent().size());
        assertEquals(5, page.getTotalElements());
        assertEquals(3, page.getTotalPages());
    }

    @Test
    void findAll_ShouldReturnEmptyIfOutOfBounds() {
        repository.save(sampleNotes);

        Page<CareNotes> page = repository.findAll(PageRequest.of(5, 10));

        assertTrue(page.getContent().isEmpty());
    }

    @Test
    void deleteById_ShouldRemoveCareNotes() {
        CareNotes saved = repository.save(sampleNotes);

        repository.deleteById(saved.getId());

        assertTrue(repository.findById(saved.getId()).isEmpty());
    }
}
