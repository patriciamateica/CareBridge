package com.carebridge.backend.vitals;

import com.carebridge.backend.vitals.model.Vitals;
import com.carebridge.backend.vitals.model.VitalsDto;
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
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;


@WebMvcTest(VitalsController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {VitalsController.class})
class VitalsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private VitalsService vitalsService;

    @MockitoBean
    private VitalsMapper vitalsMapper;

    @MockitoBean
    private com.carebridge.backend.user.UserService userService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private Vitals sampleVitals;
    private VitalsDto sampleVitalsDto;
    private UUID vitalsId;
    private UUID patientId;

    @BeforeEach
    void setUp() {
        vitalsId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        sampleVitals = new Vitals(vitalsId, LocalDate.now(), 80, 120, 16, 98, patientId);
        sampleVitalsDto = new VitalsDto(vitalsId, LocalDate.now(), 80, 120, 16, 98, patientId);
    }

    @Test
    void getAllVitals_ShouldReturnPagedDtos() throws Exception {
        Page<Vitals> mockPage = new PageImpl<>(List.of(sampleVitals));

        when(vitalsService.findAll(any(Pageable.class))).thenReturn(mockPage);
        when(vitalsMapper.toDto(any(Vitals.class))).thenReturn(sampleVitalsDto);

        mockMvc.perform(get("/api/vitals")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(vitalsId.toString()))
            .andExpect(jsonPath("$.content[0].heartRate").value(80));
    }

    @Test
    void getById_ShouldReturnVitalsDto() throws Exception {
        when(vitalsService.getById(vitalsId)).thenReturn(sampleVitals);
        when(vitalsMapper.toDto(sampleVitals)).thenReturn(sampleVitalsDto);

        mockMvc.perform(get("/api/vitals/{id}", vitalsId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(vitalsId.toString()))
            .andExpect(jsonPath("$.bloodPressure").value(120));
    }

    @Test
    void create_ShouldReturnCreatedVitalsDto() throws Exception {
        when(vitalsMapper.toEntity(any(VitalsDto.class))).thenReturn(sampleVitals);
        when(vitalsService.create(any(Vitals.class))).thenReturn(sampleVitals);
        when(vitalsMapper.toDto(any(Vitals.class))).thenReturn(sampleVitalsDto);

        mockMvc.perform(post("/api/vitals")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleVitalsDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.heartRate").value(80));
    }

    @Test
    void update_ShouldReturnUpdatedVitalsDto() throws Exception {
        when(vitalsMapper.toEntity(any(VitalsDto.class))).thenReturn(sampleVitals);
        when(vitalsService.update(eq(vitalsId), any(Vitals.class))).thenReturn(sampleVitals);
        when(vitalsMapper.toDto(any(Vitals.class))).thenReturn(sampleVitalsDto);

        mockMvc.perform(put("/api/vitals/{id}", vitalsId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleVitalsDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.respiratoryRate").value(16));
    }

    @Test
    void delete_ShouldReturnTrueWhenSuccessful() throws Exception {
        when(vitalsService.delete(vitalsId)).thenReturn(true);

        mockMvc.perform(delete("/api/vitals/{id}", vitalsId))
            .andExpect(status().isOk())
            .andExpect(content().string("true"));
    }
}
