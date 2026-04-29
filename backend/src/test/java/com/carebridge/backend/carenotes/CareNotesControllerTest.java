package com.carebridge.backend.carenotes;

import com.carebridge.backend.carenotes.model.CareNotes;
import com.carebridge.backend.carenotes.model.CareNotesDto;
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

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(com.carebridge.backend.carenotes.CareNotesController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {CareNotesController.class})
class CareNotesControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CareNotesService service;

    @MockitoBean
    private CareNotesMapper mapper;

    @MockitoBean
    private com.carebridge.backend.user.UserService userService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private CareNotes sampleNotes;
    private CareNotesDto sampleNotesDto;
    private UUID notesId;
    private UUID patientId;
    private UUID nurseId;
    private LocalDateTime timestamp;
    private com.carebridge.backend.user.model.User patientUser;
    private com.carebridge.backend.user.model.User nurseUser;

    @BeforeEach
    void setUp() {
        notesId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        nurseId = UUID.randomUUID();
        timestamp = LocalDateTime.now();

        objectMapper.registerModule(new JavaTimeModule());

        patientUser = new com.carebridge.backend.user.model.User();
        patientUser.setId(patientId);
        patientUser.setRole(com.carebridge.backend.user.Role.PATIENT);
        patientUser.setEmail("patient@test.com");

        nurseUser = new com.carebridge.backend.user.model.User();
        nurseUser.setId(nurseId);
        nurseUser.setRole(com.carebridge.backend.user.Role.NURSE);
        nurseUser.setEmail("nurse@test.com");

        sampleNotes = new CareNotes(notesId, "Vitals are stable.", patientUser, nurseUser, timestamp);
        sampleNotesDto = new CareNotesDto(notesId, "Vitals are stable.", patientId, nurseId, timestamp);
    }

    @Test
    void getAll_ShouldReturnPagedDtos() throws Exception {
        Page<CareNotes> mockPage = new PageImpl<>(List.of(sampleNotes));

        when(service.findAll(any(Pageable.class))).thenReturn(mockPage);
        when(mapper.toDto(any(CareNotes.class))).thenReturn(sampleNotesDto);

        mockMvc.perform(get("/api/care-notes")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(notesId.toString()))
            .andExpect(jsonPath("$.content[0].content").value("Vitals are stable."));
    }

    @Test
    void getById_ShouldReturnDto() throws Exception {
        when(service.getById(notesId)).thenReturn(sampleNotes);
        when(mapper.toDto(sampleNotes)).thenReturn(sampleNotesDto);

        mockMvc.perform(get("/api/care-notes/{id}", notesId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(notesId.toString()))
            .andExpect(jsonPath("$.content").value("Vitals are stable."));
    }

    @Test
    void create_ShouldReturnCreatedDto() throws Exception {
        when(userService.getUserById(patientId)).thenReturn(patientUser);
        when(userService.getUserById(nurseId)).thenReturn(nurseUser);
        when(mapper.toEntity(any(CareNotesDto.class), any(com.carebridge.backend.user.model.User.class), any(com.carebridge.backend.user.model.User.class))).thenReturn(sampleNotes);
        when(service.create(any(CareNotes.class))).thenReturn(sampleNotes);
        when(mapper.toDto(any(CareNotes.class))).thenReturn(sampleNotesDto);

        mockMvc.perform(post("/api/care-notes")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleNotesDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").value("Vitals are stable."));
    }

    @Test
    void update_ShouldReturnUpdatedDto() throws Exception {
        when(userService.getUserById(patientId)).thenReturn(patientUser);
        when(userService.getUserById(nurseId)).thenReturn(nurseUser);
        when(mapper.toEntity(any(CareNotesDto.class), any(com.carebridge.backend.user.model.User.class), any(com.carebridge.backend.user.model.User.class))).thenReturn(sampleNotes);
        when(service.update(eq(notesId), any(CareNotes.class))).thenReturn(sampleNotes);
        when(mapper.toDto(any(CareNotes.class))).thenReturn(sampleNotesDto);

        mockMvc.perform(put("/api/care-notes/{id}", notesId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleNotesDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content").value("Vitals are stable."));
    }

    @Test
    void delete_ShouldReturnTrueWhenSuccessful() throws Exception {
        when(service.delete(notesId)).thenReturn(true);

        mockMvc.perform(delete("/api/care-notes/{id}", notesId))
            .andExpect(status().isOk())
            .andExpect(content().string("true"));
    }
}
