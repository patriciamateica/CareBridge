package com.carebridge.backend.healthStatus;

import com.carebridge.backend.healthStatus.model.HealthStatus;
import com.carebridge.backend.healthStatus.model.HealthStatusDto;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(HealthStatusController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {HealthStatusController.class})
class HealthStatusControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private HealthStatusService healthStatusService;

    @MockitoBean
    private com.carebridge.backend.healthStatus.HealthStatusMapper mapper;

    @MockitoBean
    private com.carebridge.backend.user.UserService userService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private HealthStatus sampleStatus;
    private HealthStatusDto sampleStatusDto;
    private UUID statusId;
    private UUID patientId;

    @BeforeEach
    void setUp() {
        statusId = UUID.randomUUID();
        patientId = UUID.randomUUID();

        com.carebridge.backend.user.model.User patient = new com.carebridge.backend.user.model.User();
        patient.setId(patientId);
        patient.setRole(com.carebridge.backend.user.Role.PATIENT);
        patient.setEmail("patient@test.com");

        sampleStatus = new HealthStatus(statusId, 2, null, List.of("Cough"), "Mild symptoms", LocalDate.now(), patient);
        sampleStatusDto = new HealthStatusDto(statusId, 2, null, List.of("Cough"), "Mild symptoms", LocalDate.now(), patientId);
    }

    @Test
    void getAll_ShouldReturnPagedDtos() throws Exception {
        Page<HealthStatus> mockPage = new PageImpl<>(List.of(sampleStatus));

        when(healthStatusService.findAll(any(Pageable.class))).thenReturn(mockPage);
        when(mapper.toDto(any(HealthStatus.class))).thenReturn(sampleStatusDto);

        mockMvc.perform(get("/api/health-status")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(statusId.toString()))
            .andExpect(jsonPath("$.content[0].painScale").value(2))
            .andExpect(jsonPath("$.content[0].notes").value("Mild symptoms"));
    }

    @Test
    void getById_ShouldReturnDto() throws Exception {
        when(healthStatusService.getById(statusId)).thenReturn(sampleStatus);
        when(mapper.toDto(sampleStatus)).thenReturn(sampleStatusDto);

        mockMvc.perform(get("/api/health-status/{id}", statusId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(statusId.toString()))
            .andExpect(jsonPath("$.symptoms[0]").value("Cough"));
    }

    @Test
    void create_ShouldReturnCreatedDto() throws Exception {
        when(userService.getUserById(patientId)).thenReturn(sampleStatus.getPatient());
        when(mapper.toEntity(any(HealthStatusDto.class), any(com.carebridge.backend.user.model.User.class))).thenReturn(sampleStatus);
        when(healthStatusService.create(any(HealthStatus.class))).thenReturn(sampleStatus);
        when(mapper.toDto(any(HealthStatus.class))).thenReturn(sampleStatusDto);

        mockMvc.perform(post("/api/health-status")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleStatusDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.painScale").value(2));
    }

    @Test
    void update_ShouldReturnUpdatedDto() throws Exception {
        when(userService.getUserById(patientId)).thenReturn(sampleStatus.getPatient());
        when(mapper.toEntity(any(HealthStatusDto.class), any(com.carebridge.backend.user.model.User.class))).thenReturn(sampleStatus);
        when(healthStatusService.update(eq(statusId), any(HealthStatus.class))).thenReturn(sampleStatus);
        when(mapper.toDto(any(HealthStatus.class))).thenReturn(sampleStatusDto);

        mockMvc.perform(put("/api/health-status/{id}", statusId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleStatusDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.notes").value("Mild symptoms"));
    }

    @Test
    void delete_ShouldReturnTrueWhenSuccessful() throws Exception {
        when(healthStatusService.delete(statusId)).thenReturn(true);

        mockMvc.perform(delete("/api/health-status/{id}", statusId))
            .andExpect(status().isOk())
            .andExpect(content().string("true"));
    }
}
