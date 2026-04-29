package com.carebridge.backend.prescription;

import com.carebridge.backend.prescription.model.Prescription;
import com.carebridge.backend.user.Role;
import com.carebridge.backend.user.UserRepository;
import com.carebridge.backend.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class PrescriptionRepositoryTest {

    @Autowired
    private com.carebridge.backend.prescription.PrescriptionRepository repository;

    @Autowired
    private UserRepository userRepository;

    private Prescription samplePrescription;
    private User patientUser;
    private User nurseUser;

    @BeforeEach
    void setUp() {
        patientUser = new User();
        patientUser.setEmail("patient@test.com");
        patientUser.setFirstName("Patient");
        patientUser.setLastName("Test");
        patientUser.setPassword("password");
        patientUser.setRole(Role.PATIENT);
        patientUser = userRepository.saveAndFlush(patientUser);

        nurseUser = new User();
        nurseUser.setEmail("nurse@test.com");
        nurseUser.setFirstName("Nurse");
        nurseUser.setLastName("Test");
        nurseUser.setPassword("password");
        nurseUser.setRole(Role.NURSE);
        nurseUser = userRepository.saveAndFlush(nurseUser);

        samplePrescription = new Prescription(
            null,
            "Amoxicillin",
            "500mg",
            "Every 8 hours",
            patientUser,
            nurseUser
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
                "Daily", patientUser, nurseUser));
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

    @Test
    void findByPatientId_Pageable_ShouldReturnOnlyMatchingPrescriptions() {
        User otherPatient = new User();
        otherPatient.setEmail("other-patient@test.com");
        otherPatient.setFirstName("Other");
        otherPatient.setLastName("Patient");
        otherPatient.setPassword("password");
        otherPatient.setRole(Role.PATIENT);
        otherPatient = userRepository.saveAndFlush(otherPatient);

        repository.save(samplePrescription);
        repository.save(new Prescription(null, "Ibuprofen", "200mg", "Twice daily", patientUser, nurseUser));
        repository.save(new Prescription(null, "Metformin", "500mg", "Daily", otherPatient, nurseUser));

        Page<Prescription> page = repository.findByPatientId(patientUser.getId(), PageRequest.of(0, 10));

        assertEquals(2, page.getTotalElements());
        assertTrue(page.getContent().stream().allMatch(p -> p.getPatient().getId().equals(patientUser.getId())));
    }

    @Test
    void findByPatientId_Pageable_ShouldReturnEmptyForUnknownPatient() {
        repository.save(samplePrescription);

        Page<Prescription> page = repository.findByPatientId(UUID.randomUUID(), PageRequest.of(0, 10));

        assertTrue(page.getContent().isEmpty());
        assertEquals(0, page.getTotalElements());
    }

    @Test
    void findByPatientIdOrderByIdAsc_ShouldReturnFilteredAndSortedByIdAscending() {
        User otherPatient = new User();
        otherPatient.setEmail("other-patient3@test.com");
        otherPatient.setFirstName("Other3");
        otherPatient.setLastName("Patient");
        otherPatient.setPassword("password");
        otherPatient.setRole(Role.PATIENT);
        otherPatient = userRepository.saveAndFlush(otherPatient);

        repository.save(new Prescription(null, "Drug A", "10mg", "Daily", patientUser, nurseUser));
        repository.save(new Prescription(null, "Drug B", "20mg", "Daily", patientUser, nurseUser));
        repository.save(new Prescription(null, "Drug C", "30mg", "Daily", otherPatient, nurseUser));

        java.util.List<Prescription> results = repository.findByPatientIdOrderByIdAsc(patientUser.getId());

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(p -> p.getPatient().getId().equals(patientUser.getId())));
    }

    @Test
    void findByPatientIdOrderByIdAsc_ShouldReturnEmptyForUnknownPatient() {
        repository.save(samplePrescription);

        java.util.List<Prescription> results = repository.findByPatientIdOrderByIdAsc(UUID.randomUUID());

        assertTrue(results.isEmpty());
    }

    @Test
    void countByPatientId_ShouldCountOnlyMatchingPatient() {
        User otherPatient = new User();
        otherPatient.setEmail("other-patient2@test.com");
        otherPatient.setFirstName("Other2");
        otherPatient.setLastName("Patient");
        otherPatient.setPassword("password");
        otherPatient.setRole(Role.PATIENT);
        otherPatient = userRepository.saveAndFlush(otherPatient);

        repository.save(samplePrescription);
        repository.save(new Prescription(null, "Ibuprofen", "200mg", "Twice daily", patientUser, nurseUser));
        repository.save(new Prescription(null, "Metformin", "500mg", "Daily", otherPatient, nurseUser));

        long count = repository.countByPatientId(patientUser.getId());

        assertEquals(2, count);
    }

    @Test
    void countByPatientId_ShouldReturnZeroForUnknownPatient() {
        repository.save(samplePrescription);

        long count = repository.countByPatientId(UUID.randomUUID());

        assertEquals(0, count);
    }
}
