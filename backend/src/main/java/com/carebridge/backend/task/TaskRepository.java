package com.carebridge.backend.task;

import com.carebridge.backend.task.model.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "DELETE FROM tasks WHERE patient_id = ?1", nativeQuery = true)
    void deleteByPatientId(UUID patientId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "DELETE FROM tasks WHERE claimer_id = ?1", nativeQuery = true)
    void deleteByClaimerId(UUID claimerId);
}
