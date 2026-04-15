package com.carebridge.backend.prescription;

import com.carebridge.backend.prescription.model.Prescription;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class PrescriptionRepositoryTest {

    private com.carebridge.backend.prescription.PrescriptionRepository repository;
    private Prescription samplePrescription;

    @BeforeEach
    void setUp() {
        repository = new PrescriptionRepository();
        samplePrescription = new Prescription(
            null,
            "Amoxicillin",
            "500mg",
            "Every 8 hours",
            UUID.randomUUID(),
            UUID.randomUUID()
        );
    }

    @Test
    void save_ShouldGenerateIdAndStorePrescription() {
        Prescription saved = repository.save(samplePrescription);

        assertNotNull(saved.getId());
        assertEquals("Amoxicillin", saved.getName());
        assertTrue(repository.findById(saved.getId()).isPresent());
    }

    @Test
    void findById_ShouldReturnPrescriptionWhenExists() {
        Prescription saved = repository.save(samplePrescription);

        Optional<Prescription> found = repository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    void findById_ShouldReturnEmptyWhenNotExists() {
        Optional<Prescription> found = repository.findById(UUID.randomUUID());

        assertTrue(found.isEmpty());
    }

    @Test
    void findAll_ShouldReturnCorrectPage() {
        for (int i = 0; i < 5; i++) {
            repository.save(new Prescription(null, "Drug " + i, "10mg",
                "Daily", UUID.randomUUID(), UUID.randomUUID()));
        }

        Page<Prescription> page = repository.findAll(PageRequest.of(0, 2));

        assertEquals(2, page.getContent().size());
        assertEquals(5, page.getTotalElements());
        assertEquals(3, page.getTotalPages());
    }

    @Test
    void findAll_ShouldReturnEmptyIfOutOfBounds() {
        repository.save(samplePrescription);

        Page<Prescription> page = repository.findAll(PageRequest.of(5, 10));

        assertTrue(page.getContent().isEmpty());
    }

    @Test
    void deleteById_ShouldRemovePrescription() {
        Prescription saved = repository.save(samplePrescription);

        repository.deleteById(saved.getId());

        assertTrue(repository.findById(saved.getId()).isEmpty());
    }
}
