package com.carebridge.backend.appointments;

import com.carebridge.backend.appointments.model.Appointments;
import com.carebridge.backend.appointments.model.AppointmentsStatus;
import com.carebridge.backend.user.Role;
import com.carebridge.backend.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AppointmentsServiceTest {

    @Mock
    private com.carebridge.backend.appointments.AppointmentsRepository appointmentsRepository;

    @InjectMocks
    private AppointmentsService appointmentsService;

    private Appointments sampleAppointment;
    private UUID appointmentId;
    private User patientUser;
    private User nurseUser;

    @BeforeEach
    void setUp() {
        appointmentId = UUID.randomUUID();

        patientUser = new User();
        patientUser.setId(UUID.randomUUID());
        patientUser.setRole(Role.PATIENT);
        patientUser.setEmail("patient@test.com");

        nurseUser = new User();
        nurseUser.setId(UUID.randomUUID());
        nurseUser.setRole(Role.NURSE);
        nurseUser.setEmail("nurse@test.com");

        sampleAppointment = new Appointments(
            appointmentId,
            patientUser,
            nurseUser,
            "Blood test",
            LocalDateTime.now().plusDays(2),
            AppointmentsStatus.REQUESTED
        );
    }

    @Test
    void findAll_ShouldReturnPageOfAppointments() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Appointments> mockPage = new PageImpl<>(List.of(sampleAppointment));
        when(appointmentsRepository.findAll(pageRequest)).thenReturn(mockPage);

        Page<Appointments> result = appointmentsService.findAll(pageRequest);

        assertEquals(1, result.getTotalElements());
        verify(appointmentsRepository, times(1)).findAll(pageRequest);
    }

    @Test
    void getById_ShouldReturnAppointmentsWhenExists() {
        when(appointmentsRepository.findById(appointmentId)).thenReturn(Optional.of(sampleAppointment));

        Appointments result = appointmentsService.getById(appointmentId);

        assertNotNull(result);
        assertEquals(appointmentId, result.getId());
    }

    @Test
    void getById_ShouldThrowExceptionWhenNotExists() {
        when(appointmentsRepository.findById(appointmentId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> appointmentsService.getById(appointmentId));
    }

    @Test
    void create_ShouldSaveAndReturnAppointments() {
        when(appointmentsRepository.save(sampleAppointment)).thenReturn(sampleAppointment);

        Appointments result = appointmentsService.create(sampleAppointment);

        assertNotNull(result);
        verify(appointmentsRepository, times(1)).save(sampleAppointment);
    }

    @Test
    void update_ShouldUpdateAndReturnAppointmentsWhenExists() {
        User otherPatient = new User();
        otherPatient.setId(UUID.randomUUID());
        otherPatient.setRole(Role.PATIENT);

        User otherNurse = new User();
        otherNurse.setId(UUID.randomUUID());
        otherNurse.setRole(Role.NURSE);

        Appointments updatedData = new Appointments(null, otherPatient, otherNurse,
            "Updated desc", LocalDateTime.now(), AppointmentsStatus.VALIDATED);
        when(appointmentsRepository.findById(appointmentId)).thenReturn(Optional.of(sampleAppointment));
        when(appointmentsRepository.save(any(Appointments.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Appointments result = appointmentsService.update(appointmentId, updatedData);

        assertEquals("Updated desc", result.getDescription());
        assertEquals(AppointmentsStatus.VALIDATED, result.getStatus());
        assertEquals(appointmentId, result.getId());
    }

    @Test
    void delete_ShouldRemoveAppointmentsWhenExists() {
        when(appointmentsRepository.findById(appointmentId)).thenReturn(Optional.of(sampleAppointment));

        boolean result = appointmentsService.delete(appointmentId);

        assertTrue(result);
        verify(appointmentsRepository, times(1)).deleteById(appointmentId);
    }
}
