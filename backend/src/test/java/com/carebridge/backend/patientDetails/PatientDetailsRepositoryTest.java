package com.carebridge.backend.patientDetails;

import com.carebridge.backend.patientDetails.model.PatientDetails;
import com.carebridge.backend.user.Role;
import com.carebridge.backend.user.UserRepository;
import com.carebridge.backend.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PatientDetailsRepositoryTest {

    @Autowired
    private PatientDetailsRepository repository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User patientUser;

    @BeforeEach
    void setUp() {
        patientUser = new User();
        patientUser.setEmail("patient@test.com");
        patientUser.setFirstName("Test");
        patientUser.setLastName("Patient");
        patientUser.setPassword("password");
        patientUser.setRole(Role.PATIENT);
        patientUser = userRepository.saveAndFlush(patientUser);
    }

    private PatientDetails createPatientDetails(User user, String diagnosis,
            List<String> diagnostics, List<String> scans, String contact, UUID nurseId) {
        PatientDetails details = new PatientDetails();
        details.setUser(user);
        details.setPrimaryDiagnosis(diagnosis);
        details.setDiagnostics(diagnostics);
        details.setScans(scans);
        details.setEmergencyContact(contact);
        details.setAssignedNurseId(nurseId);
        entityManager.persist(details);
        entityManager.flush();
        return details;
    }

    @Test
    void save_ShouldGenerateIdAndStorePatientDetails() {
        PatientDetails saved = createPatientDetails(patientUser, "Hypertension",
            List.of("Blood Test", "ECG"), List.of("/scans/ecg_01.png"),
            "Wife: 0722123456", UUID.randomUUID());

        assertNotNull(saved.getId());
        assertEquals("Hypertension", saved.getPrimaryDiagnosis());
        assertTrue(repository.findById(saved.getId()).isPresent());
    }

    @Test
    void findById_ShouldReturnPatientDetailsWhenExists() {
        PatientDetails saved = createPatientDetails(patientUser, "Hypertension",
            List.of("Blood Test", "ECG"), List.of("/scans/ecg_01.png"),
            "Wife: 0722123456", UUID.randomUUID());

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
            User user = new User();
            user.setEmail("patient" + i + "@test.com");
            user.setFirstName("Patient");
            user.setLastName("Test" + i);
            user.setPassword("password");
            user.setRole(Role.PATIENT);
            user = userRepository.saveAndFlush(user);
            createPatientDetails(user, "Diagnosis " + i, List.of(), List.of(), "Contact", UUID.randomUUID());
        }

        Page<PatientDetails> page = repository.findAll(PageRequest.of(0, 2));

        assertEquals(2, page.getContent().size());
        assertEquals(5, page.getTotalElements());
        assertEquals(3, page.getTotalPages());
    }

    @Test
    void findAll_ShouldReturnEmptyIfOutOfBounds() {
        createPatientDetails(patientUser, "Hypertension",
            List.of("Blood Test", "ECG"), List.of("/scans/ecg_01.png"),
            "Wife: 0722123456", UUID.randomUUID());

        Page<PatientDetails> page = repository.findAll(PageRequest.of(5, 10));

        assertTrue(page.getContent().isEmpty());
    }

    @Test
    void deleteById_ShouldRemovePatientDetails() {
        PatientDetails saved = createPatientDetails(patientUser, "Hypertension",
            List.of("Blood Test", "ECG"), List.of("/scans/ecg_01.png"),
            "Wife: 0722123456", UUID.randomUUID());

        repository.deleteById(saved.getId());
        entityManager.flush();
        entityManager.clear();

        assertTrue(repository.findById(saved.getId()).isEmpty());
    }
}
