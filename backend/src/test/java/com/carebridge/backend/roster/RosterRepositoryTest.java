package com.carebridge.backend.roster;

import com.carebridge.backend.roster.model.Roster;
import com.carebridge.backend.roster.model.RosterStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class RosterRepositoryTest {

    private RosterRepository repository;
    private Roster sampleRoster;

    @BeforeEach
    void setUp() {
        repository = new RosterRepository();
        sampleRoster = new Roster(
            null,
            UUID.randomUUID(),
            UUID.randomUUID(),
            RosterStatus.PENDING
        );
    }

    @Test
    void save_ShouldGenerateIdAndStoreRoster() {
        Roster saved = repository.save(sampleRoster);

        assertNotNull(saved.getId());
        assertEquals(RosterStatus.PENDING, saved.getStatus());
        assertTrue(repository.findById(saved.getId()).isPresent());
    }

    @Test
    void findById_ShouldReturnRosterWhenExists() {
        Roster saved = repository.save(sampleRoster);

        Optional<Roster> found = repository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    void findById_ShouldReturnEmptyWhenNotExists() {
        Optional<Roster> found = repository.findById(UUID.randomUUID());

        assertTrue(found.isEmpty());
    }

    @Test
    void findAll_ShouldReturnCorrectPage() {
        for (int i = 0; i < 5; i++) {
            repository.save(new Roster(null, UUID.randomUUID(), UUID.randomUUID(), RosterStatus.ACTIVE));
        }

        Page<Roster> page = repository.findAll(PageRequest.of(0, 2));

        assertEquals(2, page.getContent().size());
        assertEquals(5, page.getTotalElements());
        assertEquals(3, page.getTotalPages());
    }

    @Test
    void findAll_ShouldReturnEmptyIfOutOfBounds() {
        repository.save(sampleRoster);

        Page<Roster> page = repository.findAll(PageRequest.of(5, 10));

        assertTrue(page.getContent().isEmpty());
    }

    @Test
    void deleteById_ShouldRemoveRoster() {
        Roster saved = repository.save(sampleRoster);

        repository.deleteById(saved.getId());

        assertTrue(repository.findById(saved.getId()).isEmpty());
    }
}
