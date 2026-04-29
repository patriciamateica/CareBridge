package com.carebridge.backend.task;

import com.carebridge.backend.task.model.Task;
import com.carebridge.backend.task.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.UUID;

@Service
@Transactional
public class TaskService {
    private final TaskRepository repository;
    private final Sinks.Many<Task> taskSink = Sinks.many().multicast().onBackpressureBuffer();

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<Task> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Task getById(UUID id) {
        return repository.findById(id).orElseThrow();
    }

    @Transactional
    public Task create(Task task) {
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.OPEN);
        }
        Task savedTask = repository.save(task);
        taskSink.tryEmitNext(savedTask);
        return savedTask;
    }

    @Transactional
    public Task update(UUID id, Task updatedTask) {
        Task oldTask = repository.findById(id).orElseThrow();

        oldTask.setTitle(updatedTask.getTitle());
        oldTask.setDescription(updatedTask.getDescription());
        oldTask.setTaskType(updatedTask.getTaskType());
        oldTask.setNeededBy(updatedTask.getNeededBy());
        oldTask.setStatus(updatedTask.getStatus());
        oldTask.setPatient(updatedTask.getPatient());
        oldTask.setClaimer(updatedTask.getClaimer());

        Task savedTask = repository.save(oldTask);
        taskSink.tryEmitNext(savedTask);
        return savedTask;
    }

    @Transactional
    public boolean delete(UUID id) {
        repository.findById(id).orElseThrow();
        repository.deleteById(id);
        return true;
    }

    @Transactional
    public void deleteAll() {
        repository.deleteAll();
    }

    @Transactional
    public Flux<Task> getNewTaskStream(UUID patientId) {
        return taskSink.asFlux()
            .filter(task -> patientId == null || task.getPatient().getId().equals(patientId));
    }
}
