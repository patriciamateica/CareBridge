package com.carebridge.backend.roster;

import com.carebridge.backend.roster.model.Roster;
import com.carebridge.backend.roster.model.RosterStatus;
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
class RosterRepositoryTest {

    @Autowired
    private RosterRepository repository;

    @Autowired
    private UserRepository userRepository;

    private Roster sampleRoster;
    private User patientUser;
    private User nurseUser;

    @BeforeEach
    void setUp() {
        patientUser = new User();
        patientUser.setEmail("patient@test.com");
        patientUser.setFirstName("Patient");
        patientUser.setLastName("Test");
        patientUser.setPassword("password");
        patientUser.setRole(com.carebridge.backend.user.Role.PATIENT);
        patientUser = userRepository.saveAndFlush(patientUser);

        nurseUser = new User();
        nurseUser.setEmail("nurse@test.com");
        nurseUser.setFirstName("Nurse");
        nurseUser.setLastName("Test");
        nurseUser.setPassword("password");
        nurseUser.setRole(com.carebridge.backend.user.Role.NURSE);
        nurseUser = userRepository.saveAndFlush(nurseUser);

        sampleRoster = new Roster(
            null,
            patientUser,
            nurseUser,
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
            User patient = new User();
            patient.setEmail("patient" + i + "@test.com");
            patient.setFirstName("Patient");
            patient.setLastName("Test" + i);
            patient.setPassword("password");
            patient.setRole(com.carebridge.backend.user.Role.PATIENT);
            patient = userRepository.saveAndFlush(patient);

            repository.save(new Roster(null, patient, nurseUser, RosterStatus.ACTIVE));
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

    @Test
    void findByNurseId_ShouldReturnOnlyMatchingRosters() {
        User otherNurse = new User();
        otherNurse.setEmail("other-nurse@test.com");
        otherNurse.setFirstName("Other");
        otherNurse.setLastName("Nurse");
        otherNurse.setPassword("password");
        otherNurse.setRole(com.carebridge.backend.user.Role.NURSE);
        otherNurse = userRepository.saveAndFlush(otherNurse);

        User patient2 = new User();
        patient2.setEmail("patient2@test.com");
        patient2.setFirstName("Patient2");
        patient2.setLastName("Test");
        patient2.setPassword("password");
        patient2.setRole(com.carebridge.backend.user.Role.PATIENT);
        patient2 = userRepository.saveAndFlush(patient2);

        repository.save(sampleRoster);
        repository.save(new Roster(null, patient2, nurseUser, RosterStatus.ACTIVE));
        repository.save(new Roster(null, patientUser, otherNurse, RosterStatus.PENDING));

        java.util.List<Roster> results = repository.findByNurseId(nurseUser.getId());

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(r -> r.getNurse().getId().equals(nurseUser.getId())));
    }

    @Test
    void findByNurseId_ShouldReturnEmptyListForUnknownNurse() {
        repository.save(sampleRoster);

        java.util.List<Roster> results = repository.findByNurseId(java.util.UUID.randomUUID());

        assertTrue(results.isEmpty());
    }
}
