package com.carebridge.backend.task;

import com.carebridge.backend.task.model.Task;
import com.carebridge.backend.task.model.TaskDto;
import com.carebridge.backend.task.model.TaskStatus;
import com.carebridge.backend.task.model.TaskType;
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

@WebMvcTest(TaskController.class)
@AutoConfigureMockMvc(addFilters = false)
@ContextConfiguration(classes = {TaskController.class})
class TaskControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private TaskService service;

    @MockitoBean
    private TaskMapper mapper;

    @MockitoBean
    private com.carebridge.backend.user.UserService userService;

    @MockitoBean
    private PasswordEncoder passwordEncoder;

    private Task sampleTask;
    private TaskDto sampleTaskDto;
    private UUID taskId;
    private UUID patientId;

    @BeforeEach
    void setUp() {
        taskId = UUID.randomUUID();
        patientId = UUID.randomUUID();

        objectMapper.registerModule(new JavaTimeModule());

        sampleTask = new Task(taskId, "Read a book together", "Chapter 4", TaskType.COMPANIONSHIP, LocalDateTime.now(), TaskStatus.OPEN, patientId, null);
        sampleTaskDto = new TaskDto(taskId, "Read a book together", "Chapter 4", TaskType.COMPANIONSHIP, LocalDateTime.now(), TaskStatus.OPEN, patientId, null);
    }

    @Test
    void getAll_ShouldReturnPagedDtos() throws Exception {
        Page<Task> mockPage = new PageImpl<>(List.of(sampleTask));

        when(service.findAll(any(Pageable.class))).thenReturn(mockPage);
        when(mapper.toDto(any(Task.class))).thenReturn(sampleTaskDto);

        mockMvc.perform(get("/api/tasks")
                .param("page", "0")
                .param("size", "10")
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.content[0].id").value(taskId.toString()))
            .andExpect(jsonPath("$.content[0].title").value("Read a book together"));
    }

    @Test
    void getById_ShouldReturnDto() throws Exception {
        when(service.getById(taskId)).thenReturn(sampleTask);
        when(mapper.toDto(sampleTask)).thenReturn(sampleTaskDto);

        mockMvc.perform(get("/api/tasks/{id}", taskId)
                .contentType(MediaType.APPLICATION_JSON))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.id").value(taskId.toString()))
            .andExpect(jsonPath("$.taskType").value("COMPANIONSHIP"));
    }

    @Test
    void create_ShouldReturnCreatedDto() throws Exception {
        when(mapper.toEntity(any(TaskDto.class))).thenReturn(sampleTask);
        when(service.create(any(Task.class))).thenReturn(sampleTask);
        when(mapper.toDto(any(Task.class))).thenReturn(sampleTaskDto);

        mockMvc.perform(post("/api/tasks")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleTaskDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.status").value("OPEN"));
    }

    @Test
    void update_ShouldReturnUpdatedDto() throws Exception {
        when(mapper.toEntity(any(TaskDto.class))).thenReturn(sampleTask);
        when(service.update(eq(taskId), any(Task.class))).thenReturn(sampleTask);
        when(mapper.toDto(any(Task.class))).thenReturn(sampleTaskDto);

        mockMvc.perform(put("/api/tasks/{id}", taskId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(sampleTaskDto)))
            .andExpect(status().isOk())
            .andExpect(jsonPath("$.title").value("Read a book together"));
    }

    @Test
    void delete_ShouldReturnTrueWhenSuccessful() throws Exception {
        when(service.delete(taskId)).thenReturn(true);

        mockMvc.perform(delete("/api/tasks/{id}", taskId))
            .andExpect(status().isOk())
            .andExpect(content().string("true"));
    }
}
