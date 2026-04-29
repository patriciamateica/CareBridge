package com.carebridge.backend.task;

import com.carebridge.backend.task.model.Task;
import com.carebridge.backend.task.model.TaskStatus;
import com.carebridge.backend.task.model.TaskType;
import com.carebridge.backend.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class TaskRepositoryTest {

    @Autowired
    private TaskRepository repository;

    @Autowired
    private TestEntityManager entityManager;

    private Task sampleTask;
    private User patient;

    @BeforeEach
    void setUp() {
        patient = new User();
        patient.setFirstName("John");
        patient.setLastName("Doe");
        patient.setEmail("john.doe@example.com");
        patient.setPassword("password");
        patient.setRole(com.carebridge.backend.user.Role.PATIENT);
        entityManager.persist(patient);

        sampleTask = new Task(
                null,
                "Grocery Run for Tuesday",
                "Need milk, eggs, and soft bread.",
                TaskType.ERRAND,
                LocalDateTime.now().plusDays(1),
                TaskStatus.OPEN,
                patient,
                null
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
            User user = new User();
            user.setFirstName("User" + i);
            user.setLastName("Test" + i);
            user.setEmail("user" + i + "@example.com");
            user.setPassword("password");
            user.setRole(com.carebridge.backend.user.Role.PATIENT);
            entityManager.persist(user);
            repository.save(new Task(null, "Task " + i, "Desc", TaskType.MEAL_PREP, LocalDateTime.now(), TaskStatus.OPEN, user, null));
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
