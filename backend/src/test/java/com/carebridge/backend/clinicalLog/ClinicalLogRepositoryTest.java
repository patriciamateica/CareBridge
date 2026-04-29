package com.carebridge.backend.clinicalLog;

import com.carebridge.backend.clinicalLog.model.ClinicalLog;
import com.carebridge.backend.clinicalLog.model.ClinicalLogStatus;
import com.carebridge.backend.clinicalLog.model.DocumentType;
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
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class ClinicalLogRepositoryTest {

    @Autowired
    private ClinicalLogRepository repository;

    @Autowired
    private UserRepository userRepository;

    private ClinicalLog sampleLog;
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

        sampleLog = new ClinicalLog(
            null,
            "Annual Blood Work",
            DocumentType.BLOOD_WORK,
            LocalDate.now(),
            "https://storage.carebridge.com/blood_work_123.pdf",
            patientUser,
            nurseUser,
            LocalDateTime.now(),
            ClinicalLogStatus.ACTIVE
        );
    }

    @Test
    void save_ShouldGenerateIdAndStoreLog() {
        ClinicalLog saved = repository.save(sampleLog);

        assertNotNull(saved.getId());
        assertEquals("Annual Blood Work", saved.getDocumentTitle());
        assertTrue(repository.findById(saved.getId()).isPresent());
    }

    @Test
    void findById_ShouldReturnLogWhenExists() {
        ClinicalLog saved = repository.save(sampleLog);

        Optional<ClinicalLog> found = repository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    void findById_ShouldReturnEmptyWhenNotExists() {
        Optional<ClinicalLog> found = repository.findById(UUID.randomUUID());

        assertTrue(found.isEmpty());
    }

    @Test
    void findAll_ShouldReturnCorrectPage() {
        for (int i = 0; i < 5; i++) {
            repository.save(new ClinicalLog(null, "Log " + i, DocumentType.MRI, LocalDate.now(), "url", patientUser, nurseUser, LocalDateTime.now(), ClinicalLogStatus.ACTIVE));
        }

        Page<ClinicalLog> page = repository.findAll(PageRequest.of(0, 2));

        assertEquals(2, page.getContent().size());
        assertEquals(5, page.getTotalElements());
        assertEquals(3, page.getTotalPages());
    }

    @Test
    void deleteById_ShouldRemoveLogFromMap() {
        ClinicalLog saved = repository.save(sampleLog);

        repository.deleteById(saved.getId());

        assertTrue(repository.findById(saved.getId()).isEmpty());
    }

    @Test
    void findByPatientId_ShouldReturnOnlyMatchingLogs() {
        User otherPatient = new User();
        otherPatient.setEmail("other-patient@test.com");
        otherPatient.setFirstName("Other");
        otherPatient.setLastName("Patient");
        otherPatient.setPassword("password");
        otherPatient.setRole(Role.PATIENT);
        otherPatient = userRepository.saveAndFlush(otherPatient);

        repository.save(sampleLog);
        repository.save(new ClinicalLog(null, "MRI Scan", DocumentType.MRI, LocalDate.now(),
            "url2", patientUser, nurseUser, LocalDateTime.now(), ClinicalLogStatus.ACTIVE));
        repository.save(new ClinicalLog(null, "Other Log", DocumentType.BLOOD_WORK, LocalDate.now(),
            "url3", otherPatient, nurseUser, LocalDateTime.now(), ClinicalLogStatus.ACTIVE));

        java.util.List<ClinicalLog> results = repository.findByPatientId(patientUser.getId());

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(l -> l.getPatient().getId().equals(patientUser.getId())));
    }

    @Test
    void findByPatientId_ShouldReturnEmptyListForUnknownPatient() {
        repository.save(sampleLog);

        java.util.List<ClinicalLog> results = repository.findByPatientId(UUID.randomUUID());

        assertTrue(results.isEmpty());
    }
}
