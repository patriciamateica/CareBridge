package com.carebridge.backend.healthStatus;

import com.carebridge.backend.healthStatus.model.HealthStatus;
import com.carebridge.backend.healthStatus.model.Mood;
import com.carebridge.backend.user.Role;
import com.carebridge.backend.user.UserRepository;
import com.carebridge.backend.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class HealthStatusRepositoryTest {

    @Autowired
    private HealthStatusRepository repository;

    @Autowired
    private UserRepository userRepository;

    private HealthStatus sampleStatus;
    private User patientUser;

    @BeforeEach
    void setUp() {
        patientUser = new User();
        patientUser.setEmail("patient@test.com");
        patientUser.setFirstName("Patient");
        patientUser.setLastName("Test");
        patientUser.setPassword("password");
        patientUser.setRole(Role.PATIENT);
        patientUser = userRepository.saveAndFlush(patientUser);

        sampleStatus = new HealthStatus(
            null,
            5,
            Mood.Calm,
            List.of("Headache", "Nausea"),
            "Patient feels a bit dizzy",
            LocalDate.now(),
            patientUser
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
            repository.save(new HealthStatus(null, i, Mood.Calm, List.of("Cough"), "Notes", LocalDate.now(), patientUser));
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

    @Test
    void findByPatientId_ShouldReturnOnlyMatchingStatuses() {
        User otherPatient = new User();
        otherPatient.setEmail("other-patient@test.com");
        otherPatient.setFirstName("Other");
        otherPatient.setLastName("Patient");
        otherPatient.setPassword("password");
        otherPatient.setRole(Role.PATIENT);
        otherPatient = userRepository.saveAndFlush(otherPatient);

        repository.save(sampleStatus);
        repository.save(new HealthStatus(null, 3, Mood.Calm, List.of("Cough"), "Second entry", LocalDate.now(), patientUser));
        repository.save(new HealthStatus(null, 7, Mood.Calm, List.of("Fever"), "Other", LocalDate.now(), otherPatient));

        List<HealthStatus> results = repository.findByPatientId(patientUser.getId());

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(h -> h.getPatient().getId().equals(patientUser.getId())));
    }

    @Test
    void findByPatientId_ShouldReturnEmptyListForUnknownPatient() {
        repository.save(sampleStatus);

        List<HealthStatus> results = repository.findByPatientId(UUID.randomUUID());

        assertTrue(results.isEmpty());
    }
}
