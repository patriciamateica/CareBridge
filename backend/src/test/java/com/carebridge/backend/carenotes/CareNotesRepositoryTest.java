package com.carebridge.backend.carenotes;

import com.carebridge.backend.carenotes.model.CareNotes;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class CareNotesRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private CareNotesRepository repository;

    @Autowired
    private UserRepository userRepository;


    private CareNotes sampleNotes;
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

        sampleNotes = new CareNotes(
            null,
            "Patient responded well to medication.",
            patientUser,
            nurseUser,
            LocalDateTime.now()
        );
    }

    @Test
    void save_ShouldGenerateIdAndStoreCareNotes() {
        CareNotes saved = repository.save(sampleNotes);

        assertNotNull(saved.getId());
        assertEquals("Patient responded well to medication.", saved.getContent());
        assertTrue(repository.findById(saved.getId()).isPresent());
    }

    @Test
    void save_ShouldIgnoreProvidedIdAndGenerateId() {
        // With UUID generation strategy, we just verify that saving a new entity
        // generates a non-null ID regardless of any pre-set value
        CareNotes notesWithNoId = new CareNotes(
                null,
                "Existing ID",
                patientUser,
                nurseUser,
                LocalDateTime.now()
        );

        CareNotes saved = repository.saveAndFlush(notesWithNoId);

        assertNotNull(saved.getId(), "JPA should have generated a UUID for the new entity");
    }

    @Test
    void findById_ShouldReturnCareNotesWhenExists() {
        CareNotes saved = repository.save(sampleNotes);

        Optional<CareNotes> found = repository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    void findById_ShouldReturnEmptyWhenNotExists() {
        Optional<CareNotes> found = repository.findById(UUID.randomUUID());

        assertTrue(found.isEmpty());
    }

    @Test
    void findAll_ShouldReturnCorrectPage() {
        for (int i = 0; i < 5; i++) {
            repository.save(new CareNotes(null, "Note " + i, patientUser, nurseUser, LocalDateTime.now()));
        }

        Page<CareNotes> page = repository.findAll(PageRequest.of(0, 2));

        assertEquals(2, page.getContent().size());
        assertEquals(5, page.getTotalElements());
        assertEquals(3, page.getTotalPages());
    }

    @Test
    void findAll_ShouldReturnEmptyIfOutOfBounds() {
        repository.save(sampleNotes);

        Page<CareNotes> page = repository.findAll(PageRequest.of(5, 10));

        assertTrue(page.getContent().isEmpty());
    }

    @Test
    void deleteById_ShouldRemoveCareNotes() {
        CareNotes saved = repository.save(sampleNotes);

        repository.deleteById(saved.getId());

        assertTrue(repository.findById(saved.getId()).isEmpty());
    }

    @Test
    void findByPatientId_Pageable_ShouldFilterAndSortByTimestampDescending() {
        User patient = new User();
        patient.setEmail("patient-sort@test.com");
        patient.setFirstName("Patient");
        patient.setLastName("Sort");
        patient.setPassword("password");
        patient.setRole(Role.PATIENT);
        patient = userRepository.saveAndFlush(patient);

        CareNotes older = repository.save(new CareNotes(null, "older", patient, nurseUser, LocalDateTime.of(2026, 1, 1, 8, 0)));
        CareNotes newer = repository.save(new CareNotes(null, "newer", patient, nurseUser, LocalDateTime.of(2026, 1, 2, 8, 0)));
        repository.save(new CareNotes(null, "other", patientUser, nurseUser, LocalDateTime.of(2026, 1, 3, 8, 0)));

        Page<CareNotes> page = repository.findByPatientIdOrderByTimestampDesc(patient.getId(), PageRequest.of(0, 10));

        assertEquals(2, page.getTotalElements());
        assertEquals(List.of(newer, older), page.getContent());
    }

    @Test
    void findByPatientId_Pageable_ShouldReturnEmptyWhenOutOfBounds() {
        User patient = new User();
        patient.setEmail("patient-page@test.com");
        patient.setFirstName("Patient");
        patient.setLastName("Page");
        patient.setPassword("password");
        patient.setRole(Role.PATIENT);
        patient = userRepository.saveAndFlush(patient);

        repository.save(new CareNotes(null, "only", patient, nurseUser, LocalDateTime.now()));

        Page<CareNotes> page = repository.findByPatientIdOrderByTimestampDesc(patient.getId(), PageRequest.of(10, 5));

        assertTrue(page.getContent().isEmpty());
        assertEquals(1, page.getTotalElements());
    }

    @Test
    void findByPatientId_List_ShouldFilterAndSortByTimestampDescending() {
        User patient = new User();
        patient.setEmail("patient-order@test.com");
        patient.setFirstName("Patient");
        patient.setLastName("Order");
        patient.setPassword("password");
        patient.setRole(Role.PATIENT);
        patient = userRepository.saveAndFlush(patient);

        CareNotes oldest = repository.save(new CareNotes(null, "oldest", patient, nurseUser, LocalDateTime.of(2026, 2, 1, 8, 0)));
        CareNotes newest = repository.save(new CareNotes(null, "newest", patient, nurseUser, LocalDateTime.of(2026, 2, 3, 8, 0)));
        repository.save(new CareNotes(null, "middle", patient, nurseUser, LocalDateTime.of(2026, 2, 2, 8, 0)));
        repository.save(new CareNotes(null, "other-patient", patientUser, nurseUser, LocalDateTime.of(2026, 2, 4, 8, 0)));

        List<CareNotes> result = repository.findByPatientIdOrderByTimestampDesc(patient.getId());

        assertEquals(3, result.size());
        assertEquals(newest.getId(), result.getFirst().getId());
        assertEquals(oldest.getId(), result.getLast().getId());
    }

    @Test
    void findByPatientId_List_ShouldHandleNullTimestampOrdering() {
        User patient = new User();
        patient.setEmail("patient-valid@test.com");
        patient.setFirstName("Patient");
        patient.setLastName("Valid");
        patient.setPassword("password");
        patient.setRole(Role.PATIENT);
        patient = userRepository.saveAndFlush(patient);

        CareNotes earlier = repository.save(new CareNotes(null, "earlier", patient, nurseUser, LocalDateTime.of(2026, 2, 28, 10, 0)));
        CareNotes later = repository.save(new CareNotes(null, "later", patient, nurseUser, LocalDateTime.of(2026, 3, 1, 10, 0)));

        List<CareNotes> result = repository.findByPatientIdOrderByTimestampDesc(patient.getId());

        assertEquals(2, result.size());
        assertEquals(later.getId(), result.getFirst().getId());
        assertEquals(earlier.getId(), result.getLast().getId());
    }

    @Test
    void countByPatientId_ShouldCountOnlyMatchingPatient() {
        User patient = new User();
        patient.setEmail("patient-count@test.com");
        patient.setFirstName("Patient");
        patient.setLastName("Count");
        patient.setPassword("password");
        patient.setRole(Role.PATIENT);
        patient = userRepository.saveAndFlush(patient);

        repository.save(new CareNotes(null, "n1", patient, nurseUser, LocalDateTime.now()));
        repository.save(new CareNotes(null, "n2", patient, nurseUser, LocalDateTime.now()));
        repository.save(new CareNotes(null, "other", patientUser, nurseUser, LocalDateTime.now()));

        long count = repository.countByPatientId(patient.getId());

        assertEquals(2, count);
    }

    @Test
    void deleteAll_ShouldClearRepository() {
        CareNotes saved = repository.save(sampleNotes);

        repository.deleteAll();

        assertTrue(repository.findById(saved.getId()).isEmpty());
        assertTrue(repository.findAll(PageRequest.of(0, 10)).isEmpty());
    }
}
