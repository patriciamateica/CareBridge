package com.carebridge.backend.vitals;

import com.carebridge.backend.vitals.model.Vitals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class VitalsRepositoryTest {

    private com.carebridge.backend.vitals.VitalsRepository repository;
    private Vitals sampleVitals;

    @BeforeEach
    void setUp() {
        repository = new VitalsRepository();
        sampleVitals = new Vitals(null, LocalDate.now(), 80, 120, 16, 98, UUID.randomUUID());
    }

    @Test
    void save_ShouldGenerateIdAndStoreVitals() {
        Vitals saved = repository.save(sampleVitals);

        assertNotNull(saved.getId());
        assertEquals(80, saved.getHeartRate());
        assertTrue(repository.findById(saved.getId()).isPresent());
    }

    @Test
    void findById_ShouldReturnVitalsWhenExists() {
        Vitals saved = repository.save(sampleVitals);

        Optional<Vitals> found = repository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    void findById_ShouldReturnEmptyWhenNotExists() {
        Optional<Vitals> found = repository.findById(UUID.randomUUID());

        assertTrue(found.isEmpty());
    }

    @Test
    void findAllPaginated_ShouldReturnCorrectPage() {
        for (int i = 0; i < 5; i++) {
            repository.save(new Vitals(null, LocalDate.now(), 80 + i, 120, 16, 98, UUID.randomUUID()));
        }

        Page<Vitals> page = repository.findAll(PageRequest.of(0, 2));

        assertEquals(2, page.getContent().size());
        assertEquals(5, page.getTotalElements());
        assertEquals(3, page.getTotalPages());
    }

    @Test
    void findAllPaginated_ShouldReturnEmptyIfOutOfBounds() {
        repository.save(sampleVitals);

        Page<Vitals> page = repository.findAll(PageRequest.of(5, 10));

        assertTrue(page.getContent().isEmpty());
    }

    @Test
    void deleteById_ShouldRemoveVitals() {
        Vitals saved = repository.save(sampleVitals);

        repository.deleteById(saved.getId());

        assertTrue(repository.findById(saved.getId()).isEmpty());
    }
}
