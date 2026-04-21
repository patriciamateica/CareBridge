package com.carebridge.backend.seeder;

import com.carebridge.backend.appointments.AppointmentsService;
import com.carebridge.backend.carenotes.CareNotesService;
import com.carebridge.backend.clinicalLog.ClinicalLogService;
import com.carebridge.backend.healthStatus.HealthStatusService;
import com.carebridge.backend.nursedetails.NurseDetailsService;
import com.carebridge.backend.nursedetails.model.NurseDetails;
import com.carebridge.backend.patientDetails.PatientDetailsService;
import com.carebridge.backend.prescription.PrescriptionService;
import com.carebridge.backend.roster.RosterService;
import com.carebridge.backend.roster.model.Roster;
import com.carebridge.backend.roster.model.RosterStatus;
import com.carebridge.backend.task.TaskService;
import com.carebridge.backend.user.Role;
import com.carebridge.backend.user.UserRepository;
import com.carebridge.backend.user.model.User;
import com.carebridge.backend.vitals.VitalsService;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.atLeastOnce;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.timeout;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SeederServiceTest {

    @Mock private AppointmentsService appointmentsService;
    @Mock private CareNotesService careNotesService;
    @Mock private ClinicalLogService clinicalLogService;
    @Mock private HealthStatusService healthStatusService;
    @Mock private TaskService taskService;
    @Mock private VitalsService vitalsService;
    @Mock private UserRepository userRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private PrescriptionService prescriptionService;
    @Mock private PatientDetailsService patientDetailsService;
    @Mock private NurseDetailsService nurseDetailsService;
    @Mock private RosterService rosterService;

    @InjectMocks
    private SeederService seederService;

    @AfterEach
    void tearDown() {
        seederService.stopLoop();
    }

    @Test
    void seedRepository_ShouldCreate15PatientBundlesAnd30Prescriptions() {
        User nurse1 = buildUser(UUID.randomUUID(), Role.NURSE);
        User nurse2 = buildUser(UUID.randomUUID(), Role.NURSE);

        when(userRepository.findAll()).thenReturn(List.of(), List.of(nurse1, nurse2));
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(nurseDetailsService.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(List.of()));

        seederService.seedRepository();

        verify(appointmentsService, times(15)).create(any());
        verify(vitalsService, times(15)).create(any());
        verify(taskService, times(15)).create(any());
        verify(healthStatusService, times(15)).create(any());
        verify(clinicalLogService, times(15)).create(any());
        verify(careNotesService, times(15)).create(any());
        verify(prescriptionService, times(30)).create(any());
        verify(rosterService, times(15)).create(any());
        verify(patientDetailsService, times(15)).create(any());
        verify(nurseDetailsService, atLeastOnce()).create(any(NurseDetails.class));
    }

    @Test
    void unseedRepository_ShouldDeleteAllAndRecreateAdminIfMissing() {
        when(userRepository.findByEmail(anyString())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("encoded-admin-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        seederService.unseedRepository();

        verify(appointmentsService, times(1)).deleteAll();
        verify(careNotesService, times(1)).deleteAll();
        verify(clinicalLogService, times(1)).deleteAll();
        verify(healthStatusService, times(1)).deleteAll();
        verify(taskService, times(1)).deleteAll();
        verify(vitalsService, times(1)).deleteAll();
        verify(prescriptionService, times(1)).deleteAll();
        verify(patientDetailsService, times(1)).deleteAll();
        verify(nurseDetailsService, times(1)).deleteAll();
        verify(rosterService, times(1)).deleteAll();
        verify(userRepository, times(1)).deleteAll();
        verify(userRepository, times(1)).save(any(User.class));
    }

    @Test
    void initAdminUser_ShouldCreateAdminWhenMissing() {
        when(userRepository.findByEmail("admin@carebridge.local")).thenReturn(Optional.empty());
        when(passwordEncoder.encode("Admin123!")).thenReturn("encoded-admin-password");
        when(userRepository.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

        seederService.initAdminUser();

        ArgumentCaptor<User> captor = ArgumentCaptor.forClass(User.class);
        verify(userRepository, times(1)).save(captor.capture());
        User savedAdmin = captor.getValue();

        assertEquals("admin@carebridge.local", savedAdmin.getEmail());
        assertEquals(Role.ADMIN, savedAdmin.getRole());
        assertEquals("encoded-admin-password", savedAdmin.getPassword());
    }

    @Test
    void initAdminUser_ShouldNotCreateAdminWhenAlreadyExists() {
        User existingAdmin = new User();
        existingAdmin.setId(UUID.randomUUID());
        existingAdmin.setEmail("admin@carebridge.local");
        when(userRepository.findByEmail("admin@carebridge.local")).thenReturn(Optional.of(existingAdmin));

        seederService.initAdminUser();

        verify(userRepository, never()).save(any(User.class));
        verify(passwordEncoder, never()).encode(anyString());
    }

    @Test
    void startLoop_ShouldGenerateLiveUpdatesForExistingRoster() {
        UUID patientId = UUID.randomUUID();
        UUID nurseId = UUID.randomUUID();
        Roster roster = new Roster(UUID.randomUUID(), patientId, nurseId, RosterStatus.ACTIVE);
        when(rosterService.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(List.of(roster)));

        seederService.startLoop();

        verify(vitalsService, timeout(1500).atLeastOnce()).create(any());
        verify(taskService, timeout(1500).atLeastOnce()).create(any());
        verify(appointmentsService, timeout(1500).atLeastOnce()).create(any());
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

