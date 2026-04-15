package com.carebridge.backend.task.model;

import java.time.LocalDateTime;
import java.util.UUID;

public class Task {
    private UUID id;
    private String title;
    private String description;
    private TaskType taskType;
    private LocalDateTime neededBy;
    private TaskStatus status;
    private UUID patientId;
    private UUID claimerId;

    public Task(
        UUID id,
        String title,
        String description,
        TaskType taskType,
        LocalDateTime neededBy,
        TaskStatus status,
        UUID patientId,
        UUID claimerId
    ) {
        this.id = id;
        this.title = title;
        this.description = description;
        this.taskType = taskType;
        this.neededBy = neededBy;
        this.status = status;
        this.patientId = patientId;
        this.claimerId = claimerId;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public TaskType getTaskType() {
        return taskType;
    }

    public void setTaskType(TaskType taskType) {
        this.taskType = taskType;
    }

    public LocalDateTime getNeededBy() {
        return neededBy;
    }

    public void setNeededBy(LocalDateTime neededBy) {
        this.neededBy = neededBy;
    }

    public TaskStatus getStatus() {
        return status;
    }

    public void setStatus(TaskStatus status) {
        this.status = status;
    }

    public UUID getPatientId() {
        return patientId;
    }

    public void setPatientId(UUID patientId) {
        this.patientId = patientId;
    }

    public UUID getClaimerId() {
        return claimerId;
    }

    public void setClaimerId(UUID claimerId) {
        this.claimerId = claimerId;
    }
}
