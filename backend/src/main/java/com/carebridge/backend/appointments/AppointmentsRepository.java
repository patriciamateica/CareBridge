package com.carebridge.backend.appointments;

import com.carebridge.backend.appointments.model.Appointments;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface AppointmentsRepository extends JpaRepository<Appointments, UUID> {
    List<Appointments> findByPatientId(UUID patientId);
    Page<Appointments> findByPatientIdOrderByTimeSlotDesc(UUID patientId, Pageable pageable);
    Page<Appointments> findByNurseIdOrderByTimeSlotDesc(UUID nurseId, Pageable pageable);
    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "DELETE FROM appointments WHERE patient_id = ?1", nativeQuery = true)
    void deleteByPatientId(UUID patientId);

    @org.springframework.data.jpa.repository.Modifying
    @org.springframework.transaction.annotation.Transactional
    @org.springframework.data.jpa.repository.Query(value = "DELETE FROM appointments WHERE nurse_id = ?1", nativeQuery = true)
    void deleteByNurseId(UUID nurseId);
}
