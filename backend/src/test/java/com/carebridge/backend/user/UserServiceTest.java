package com.carebridge.backend.user;

import com.carebridge.backend.security.RegisterRequest;
import com.carebridge.backend.user.model.User;
import com.carebridge.backend.user.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {

    @Mock
    private UserRepository userRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private com.carebridge.backend.patientDetails.PatientDetailsService patientDetailsService;

    @Mock
    private com.carebridge.backend.vitals.VitalsRepository vitalsRepository;

    @Mock
    private com.carebridge.backend.task.TaskRepository taskRepository;

    @Mock
    private com.carebridge.backend.appointments.AppointmentsRepository appointmentsRepository;

    @Mock
    private com.carebridge.backend.roster.RosterRepository rosterRepository;

    @Mock
    private com.carebridge.backend.carenotes.CareNotesRepository careNotesRepository;

    @Mock
    private com.carebridge.backend.clinicalLog.ClinicalLogRepository clinicalLogRepository;

    @Mock
    private com.carebridge.backend.healthStatus.HealthStatusRepository healthStatusRepository;

    @Mock
    private com.carebridge.backend.prescription.PrescriptionRepository prescriptionRepository;

    @Mock
    private RoleRepository roleRepository;

    @InjectMocks
    private UserService userService;

    private RegisterRequest validRequest;
    private User sampleUser;
    private Role patientRole;

    @BeforeEach
    void setUp() {
        validRequest = new RegisterRequest(
            "Jane",
            "Doe",
            "jane@example.com",
            "password123",
            "0722111222"
        );

        patientRole = new Role("PATIENT");

        sampleUser = new User();
        sampleUser.setEmail("jane@example.com");
        sampleUser.addRole(patientRole);
    }

    @Test
    void register_ShouldSaveAndReturnUser() {
        when(userRepository.findByEmailIgnoreCase(validRequest.email())).thenReturn(Optional.empty());
        when(passwordEncoder.encode(anyString())).thenReturn("hashedPass");
        when(roleRepository.findByName("PATIENT")).thenReturn(Optional.of(patientRole));
        when(userRepository.save(any(User.class))).thenReturn(sampleUser);

        User result = userService.register(validRequest);

        assertNotNull(result);
        assertEquals("jane@example.com", result.getEmail());
        assertTrue(result.hasRole("PATIENT"));
        verify(userRepository, times(1)).save(any(User.class));
        verify(roleRepository, times(1)).findByName("PATIENT");
    }

    @Test
    void register_ShouldThrowExceptionIfEmailExists() {
        when(userRepository.findByEmailIgnoreCase(validRequest.email())).thenReturn(Optional.of(sampleUser));

        IllegalArgumentException exception = assertThrows(IllegalArgumentException.class,
            () -> userService.register(validRequest));

        assertEquals("Email already exists", exception.getMessage());
        verify(userRepository, never()).save(any(User.class));
    }
}
