package com.carebridge.backend.roster;

import com.carebridge.backend.roster.model.Roster;
import com.carebridge.backend.roster.model.RosterDto;
import com.carebridge.backend.roster.model.RosterStatus;
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

@WebMvcTest(RosterController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {RosterController.class})
class RosterControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private RosterService rosterService;

    @MockitoBean
    private RosterMapper mapper;

    @MockitoBean
    private com.carebridge.backend.user.UserService userService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private Roster sampleRoster;
    private RosterDto sampleRosterDto;
    private UUID rosterId;
    private UUID patientId;
    private UUID nurseId;
    private User patient;
    private User nurse;

    @BeforeEach
    void setUp() {
        rosterId = UUID.randomUUID();
        patientId = UUID.randomUUID();
        nurseId = UUID.randomUUID();

        patient = new User();
        patient.setId(patientId);
        patient.setRole(com.carebridge.backend.user.Role.PATIENT);

        nurse = new User();
        nurse.setId(nurseId);
        nurse.setRole(com.carebridge.backend.user.Role.NURSE);

        sampleRoster = new Roster(rosterId, patient, nurse, RosterStatus.PENDING);
        sampleRosterDto = new RosterDto(rosterId, patientId, nurseId, RosterStatus.PENDING);
    }

    @Test
    void getAll_ShouldReturnPagedDtos() throws Exception {
        Page<Roster> mockPage = new PageImpl<>(List.of(sampleRoster));

        when(rosterService.findAll(any(Pageable.class))).thenReturn(mockPage);
        when(mapper.toDto(any(Roster.class))).thenReturn(sampleRosterDto);

        mockMvc.perform(get("/api/rosters")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(rosterId.toString()))
            .andExpect(jsonPath("$.content[0].status").value("PENDING"));
    }

    @Test
    void getById_ShouldReturnDto() throws Exception {
        when(rosterService.getById(rosterId)).thenReturn(sampleRoster);
        when(mapper.toDto(sampleRoster)).thenReturn(sampleRosterDto);

        mockMvc.perform(get("/api/rosters/{id}", rosterId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(rosterId.toString()))
            .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void create_ShouldReturnCreatedDto() throws Exception {
        when(userService.getUserById(patientId)).thenReturn(patient);
        when(userService.getUserById(nurseId)).thenReturn(nurse);
        when(mapper.toEntity(any(RosterDto.class), any(User.class), any(User.class))).thenReturn(sampleRoster);
        when(rosterService.create(any(Roster.class))).thenReturn(sampleRoster);
        when(mapper.toDto(any(Roster.class))).thenReturn(sampleRosterDto);

        mockMvc.perform(post("/api/rosters")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleRosterDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void update_ShouldReturnUpdatedDto() throws Exception {
        when(userService.getUserById(patientId)).thenReturn(patient);
        when(userService.getUserById(nurseId)).thenReturn(nurse);
        when(mapper.toEntity(any(RosterDto.class), any(User.class), any(User.class))).thenReturn(sampleRoster);
        when(rosterService.update(eq(rosterId), any(Roster.class))).thenReturn(sampleRoster);
        when(mapper.toDto(any(Roster.class))).thenReturn(sampleRosterDto);

        mockMvc.perform(put("/api/rosters/{id}", rosterId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleRosterDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("PENDING"));
    }

    @Test
    void delete_ShouldReturnTrueWhenSuccessful() throws Exception {
        when(rosterService.delete(rosterId)).thenReturn(true);

        mockMvc.perform(delete("/api/rosters/{id}", rosterId))
            .andExpect(status().isOk())
            .andExpect(content().string("true"));
    }
}
