package com.carebridge.backend.task;

import com.carebridge.backend.task.model.Task;
import com.carebridge.backend.task.model.TaskStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class TaskService {
    private final TaskRepository repository;

    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public Page<Task> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public Task getById(UUID id) {
        return repository.findById(id).orElseThrow();
    }

    public Task create(Task task) {
        if (task.getStatus() == null) {
            task.setStatus(TaskStatus.OPEN);
        }
        return repository.save(task);
    }

    public Task update(UUID id, Task updatedTask) {
        Task oldTask = repository.findById(id).orElseThrow();

        oldTask.setTitle(updatedTask.getTitle());
        oldTask.setDescription(updatedTask.getDescription());
        oldTask.setTaskType(updatedTask.getTaskType());
        oldTask.setNeededBy(updatedTask.getNeededBy());
        oldTask.setStatus(updatedTask.getStatus());
        oldTask.setPatientId(updatedTask.getPatientId());
        oldTask.setClaimerId(updatedTask.getClaimerId());

        return oldTask;
    }

    public boolean delete(UUID id) {
        repository.findById(id).orElseThrow();
        repository.deleteById(id);
        return true;
    }
}
