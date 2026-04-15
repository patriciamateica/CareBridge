package com.carebridge.backend.nursedetails;

import com.carebridge.backend.nursedetails.model.NurseDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class NurseDetailsRepositoryTest {

    private NurseDetailsRepository repository;
    private NurseDetails sampleDetails;

    @BeforeEach
    void setUp() {
        repository = new NurseDetailsRepository();
        sampleDetails = new NurseDetails(
            null,
            UUID.randomUUID(),
            "Pediatrics",
            "Central Hospital",
            5,
            true
        );
    }

    @Test
    void save_ShouldGenerateIdAndStoreNurseDetails() {
        NurseDetails saved = repository.save(sampleDetails);

        assertNotNull(saved.getId());
        assertEquals("Pediatrics", saved.getSpecialization());
        assertTrue(repository.findById(saved.getId()).isPresent());
    }

    @Test
    void findById_ShouldReturnNurseDetailsWhenExists() {
        NurseDetails saved = repository.save(sampleDetails);

        Optional<NurseDetails> found = repository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    void findById_ShouldReturnEmptyWhenNotExists() {
        Optional<NurseDetails> found = repository.findById(UUID.randomUUID());

        assertTrue(found.isEmpty());
    }

    @Test
    void findAll_ShouldReturnCorrectPage() {
        for (int i = 0; i < 5; i++) {
            repository.save(new NurseDetails(null, UUID.randomUUID(), "Spec " + i, "Hospital", 2, false));
        }

        Page<NurseDetails> page = repository.findAll(PageRequest.of(0, 2));

        assertEquals(2, page.getContent().size());
        assertEquals(5, page.getTotalElements());
        assertEquals(3, page.getTotalPages());
    }

    @Test
    void findAll_ShouldReturnEmptyIfOutOfBounds() {
        repository.save(sampleDetails);

        Page<NurseDetails> page = repository.findAll(PageRequest.of(5, 10));

        assertTrue(page.getContent().isEmpty());
    }

    @Test
    void deleteById_ShouldRemoveNurseDetails() {
        NurseDetails saved = repository.save(sampleDetails);

        repository.deleteById(saved.getId());

        assertTrue(repository.findById(saved.getId()).isEmpty());
    }
}
