package com.carebridge.backend.patientDetails;

import com.carebridge.backend.patientDetails.model.PatientDetails;
import com.carebridge.backend.patientDetails.model.PatientDetailsDto;
import com.carebridge.backend.user.model.User;
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

import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(com.carebridge.backend.patientDetails.PatientDetailsController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {PatientDetailsController.class})
class PatientDetailsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PatientDetailsService service;

    @MockitoBean
    private PatientDetailsMapper mapper;

    @MockitoBean
    private com.carebridge.backend.user.UserService userService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private PatientDetails sampleDetails;
    private PatientDetailsDto sampleDetailsDto;
    private UUID detailsId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        detailsId = UUID.randomUUID();
        userId = UUID.randomUUID();

        User testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("patient@test.com");

        sampleDetails = new PatientDetails(testUser, "Asthma", List.of("Spirometry"), List.of(), "Brother: 0755123123", UUID.randomUUID());
        sampleDetailsDto = new PatientDetailsDto(detailsId, userId, "Asthma", List.of("Spirometry"), List.of(), "Brother: 0755123123", UUID.randomUUID());
    }

    @Test
    void getAll_ShouldReturnPagedDtos() throws Exception {
        Page<PatientDetails> mockPage = new PageImpl<>(List.of(sampleDetails));

        when(service.findAll(any(Pageable.class))).thenReturn(mockPage);
        when(mapper.toDto(any(PatientDetails.class))).thenReturn(sampleDetailsDto);

        mockMvc.perform(get("/api/patient-details")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(detailsId.toString()))
            .andExpect(jsonPath("$.content[0].primaryDiagnosis").value("Asthma"));
    }

    @Test
    void getById_ShouldReturnDto() throws Exception {
        when(service.getById(detailsId)).thenReturn(sampleDetails);
        when(mapper.toDto(sampleDetails)).thenReturn(sampleDetailsDto);

        mockMvc.perform(get("/api/patient-details/{id}", detailsId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(detailsId.toString()))
            .andExpect(jsonPath("$.primaryDiagnosis").value("Asthma"));
    }

    @Test
    void create_ShouldReturnCreatedDto() throws Exception {
        User testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("patient@test.com");
        testUser.setRole(com.carebridge.backend.user.Role.PATIENT);

        when(userService.getUserById(userId)).thenReturn(testUser);
        when(mapper.toEntity(any(PatientDetailsDto.class), any(User.class))).thenReturn(sampleDetails);
        when(service.create(any(PatientDetails.class))).thenReturn(sampleDetails);
        when(mapper.toDto(any(PatientDetails.class))).thenReturn(sampleDetailsDto);

        mockMvc.perform(post("/api/patient-details")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleDetailsDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.primaryDiagnosis").value("Asthma"));
    }

    @Test
    void update_ShouldReturnUpdatedDto() throws Exception {
        User testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("patient@test.com");
        testUser.setRole(com.carebridge.backend.user.Role.PATIENT);

        when(userService.getUserById(userId)).thenReturn(testUser);
        when(mapper.toEntity(any(PatientDetailsDto.class), any(User.class))).thenReturn(sampleDetails);
        when(service.update(eq(detailsId), any(PatientDetails.class))).thenReturn(sampleDetails);
        when(mapper.toDto(any(PatientDetails.class))).thenReturn(sampleDetailsDto);

        mockMvc.perform(put("/api/patient-details/{id}", detailsId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleDetailsDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.emergencyContact").value("Brother: 0755123123"));
    }

    @Test
    void delete_ShouldReturnTrueWhenSuccessful() throws Exception {
        when(service.delete(detailsId)).thenReturn(true);

        mockMvc.perform(delete("/api/patient-details/{id}", detailsId))
            .andExpect(status().isOk())
            .andExpect(content().string("true"));
    }
}
