package com.carebridge.backend.task;

import com.carebridge.backend.task.model.Task;
import com.carebridge.backend.task.model.TaskStatus;
import com.carebridge.backend.task.model.TaskType;
import com.carebridge.backend.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TaskServiceTest {

    @Mock
    private TaskRepository repository;

    @InjectMocks
    private TaskService service;

    private Task sampleTask;
    private UUID taskId;
    private User patient;

    @BeforeEach
    void setUp() {
        taskId = UUID.randomUUID();
        patient = new User();
        patient.setId(UUID.randomUUID());

        sampleTask = new Task(
            taskId,
            "Drive to Clinic",
            "Needs a ride at 2 PM.",
            TaskType.TRANSPORTATION,
            LocalDateTime.now(),
            TaskStatus.OPEN,
            patient,
            null
        );
    }

    @Test
    void findAll_ShouldReturnPageOfTasks() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Task> mockPage = new PageImpl<>(List.of(sampleTask));
        when(repository.findAll(pageRequest)).thenReturn(mockPage);

        Page<Task> result = service.findAll(pageRequest);

        assertEquals(1, result.getTotalElements());
        verify(repository, times(1)).findAll(pageRequest);
    }

    @Test
    void getById_ShouldReturnTaskWhenExists() {
        when(repository.findById(taskId)).thenReturn(Optional.of(sampleTask));

        Task result = service.getById(taskId);

        assertNotNull(result);
        assertEquals(taskId, result.getId());
    }

    @Test
    void getById_ShouldThrowExceptionWhenNotExists() {
        when(repository.findById(taskId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.getById(taskId));
    }

    @Test
    void create_ShouldSetStatusOpenIfNullAndSave() {
        User newPatient = new User();
        newPatient.setId(UUID.randomUUID());
        Task newTask = new Task(null, "Cook Dinner", "Pasta", TaskType.MEAL_PREP, LocalDateTime.now(), null, newPatient, null);
        when(repository.save(newTask)).thenReturn(newTask);

        Task result = service.create(newTask);

        assertEquals(TaskStatus.OPEN, result.getStatus());
        verify(repository, times(1)).save(newTask);
    }

    @Test
    void update_ShouldUpdateAndReturnTaskWhenExists() {
        User newClaimer = new User();
        newClaimer.setId(UUID.randomUUID());
        Task updatedData = new Task(null, "Drive to Clinic", "Needs a ride at 2 PM.", TaskType.TRANSPORTATION, LocalDateTime.now(), TaskStatus.CLAIMED, patient, newClaimer);
        when(repository.findById(taskId)).thenReturn(Optional.of(sampleTask));
        when(repository.save(any(Task.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Task result = service.update(taskId, updatedData);

        assertNotNull(result);
        assertEquals(TaskStatus.CLAIMED, result.getStatus());
        assertEquals(newClaimer, result.getClaimer());
        assertEquals(taskId, result.getId());
    }

    @Test
    void delete_ShouldRemoveTaskWhenExists() {
        when(repository.findById(taskId)).thenReturn(Optional.of(sampleTask));

        boolean result = service.delete(taskId);

        assertTrue(result);
        verify(repository, times(1)).deleteById(taskId);
    }
}
