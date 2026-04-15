package com.carebridge.backend.task;

import com.carebridge.backend.task.model.Task;
import com.carebridge.backend.task.model.TaskStatus;
import com.carebridge.backend.task.model.TaskType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class TaskRepositoryTest {

    private TaskRepository repository;
    private Task sampleTask;

    @BeforeEach
    void setUp() {
        repository = new TaskRepository();
        sampleTask = new Task(
            null,
            "Grocery Run for Tuesday",
            "Need milk, eggs, and soft bread.",
            TaskType.ERRAND,
            LocalDateTime.now().plusDays(1),
            TaskStatus.OPEN,
            UUID.randomUUID(),
            null // claimerId is null because it's open
        );
    }

    @Test
    void save_ShouldGenerateIdAndStoreTask() {
        Task saved = repository.save(sampleTask);

        assertNotNull(saved.getId());
        assertEquals("Grocery Run for Tuesday", saved.getTitle());
        assertTrue(repository.findById(saved.getId()).isPresent());
    }

    @Test
    void findById_ShouldReturnTaskWhenExists() {
        Task saved = repository.save(sampleTask);

        Optional<Task> found = repository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    void findById_ShouldReturnEmptyWhenNotExists() {
        Optional<Task> found = repository.findById(UUID.randomUUID());

        assertTrue(found.isEmpty());
    }

    @Test
    void findAll_ShouldReturnCorrectPage() {
        for (int i = 0; i < 5; i++) {
            repository.save(new Task(null, "Task " + i, "Desc", TaskType.MEAL_PREP, LocalDateTime.now(), TaskStatus.OPEN, UUID.randomUUID(), null));
        }

        Page<Task> page = repository.findAll(PageRequest.of(0, 2));

        assertEquals(2, page.getContent().size());
        assertEquals(5, page.getTotalElements());
        assertEquals(3, page.getTotalPages());
    }

    @Test
    void deleteById_ShouldRemoveTask() {
        Task saved = repository.save(sampleTask);

        repository.deleteById(saved.getId());

        assertTrue(repository.findById(saved.getId()).isEmpty());
    }
}
