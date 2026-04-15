package com.carebridge.backend.healthStatus;

import com.carebridge.backend.healthStatus.model.HealthStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class HealthStatusRepositoryTest {

    private HealthStatusRepository repository;
    private HealthStatus sampleStatus;

    @BeforeEach
    void setUp() {
        repository = new com.carebridge.backend.healthStatus.HealthStatusRepository();
        sampleStatus = new HealthStatus(
            null,
            5,
            null,
            List.of("Headache", "Nausea"),
            "Patient feels a bit dizzy",
            LocalDate.now(),
            UUID.randomUUID()
        );
    }

    @Test
    void save_ShouldGenerateIdAndStoreHealthStatus() {
        HealthStatus saved = repository.save(sampleStatus);

        assertNotNull(saved.getId());
        assertEquals(5, saved.getPainScale());
        assertTrue(repository.findById(saved.getId()).isPresent());
    }

    @Test
    void findById_ShouldReturnHealthStatusWhenExists() {
        HealthStatus saved = repository.save(sampleStatus);

        Optional<HealthStatus> found = repository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    void findById_ShouldReturnEmptyWhenNotExists() {
        Optional<HealthStatus> found = repository.findById(UUID.randomUUID());

        assertTrue(found.isEmpty());
    }

    @Test
    void findAll_ShouldReturnCorrectPage() {
        for (int i = 0; i < 5; i++) {
            repository.save(new HealthStatus(null, i, null, List.of("Cough"), "Notes", LocalDate.now(), UUID.randomUUID()));
        }

        Page<HealthStatus> page = repository.findAll(PageRequest.of(0, 2));

        assertEquals(2, page.getContent().size());
        assertEquals(5, page.getTotalElements());
        assertEquals(3, page.getTotalPages());
    }

    @Test
    void findAll_ShouldReturnEmptyIfOutOfBounds() {
        repository.save(sampleStatus);

        Page<HealthStatus> page = repository.findAll(PageRequest.of(5, 10));

        assertTrue(page.getContent().isEmpty());
    }

    @Test
    void deleteById_ShouldRemoveHealthStatus() {
        HealthStatus saved = repository.save(sampleStatus);

        repository.deleteById(saved.getId());

        assertTrue(repository.findById(saved.getId()).isEmpty());
    }
}
