package com.carebridge.backend.task;

import com.carebridge.backend.task.model.Task;
import com.carebridge.backend.task.model.TaskStatus;
import com.carebridge.backend.task.model.TaskType;
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

    @BeforeEach
    void setUp() {
        taskId = UUID.randomUUID();
        sampleTask = new Task(
            taskId,
            "Drive to Clinic",
            "Needs a ride at 2 PM.",
            TaskType.TRANSPORTATION,
            LocalDateTime.now(),
            TaskStatus.OPEN,
            UUID.randomUUID(),
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
        Task newTask = new Task(null, "Cook Dinner", "Pasta", TaskType.MEAL_PREP, LocalDateTime.now(), null, UUID.randomUUID(), null);
        when(repository.save(newTask)).thenReturn(newTask);

        Task result = service.create(newTask);

        assertEquals(TaskStatus.OPEN, result.getStatus());
        verify(repository, times(1)).save(newTask);
    }

    @Test
    void update_ShouldUpdateAndReturnTaskWhenExists() {
        UUID newClaimerId = UUID.randomUUID();
        Task updatedData = new Task(null, "Drive to Clinic", "Needs a ride at 2 PM.", TaskType.TRANSPORTATION, LocalDateTime.now(), TaskStatus.CLAIMED, sampleTask.getPatientId(), newClaimerId);
        when(repository.findById(taskId)).thenReturn(Optional.of(sampleTask));

        Task result = service.update(taskId, updatedData);

        assertEquals(TaskStatus.CLAIMED, result.getStatus());
        assertEquals(newClaimerId, result.getClaimerId());
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
