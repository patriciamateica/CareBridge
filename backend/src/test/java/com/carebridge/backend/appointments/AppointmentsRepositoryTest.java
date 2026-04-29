package com.carebridge.backend.appointments;

import com.carebridge.backend.appointments.model.Appointments;
import com.carebridge.backend.appointments.model.AppointmentsStatus;
import com.carebridge.backend.user.Role;
import com.carebridge.backend.user.UserRepository;
import com.carebridge.backend.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class AppointmentsRepositoryTest {

    @Autowired
    private AppointmentsRepository repository;

    @Autowired
    private UserRepository userRepository;

    private Appointments sampleAppointment;
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

        sampleAppointment = new Appointments(
            null,
            patientUser,
            nurseUser,
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
            repository.save(new Appointments(null, patientUser, nurseUser,
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

    @Test
    void findByPatientId_ShouldReturnOnlyMatchingAppointments() {
        User otherPatient = new User();
        otherPatient.setEmail("other-patient@test.com");
        otherPatient.setFirstName("Other");
        otherPatient.setLastName("Patient");
        otherPatient.setPassword("password");
        otherPatient.setRole(Role.PATIENT);
        otherPatient = userRepository.saveAndFlush(otherPatient);

        repository.save(sampleAppointment);
        repository.save(new Appointments(null, patientUser, nurseUser, "Follow-up",
            LocalDateTime.now().plusDays(2), AppointmentsStatus.VALIDATED));
        repository.save(new Appointments(null, otherPatient, nurseUser, "Other checkup",
            LocalDateTime.now().plusDays(3), AppointmentsStatus.REQUESTED));

        java.util.List<Appointments> results = repository.findByPatientId(patientUser.getId());

        assertEquals(2, results.size());
        assertTrue(results.stream().allMatch(a -> a.getPatient().getId().equals(patientUser.getId())));
    }

    @Test
    void findByPatientId_ShouldReturnEmptyListForUnknownPatient() {
        repository.save(sampleAppointment);

        java.util.List<Appointments> results = repository.findByPatientId(UUID.randomUUID());

        assertTrue(results.isEmpty());
    }
}
