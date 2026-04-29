package com.carebridge.backend.nursedetails;

import com.carebridge.backend.nursedetails.model.NurseDetails;
import com.carebridge.backend.nursedetails.model.NurseDetailsDto;
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

@WebMvcTest(NurseDetailsController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {NurseDetailsController.class})
class NurseDetailsControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private NurseDetailsService service;

    @MockitoBean
    private NurseDetailsMapper mapper;

    @MockitoBean
    private com.carebridge.backend.user.UserService userService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private NurseDetails sampleDetails;
    private NurseDetailsDto sampleDetailsDto;
    private UUID detailsId;
    private UUID userId;

    @BeforeEach
    void setUp() {
        detailsId = UUID.randomUUID();
        userId = UUID.randomUUID();

        User testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("nurse@test.com");

        sampleDetails = new NurseDetails(testUser, "Oncology", "City Hospital", 8, true);
        sampleDetailsDto = new NurseDetailsDto(detailsId, userId, "Oncology", "City Hospital", 8, true);
    }

    @Test
    void getAll_ShouldReturnPagedDtos() throws Exception {
        Page<NurseDetails> mockPage = new PageImpl<>(List.of(sampleDetails));

        when(service.findAll(any(Pageable.class))).thenReturn(mockPage);
        when(mapper.toDto(any(NurseDetails.class))).thenReturn(sampleDetailsDto);

        mockMvc.perform(get("/api/nurse-details")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(detailsId.toString()))
            .andExpect(jsonPath("$.content[0].specialization").value("Oncology"));
    }

    @Test
    void getById_ShouldReturnDto() throws Exception {
        when(service.getById(detailsId)).thenReturn(sampleDetails);
        when(mapper.toDto(sampleDetails)).thenReturn(sampleDetailsDto);

        mockMvc.perform(get("/api/nurse-details/{id}", detailsId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(detailsId.toString()))
            .andExpect(jsonPath("$.experienceYears").value(8));
    }

    @Test
    void create_ShouldReturnCreatedDto() throws Exception {
        User testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("nurse@test.com");
        testUser.setRole(com.carebridge.backend.user.Role.NURSE);

        when(userService.getUserById(userId)).thenReturn(testUser);
        when(mapper.toEntity(any(NurseDetailsDto.class), any(User.class))).thenReturn(sampleDetails);
        when(service.create(any(NurseDetails.class))).thenReturn(sampleDetails);
        when(mapper.toDto(any(NurseDetails.class))).thenReturn(sampleDetailsDto);

        mockMvc.perform(post("/api/nurse-details")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleDetailsDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.hospitalAffiliation").value("City Hospital"));
    }

    @Test
    void update_ShouldReturnUpdatedDto() throws Exception {
        User testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("nurse@test.com");
        testUser.setRole(com.carebridge.backend.user.Role.NURSE);

        when(userService.getUserById(userId)).thenReturn(testUser);
        when(mapper.toEntity(any(NurseDetailsDto.class), any(User.class))).thenReturn(sampleDetails);
        when(service.update(eq(detailsId), any(NurseDetails.class))).thenReturn(sampleDetails);
        when(mapper.toDto(any(NurseDetails.class))).thenReturn(sampleDetailsDto);

        mockMvc.perform(put("/api/nurse-details/{id}", detailsId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleDetailsDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.specialization").value("Oncology"));
    }

    @Test
    void delete_ShouldReturnTrueWhenSuccessful() throws Exception {
        when(service.delete(detailsId)).thenReturn(true);

        mockMvc.perform(delete("/api/nurse-details/{id}", detailsId))
            .andExpect(status().isOk())
            .andExpect(content().string("true"));
    }
}
