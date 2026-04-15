package com.carebridge.backend.clinicalLog;

import com.carebridge.backend.clinicalLog.model.ClinicalLog;
import com.carebridge.backend.clinicalLog.model.ClinicalLogDto;
import com.carebridge.backend.clinicalLog.model.ClinicalLogStatus;
import com.carebridge.backend.clinicalLog.model.DocumentType;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(ClinicalLogController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {ClinicalLogController.class})
class ClinicalLogControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ClinicalLogService service;

    @MockitoBean
    private ClinicalLogMapper mapper;

    @MockitoBean
    private com.carebridge.backend.user.UserService userService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private ClinicalLog sampleLog;
    private ClinicalLogDto sampleLogDto;
    private UUID logId;

    @BeforeEach
    void setUp() {
        logId = UUID.randomUUID();

        objectMapper.registerModule(new JavaTimeModule());

        sampleLog = new ClinicalLog(logId, "Chest X-Ray", DocumentType.X_RAY, LocalDate.now(), "url", UUID.randomUUID(), UUID.randomUUID(), LocalDateTime.now(), ClinicalLogStatus.ACTIVE);
        sampleLogDto = new ClinicalLogDto(logId, "Chest X-Ray", DocumentType.X_RAY, LocalDate.now(), "url", UUID.randomUUID(), UUID.randomUUID(), LocalDateTime.now(), ClinicalLogStatus.ACTIVE);
    }

    @Test
    void getAll_ShouldReturnPagedDtos() throws Exception {
        Page<ClinicalLog> mockPage = new PageImpl<>(List.of(sampleLog));

        when(service.findAll(any(Pageable.class))).thenReturn(mockPage);
        when(mapper.toDto(any(ClinicalLog.class))).thenReturn(sampleLogDto);

        mockMvc.perform(get("/api/clinical-logs")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(logId.toString()))
            .andExpect(jsonPath("$.content[0].documentTitle").value("Chest X-Ray"));
    }

    @Test
    void getById_ShouldReturnDto() throws Exception {
        when(service.getById(logId)).thenReturn(sampleLog);
        when(mapper.toDto(sampleLog)).thenReturn(sampleLogDto);

        mockMvc.perform(get("/api/clinical-logs/{id}", logId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(logId.toString()))
            .andExpect(jsonPath("$.documentType").value("X_RAY"));
    }

    @Test
    void create_ShouldReturnCreatedDto() throws Exception {
        when(mapper.toEntity(any(ClinicalLogDto.class))).thenReturn(sampleLog);
        when(service.create(any(ClinicalLog.class))).thenReturn(sampleLog);
        when(mapper.toDto(any(ClinicalLog.class))).thenReturn(sampleLogDto);

        mockMvc.perform(post("/api/clinical-logs")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleLogDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.documentTitle").value("Chest X-Ray"));
    }

    @Test
    void delete_ShouldReturnTrueWhenSuccessful() throws Exception {
        when(service.delete(logId)).thenReturn(true);

        mockMvc.perform(delete("/api/clinical-logs/{id}", logId))
            .andExpect(status().isOk())
            .andExpect(content().string("true"));
    }
}
