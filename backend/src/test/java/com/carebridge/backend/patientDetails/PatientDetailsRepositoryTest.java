package com.carebridge.backend.patientDetails;

import com.carebridge.backend.patientDetails.model.PatientDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PatientDetailsRepositoryTest {

    private PatientDetailsRepository repository;
    private PatientDetails sampleDetails;

    @BeforeEach
    void setUp() {
        repository = new PatientDetailsRepository();
        sampleDetails = new PatientDetails(
            null,
            UUID.randomUUID(),
            "Hypertension",
            List.of("Blood Test", "ECG"),
            List.of("/scans/ecg_01.png"),
            "Wife: 0722123456",
            UUID.randomUUID()
        );
    }

    @Test
    void save_ShouldGenerateIdAndStorePatientDetails() {
        PatientDetails saved = repository.save(sampleDetails);

        assertNotNull(saved.getId());
        assertEquals("Hypertension", saved.getPrimaryDiagnosis());
        assertTrue(repository.findById(saved.getId()).isPresent());
    }

    @Test
    void findById_ShouldReturnPatientDetailsWhenExists() {
        PatientDetails saved = repository.save(sampleDetails);

        Optional<PatientDetails> found = repository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    void findById_ShouldReturnEmptyWhenNotExists() {
        Optional<PatientDetails> found = repository.findById(UUID.randomUUID());

        assertTrue(found.isEmpty());
    }

    @Test
    void findAll_ShouldReturnCorrectPage() {
        for (int i = 0; i < 5; i++) {
            repository.save(new PatientDetails(null, UUID.randomUUID(),
                "Diagnosis " + i, List.of(), List.of(), "Contact", UUID.randomUUID()));
        }

        Page<PatientDetails> page = repository.findAll(PageRequest.of(0, 2));

        assertEquals(2, page.getContent().size());
        assertEquals(5, page.getTotalElements());
        assertEquals(3, page.getTotalPages());
    }

    @Test
    void findAll_ShouldReturnEmptyIfOutOfBounds() {
        repository.save(sampleDetails);

        Page<PatientDetails> page = repository.findAll(PageRequest.of(5, 10));

        assertTrue(page.getContent().isEmpty());
    }

    @Test
    void deleteById_ShouldRemovePatientDetails() {
        PatientDetails saved = repository.save(sampleDetails);

        repository.deleteById(saved.getId());

        assertTrue(repository.findById(saved.getId()).isEmpty());
    }
}
