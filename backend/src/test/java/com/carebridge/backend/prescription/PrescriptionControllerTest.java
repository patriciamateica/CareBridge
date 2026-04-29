package com.carebridge.backend.prescription;

import com.carebridge.backend.prescription.model.Prescription;
import com.carebridge.backend.prescription.model.PrescriptionDto;
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

@WebMvcTest(com.carebridge.backend.prescription.PrescriptionController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {PrescriptionController.class})
class PrescriptionControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private PrescriptionService prescriptionService;

    @MockitoBean
    private PrescriptionMapper mapper;

    @MockitoBean
    private com.carebridge.backend.user.UserService userService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private Prescription samplePrescription;
    private PrescriptionDto samplePrescriptionDto;
    private UUID prescriptionId;
    private UUID patientId;
    private UUID nurseId;

    @BeforeEach
    void setUp() {
        prescriptionId = UUID.randomUUID();
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

        samplePrescription = new Prescription(prescriptionId, "Lisinopril", "10mg", "Once daily", patient, nurse);
        samplePrescriptionDto = new PrescriptionDto(prescriptionId, "Lisinopril", "10mg", "Once daily", patientId, nurseId);
    }

    @Test
    void getAll_ShouldReturnPagedDtos() throws Exception {
        Page<Prescription> mockPage = new PageImpl<>(List.of(samplePrescription));

        when(prescriptionService.findAll(any(Pageable.class))).thenReturn(mockPage);
        when(mapper.toDto(any(Prescription.class))).thenReturn(samplePrescriptionDto);

        mockMvc.perform(get("/api/prescriptions")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(prescriptionId.toString()))
            .andExpect(jsonPath("$.content[0].name").value("Lisinopril"))
            .andExpect(jsonPath("$.content[0].dose").value("10mg"));
    }

    @Test
    void getById_ShouldReturnDto() throws Exception {
        when(prescriptionService.getById(prescriptionId)).thenReturn(samplePrescription);
        when(mapper.toDto(samplePrescription)).thenReturn(samplePrescriptionDto);

        mockMvc.perform(get("/api/prescriptions/{id}", prescriptionId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(prescriptionId.toString()))
            .andExpect(jsonPath("$.name").value("Lisinopril"));
    }

    @Test
    void create_ShouldReturnCreatedDto() throws Exception {
        when(userService.getUserById(patientId)).thenReturn((com.carebridge.backend.user.model.User) samplePrescription.getPatient());
        when(userService.getUserById(nurseId)).thenReturn((com.carebridge.backend.user.model.User) samplePrescription.getNurse());
        when(mapper.toEntity(any(PrescriptionDto.class), any(com.carebridge.backend.user.model.User.class), any(com.carebridge.backend.user.model.User.class))).thenReturn(samplePrescription);
        when(prescriptionService.create(any(Prescription.class))).thenReturn(samplePrescription);
        when(mapper.toDto(any(Prescription.class))).thenReturn(samplePrescriptionDto);

        mockMvc.perform(post("/api/prescriptions")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(samplePrescriptionDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.timing").value("Once daily"));
    }

    @Test
    void update_ShouldReturnUpdatedDto() throws Exception {
        when(userService.getUserById(patientId)).thenReturn((com.carebridge.backend.user.model.User) samplePrescription.getPatient());
        when(userService.getUserById(nurseId)).thenReturn((com.carebridge.backend.user.model.User) samplePrescription.getNurse());
        when(mapper.toEntity(any(PrescriptionDto.class), any(com.carebridge.backend.user.model.User.class), any(com.carebridge.backend.user.model.User.class))).thenReturn(samplePrescription);
        when(prescriptionService.update(eq(prescriptionId), any(Prescription.class))).thenReturn(samplePrescription);
        when(mapper.toDto(any(Prescription.class))).thenReturn(samplePrescriptionDto);

        mockMvc.perform(put("/api/prescriptions/{id}", prescriptionId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(samplePrescriptionDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.name").value("Lisinopril"));
    }

    @Test
    void delete_ShouldReturnTrueWhenSuccessful() throws Exception {
        when(prescriptionService.delete(prescriptionId)).thenReturn(true);

        mockMvc.perform(delete("/api/prescriptions/{id}", prescriptionId))
            .andExpect(status().isOk())
            .andExpect(content().string("true"));
    }
}
