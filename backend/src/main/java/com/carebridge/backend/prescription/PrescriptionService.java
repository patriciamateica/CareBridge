package com.carebridge.backend.prescription;

import com.carebridge.backend.prescription.model.Prescription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class PrescriptionService {
    private final PrescriptionRepository prescriptionRepository;
    private final Sinks.Many<Prescription> createdSink = Sinks.many().multicast().onBackpressureBuffer();
    private final Sinks.Many<UUID> deletedSink = Sinks.many().multicast().onBackpressureBuffer();

    public PrescriptionService(PrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
    }

    @Transactional(readOnly = true)
    public Page<Prescription> findAll(Pageable pageable) {
        return prescriptionRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Page<Prescription> findByPatientId(UUID patientId, Pageable pageable) {
        return prescriptionRepository.findByPatientId(patientId, pageable);
    }

    @Transactional(readOnly = true)
    public Prescription getById(UUID id) {
        return prescriptionRepository.findById(id).orElseThrow();
    }

    @Transactional
    public Prescription create(Prescription prescription) {
        Prescription saved = prescriptionRepository.save(prescription);
        createdSink.tryEmitNext(saved);
        return saved;
    }

    @Transactional
    public Prescription update(UUID id, Prescription updatedPrescription) {
        Prescription oldPrescription = prescriptionRepository.findById(id).orElseThrow();

        oldPrescription.setName(updatedPrescription.getName());
        oldPrescription.setDose(updatedPrescription.getDose());
        oldPrescription.setTiming(updatedPrescription.getTiming());
        oldPrescription.setPatient(updatedPrescription.getPatient());
        oldPrescription.setNurse(updatedPrescription.getNurse());

        return prescriptionRepository.save(oldPrescription);
    }

    @Transactional
    public boolean delete(UUID id) {
        Prescription prescription = prescriptionRepository.findById(id).orElseThrow();
        prescriptionRepository.deleteById(id);
        deletedSink.tryEmitNext(prescription.getPatient().getId());
        return true;
    }

    @Transactional
    public void deleteAll() {
        prescriptionRepository.deleteAll();
    }

    @Transactional(readOnly = true)
    public List<Prescription> getByPatientId(UUID patientId) {
        return prescriptionRepository.findByPatientIdOrderByIdAsc(patientId);
    }

    @Transactional(readOnly = true)
    public long countByPatientId(UUID patientId) {
        return prescriptionRepository.countByPatientId(patientId);
    }

    @Transactional
    public Flux<Prescription> getCreatedStream(UUID patientId) {
        return createdSink.asFlux()
            .filter(p -> patientId == null || p.getPatient().getId().equals(patientId));
    }

    @Transactional(readOnly = true)
    public Flux<UUID> getDeletedStream(UUID patientId) {
        return deletedSink.asFlux();
    }
}
