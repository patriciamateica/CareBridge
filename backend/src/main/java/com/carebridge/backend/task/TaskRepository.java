package com.carebridge.backend.task;

import com.carebridge.backend.task.model.Task;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Repository
public interface TaskRepository extends JpaRepository<Task, UUID> {

    /** Pageable query — used by TaskController GET /api/tasks/patient/{patientId} */
    Page<Task> findByPatient_Id(UUID patientId, Pageable pageable);

    /** Pageable query — used by TaskController GET /api/tasks/nurse/{nurseId} */
    Page<Task> findByClaimer_Id(UUID claimerId, Pageable pageable);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM tasks WHERE patient_id = ?1", nativeQuery = true)
    void deleteByPatientId(UUID patientId);

    @Modifying
    @Transactional
    @Query(value = "DELETE FROM tasks WHERE claimer_id = ?1", nativeQuery = true)
    void deleteByClaimerId(UUID claimerId);
}

