package com.carebridge.backend.seeder;

import com.carebridge.backend.appointments.AppointmentsService;
import com.carebridge.backend.carenotes.CareNotesService;
import com.carebridge.backend.clinicalLog.ClinicalLogService;
import com.carebridge.backend.healthStatus.HealthStatusService;
import com.carebridge.backend.nursedetails.NurseDetailsService;
import com.carebridge.backend.patientDetails.PatientDetailsService;
import com.carebridge.backend.prescription.PrescriptionService;
import com.carebridge.backend.roster.RosterService;
import com.carebridge.backend.task.TaskService;
import com.carebridge.backend.user.Role;
import com.carebridge.backend.user.UserRepository;
import com.carebridge.backend.user.UserService;
import com.carebridge.backend.user.model.User;
import com.carebridge.backend.vitals.VitalsService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Collections;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SeederServiceTest {

    @Mock
    private AppointmentsService appointmentsService;
    @Mock
    private CareNotesService careNotesService;
    @Mock
    private ClinicalLogService clinicalLogService;
    @Mock
    private HealthStatusService healthStatusService;
    @Mock
    private TaskService taskService;
    @Mock
    private VitalsService vitalsService;
    @Mock
    private UserRepository userRepository;
    @Mock
    private UserService userService;
    @Mock
    private PasswordEncoder passwordEncoder;
    @Mock
    private PrescriptionService prescriptionService;
    @Mock
    private PatientDetailsService patientDetailsService;
    @Mock
    private NurseDetailsService nurseDetailsService;
    @Mock
    private RosterService rosterService;

    @InjectMocks
    private SeederService seederService;

    @AfterEach
    void tearDown() {
        seederService.stopLoop();
    }

    @Test
    void seedDatabase_ShouldCreateInitialData() {
        when(userRepository.findAll()).thenReturn(Collections.emptyList())
            .thenAnswer(invocation -> {
                // After seeding users, return realistic users for downstream seed methods
                java.util.List<User> users = new java.util.ArrayList<>();
                for (int i = 1; i <= 10; i++) {
                    User p = new User();
                    p.setId(UUID.randomUUID());
                    p.setRole(Role.PATIENT);
                    p.setEmail("patient" + i + "@carebridge.local");
                    users.add(p);
                }
                User nurse = new User();
                nurse.setId(UUID.randomUUID());
                nurse.setRole(Role.NURSE);
                nurse.setEmail("nurse@carebridge.local");
                users.add(nurse);
                return users;
            });

        User nurseUser = new User();
        nurseUser.setId(UUID.randomUUID());
        nurseUser.setRole(Role.NURSE);
        nurseUser.setEmail("nurse@carebridge.local");
        when(userRepository.findByEmailIgnoreCase(anyString())).thenReturn(Optional.of(nurseUser));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> {
            User u = invocation.getArgument(0);
            if (u.getId() == null) {
                u.setId(UUID.randomUUID());
            }
            return u;
        });

        seederService.seedDatabase();

        verify(userRepository, times(13)).save(any(User.class));
        verify(nurseDetailsService, times(1)).create(any());
        verify(patientDetailsService, times(10)).create(any());
        verify(rosterService, times(10)).create(any());
        verify(vitalsService, times(15)).create(any());
        verify(taskService, times(15)).create(any());
        verify(appointmentsService, times(10)).create(any());
        verify(healthStatusService, times(15)).create(any());
        verify(clinicalLogService, times(10)).create(any());
        verify(careNotesService, times(15)).create(any());
        verify(prescriptionService, times(15)).create(any());
    }

    @Test
    void clearDatabase_ShouldDeleteAllData() {
        seederService.clearDatabase();

        verify(appointmentsService, times(1)).deleteAll();
        verify(careNotesService, times(1)).deleteAll();
        verify(clinicalLogService, times(1)).deleteAll();
        verify(healthStatusService, times(1)).deleteAll();
        verify(taskService, times(1)).deleteAll();
        verify(vitalsService, times(1)).deleteAll();
        verify(prescriptionService, times(1)).deleteAll();
        verify(rosterService, times(1)).deleteAll();
        verify(patientDetailsService, times(1)).deleteAll();
        verify(nurseDetailsService, times(1)).deleteAll();
        verify(userRepository, times(1)).deleteAll();
    }

    @Test
    void startLoop_ShouldGenerateLiveUpdatesForExistingRoster() {
        User patient = buildUser(UUID.randomUUID(), Role.PATIENT);
        User nurse = buildUser(UUID.randomUUID(), Role.NURSE);
        when(userRepository.findAll()).thenReturn(List.of(patient, nurse));
        when(userRepository.findByEmailIgnoreCase("nurse@carebridge.local")).thenReturn(Optional.of(nurse));

        seederService.startLoop();

        verify(vitalsService, timeout(6000).atLeastOnce()).create(any());
        verify(taskService, timeout(6000).atLeastOnce()).create(any());
        verify(careNotesService, timeout(6000).atLeastOnce()).create(any());
    }

    @Test
    void stopLoop_ShouldBeSafeWhenNeverStarted() {
        seederService.stopLoop();
        verify(vitalsService, never()).create(any());
    }

    private User buildUser(UUID id, Role role) {
        User user = new User();
        user.setId(id);
        user.setRole(role);
        user.setEmail(role.name().toLowerCase() + "@carebridge.local");
        return user;
    }
}
