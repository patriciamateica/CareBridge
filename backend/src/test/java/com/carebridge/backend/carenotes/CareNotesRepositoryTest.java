package com.carebridge.backend.carenotes;

import com.carebridge.backend.carenotes.model.CareNotes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
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
    void save_ShouldPreserveProvidedId() {
        UUID fixedId = UUID.randomUUID();
        CareNotes notesWithId = new CareNotes(
            fixedId,
            "Existing ID",
            UUID.randomUUID(),
            UUID.randomUUID(),
            LocalDateTime.now()
        );

        CareNotes saved = repository.save(notesWithId);

        assertEquals(fixedId, saved.getId());
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

    @Test
    void findByPatientId_Pageable_ShouldFilterAndSortByTimestampDescending() {
        UUID patientId = UUID.randomUUID();
        UUID nurseId = UUID.randomUUID();

        CareNotes older = repository.save(new CareNotes(null, "older", patientId, nurseId, LocalDateTime.of(2026, 1, 1, 8, 0)));
        CareNotes newer = repository.save(new CareNotes(null, "newer", patientId, nurseId, LocalDateTime.of(2026, 1, 2, 8, 0)));
        repository.save(new CareNotes(null, "other", UUID.randomUUID(), nurseId, LocalDateTime.of(2026, 1, 3, 8, 0)));

        Page<CareNotes> page = repository.findByPatientId(patientId, PageRequest.of(0, 10));

        assertEquals(2, page.getTotalElements());
        assertEquals(List.of(newer.getId(), older.getId()), page.getContent().stream().map(CareNotes::getId).toList());
    }

    @Test
    void findByPatientId_Pageable_ShouldReturnEmptyWhenOutOfBounds() {
        UUID patientId = UUID.randomUUID();
        UUID nurseId = UUID.randomUUID();
        repository.save(new CareNotes(null, "only", patientId, nurseId, LocalDateTime.now()));

        Page<CareNotes> page = repository.findByPatientId(patientId, PageRequest.of(10, 5));

        assertTrue(page.getContent().isEmpty());
        assertEquals(1, page.getTotalElements());
    }

    @Test
    void findByPatientId_List_ShouldFilterAndSortByTimestampDescending() {
        UUID patientId = UUID.randomUUID();
        UUID nurseId = UUID.randomUUID();

        CareNotes oldest = repository.save(new CareNotes(null, "oldest", patientId, nurseId, LocalDateTime.of(2026, 2, 1, 8, 0)));
        CareNotes newest = repository.save(new CareNotes(null, "newest", patientId, nurseId, LocalDateTime.of(2026, 2, 3, 8, 0)));
        repository.save(new CareNotes(null, "middle", patientId, nurseId, LocalDateTime.of(2026, 2, 2, 8, 0)));
        repository.save(new CareNotes(null, "other-patient", UUID.randomUUID(), nurseId, LocalDateTime.of(2026, 2, 4, 8, 0)));

        List<CareNotes> result = repository.findByPatientId(patientId);

        assertEquals(3, result.size());
        assertEquals(newest.getId(), result.getFirst().getId());
        assertEquals(oldest.getId(), result.getLast().getId());
    }

    @Test
    void findByPatientId_List_ShouldHandleNullTimestampOrdering() {
        UUID patientId = UUID.randomUUID();
        UUID nurseId = UUID.randomUUID();

        CareNotes withNullTimestamp = repository.save(new CareNotes(null, "null-ts", patientId, nurseId, null));
        CareNotes withTimestamp = repository.save(new CareNotes(null, "valid-ts", patientId, nurseId, LocalDateTime.of(2026, 3, 1, 10, 0)));

        List<CareNotes> result = repository.findByPatientId(patientId);

        assertEquals(2, result.size());
        assertEquals(withTimestamp.getId(), result.getFirst().getId());
        assertEquals(withNullTimestamp.getId(), result.getLast().getId());
    }

    @Test
    void countByPatientId_ShouldCountOnlyMatchingPatient() {
        UUID patientId = UUID.randomUUID();
        UUID nurseId = UUID.randomUUID();

        repository.save(new CareNotes(null, "n1", patientId, nurseId, LocalDateTime.now()));
        repository.save(new CareNotes(null, "n2", patientId, nurseId, LocalDateTime.now()));
        repository.save(new CareNotes(null, "other", UUID.randomUUID(), nurseId, LocalDateTime.now()));

        long count = repository.countByPatientId(patientId);

        assertEquals(2, count);
    }

    @Test
    void deleteAll_ShouldClearRepository() {
        CareNotes saved = repository.save(sampleNotes);

        repository.deleteAll();

        assertTrue(repository.findById(saved.getId()).isEmpty());
        assertTrue(repository.findAll(PageRequest.of(0, 10)).isEmpty());
    }
}
