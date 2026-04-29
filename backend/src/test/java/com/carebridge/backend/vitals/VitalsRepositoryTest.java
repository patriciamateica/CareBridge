package com.carebridge.backend.vitals;

import com.carebridge.backend.user.Role;
import com.carebridge.backend.user.UserRepository;
import com.carebridge.backend.user.model.User;
import com.carebridge.backend.vitals.model.Vitals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class VitalsRepositoryTest {

    @Autowired
    private com.carebridge.backend.vitals.VitalsRepository repository;

    @Autowired
    private UserRepository userRepository;

    private Vitals sampleVitals;
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

        sampleVitals = new Vitals(null, LocalDate.now(), 80, 120, 16, 98, patientUser);
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
            repository.save(new Vitals(null, LocalDate.now(), 80 + i, 120, 16, 98, patientUser));
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

    @Test
    void findByPatientId_ShouldReturnOnlyMatchingVitals() {
        User otherPatient = new User();
        otherPatient.setEmail("other-patient@test.com");
        otherPatient.setFirstName("Other");
        otherPatient.setLastName("Patient");
        otherPatient.setPassword("password");
        otherPatient.setRole(Role.PATIENT);
        otherPatient = userRepository.saveAndFlush(otherPatient);

        repository.save(sampleVitals);
        repository.save(new Vitals(null, LocalDate.now(), 72, 110, 14, 97, patientUser));
        repository.save(new Vitals(null, LocalDate.now(), 90, 130, 20, 95, otherPatient));

        java.util.List<Vitals> results = repository.findByPatientId(patientUser.getId());

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(v -> v.getPatient().getId().equals(patientUser.getId())));
    }

    @Test
    void findByPatientId_ShouldReturnEmptyListForUnknownPatient() {
        repository.save(sampleVitals);

        java.util.List<Vitals> results = repository.findByPatientId(UUID.randomUUID());

        assertTrue(results.isEmpty());
    }
}
