package com.carebridge.backend.appointments;

import com.carebridge.backend.appointments.model.Appointments;
import com.carebridge.backend.appointments.model.AppointmentsDto;
import com.carebridge.backend.appointments.model.AppointmentsStatus;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(AppointmentsController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {AppointmentsController.class})
class AppointmentsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private AppointmentsService appointmentsService;

    @MockitoBean
    private AppointmentsMapper mapper;

    @MockitoBean
    private com.carebridge.backend.user.UserService userService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private Appointments sampleAppointment;
    private AppointmentsDto sampleAppointmentDto;
    private UUID appointmentId;
    private UUID patientId;
    private UUID nurseId;

    @BeforeEach
    void setUp() {
        appointmentId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        nurseId = UUID.randomUUID();

        com.carebridge.backend.user.model.User patient = new com.carebridge.backend.user.model.User();
        patient.setId(patientId);
        patient.setRole(com.carebridge.backend.user.Role.PATIENT);
        patient.setEmail("patient@test.com");

        com.carebridge.backend.user.model.User nurse = new com.carebridge.backend.user.model.User();
        nurse.setId(nurseId);
        nurse.setRole(com.carebridge.backend.user.Role.NURSE);
        nurse.setEmail("nurse@test.com");

        sampleAppointment = new Appointments(appointmentId, patient, nurse, "Physiotherapy", LocalDateTime.now(), AppointmentsStatus.REQUESTED);
        sampleAppointmentDto = new AppointmentsDto(appointmentId, patientId, nurseId, "Physiotherapy", LocalDateTime.now(), AppointmentsStatus.REQUESTED);
    }

    @Test
    void getAll_ShouldReturnPagedDtos() throws Exception {
        Page<Appointments> mockPage = new PageImpl<>(List.of(sampleAppointment));

        when(appointmentsService.findAll(any(Pageable.class))).thenReturn(mockPage);
        when(mapper.toDto(any(Appointments.class))).thenReturn(sampleAppointmentDto);

        mockMvc.perform(get("/api/appointments")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(appointmentId.toString()))
            .andExpect(jsonPath("$.content[0].description").value("Physiotherapy"))
            .andExpect(jsonPath("$.content[0].status").value("REQUESTED"));
    }

    @Test
    void getById_ShouldReturnDto() throws Exception {
        when(appointmentsService.getById(appointmentId)).thenReturn(sampleAppointment);
        when(mapper.toDto(sampleAppointment)).thenReturn(sampleAppointmentDto);

        mockMvc.perform(get("/api/appointments/{id}", appointmentId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(appointmentId.toString()))
            .andExpect(jsonPath("$.description").value("Physiotherapy"));
    }

    @Test
    void create_ShouldReturnCreatedDto() throws Exception {
        when(userService.getUserById(patientId)).thenReturn((com.carebridge.backend.user.model.User) sampleAppointment.getPatient());
        when(userService.getUserById(nurseId)).thenReturn((com.carebridge.backend.user.model.User) sampleAppointment.getNurse());
        when(mapper.toEntity(any(AppointmentsDto.class), any(com.carebridge.backend.user.model.User.class), any(com.carebridge.backend.user.model.User.class))).thenReturn(sampleAppointment);
        when(appointmentsService.create(any(Appointments.class))).thenReturn(sampleAppointment);
        when(mapper.toDto(any(Appointments.class))).thenReturn(sampleAppointmentDto);

        mockMvc.perform(post("/api/appointments")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleAppointmentDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("REQUESTED"));
    }

    @Test
    void update_ShouldReturnUpdatedDto() throws Exception {
        when(userService.getUserById(patientId)).thenReturn((com.carebridge.backend.user.model.User) sampleAppointment.getPatient());
        when(userService.getUserById(nurseId)).thenReturn((com.carebridge.backend.user.model.User) sampleAppointment.getNurse());
        when(mapper.toEntity(any(AppointmentsDto.class), any(com.carebridge.backend.user.model.User.class), any(com.carebridge.backend.user.model.User.class))).thenReturn(sampleAppointment);
        when(appointmentsService.update(eq(appointmentId), any(Appointments.class))).thenReturn(sampleAppointment);
        when(mapper.toDto(any(Appointments.class))).thenReturn(sampleAppointmentDto);

        mockMvc.perform(put("/api/appointments/{id}", appointmentId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleAppointmentDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.description").value("Physiotherapy"));
    }

    @Test
    void delete_ShouldReturnTrueWhenSuccessful() throws Exception {
        when(appointmentsService.delete(appointmentId)).thenReturn(true);

        mockMvc.perform(delete("/api/appointments/{id}", appointmentId))
            .andExpect(status().isOk())
            .andExpect(content().string("true"));
    }
}
