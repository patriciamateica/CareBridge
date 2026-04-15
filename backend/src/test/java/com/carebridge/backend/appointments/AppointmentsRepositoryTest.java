package com.carebridge.backend.appointments;

import com.carebridge.backend.appointments.model.Appointments;
import com.carebridge.backend.appointments.model.AppointmentsStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class AppointmentsRepositoryTest {

    private com.carebridge.backend.appointments.AppointmentsRepository repository;
    private Appointments sampleAppointment;

    @BeforeEach
    void setUp() {
        repository = new AppointmentsRepository();
        sampleAppointment = new Appointments(
            null,
            UUID.randomUUID(),
            UUID.randomUUID(),
            "Routine checkup",
            LocalDateTime.now().plusDays(1),
            AppointmentsStatus.REQUESTED
        );
    }

    @Test
    void save_ShouldGenerateIdAndStoreAppointments() {
        Appointments saved = repository.save(sampleAppointment);

        assertNotNull(saved.getId());
        assertEquals("Routine checkup", saved.getDescription());
        assertTrue(repository.findById(saved.getId()).isPresent());
    }

    @Test
    void findById_ShouldReturnAppointmentsWhenExists() {
        Appointments saved = repository.save(sampleAppointment);

        Optional<Appointments> found = repository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    void findById_ShouldReturnEmptyWhenNotExists() {
        Optional<Appointments> found = repository.findById(UUID.randomUUID());

        assertTrue(found.isEmpty());
    }

    @Test
    void findAll_ShouldReturnCorrectPage() {
        for (int i = 0; i < 5; i++) {
            repository.save(new Appointments(null, UUID.randomUUID(), UUID.randomUUID(),
                "Desc " + i, LocalDateTime.now(), AppointmentsStatus.VALIDATED));
        }

        Page<Appointments> page = repository.findAll(PageRequest.of(0, 2));

        assertEquals(2, page.getContent().size());
        assertEquals(5, page.getTotalElements());
        assertEquals(3, page.getTotalPages());
    }

    @Test
    void findAll_ShouldReturnEmptyIfOutOfBounds() {
        repository.save(sampleAppointment);

        Page<Appointments> page = repository.findAll(PageRequest.of(5, 10));

        assertTrue(page.getContent().isEmpty());
    }

    @Test
    void deleteById_ShouldRemoveAppointments() {
        Appointments saved = repository.save(sampleAppointment);

        repository.deleteById(saved.getId());

        assertTrue(repository.findById(saved.getId()).isEmpty());
    }
}
