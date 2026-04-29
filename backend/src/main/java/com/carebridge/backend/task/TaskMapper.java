package com.carebridge.backend.task;

import com.carebridge.backend.task.model.Task;
import com.carebridge.backend.task.model.TaskDto;
import com.carebridge.backend.user.model.User;
import org.springframework.stereotype.Component;

@Component
public class TaskMapper {

    public TaskDto toDto(Task task) {
        return new TaskDto(
            task.getId(),
            task.getTitle(),
            task.getDescription(),
            task.getTaskType(),
            task.getNeededBy(),
            task.getStatus(),
            task.getPatient().getId(),
            task.getClaimer().getId()
        );
    }

    public Task toEntity(TaskDto dto, User claimer, User patient) {
        return new Task(
            dto.id(),
            dto.title(),
            dto.description(),
            dto.taskType(),
            dto.neededBy(),
            dto.status(),
            patient,
            claimer
        );
    }
}
