package com.carebridge.backend.prescription;

import com.carebridge.backend.prescription.model.Prescription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.UUID;

@Service
public class PrescriptionService {
    private final PrescriptionRepository prescriptionRepository;
    private final Sinks.Many<Prescription> createdSink = Sinks.many().multicast().onBackpressureBuffer();
    private final Sinks.Many<UUID> deletedSink = Sinks.many().multicast().onBackpressureBuffer();

    public PrescriptionService(PrescriptionRepository prescriptionRepository) {
        this.prescriptionRepository = prescriptionRepository;
    }

    public Page<Prescription> findAll(Pageable pageable) {
        return prescriptionRepository.findAll(pageable);
    }

    public Prescription getById(UUID id) {
        return prescriptionRepository.findById(id).orElseThrow();
    }

    public Prescription create(Prescription prescription) {
        Prescription saved = prescriptionRepository.save(prescription);
        createdSink.tryEmitNext(saved);
        return saved;
    }

    public Prescription update(UUID id, Prescription updatedPrescription) {
        Prescription oldPrescription = prescriptionRepository.findById(id).orElseThrow();

        oldPrescription.setName(updatedPrescription.getName());
        oldPrescription.setDose(updatedPrescription.getDose());
        oldPrescription.setTiming(updatedPrescription.getTiming());
        oldPrescription.setPatientId(updatedPrescription.getPatientId());
        oldPrescription.setNurseId(updatedPrescription.getNurseId());

        return oldPrescription;
    }

    public boolean delete(UUID id) {
        Prescription prescription = prescriptionRepository.findById(id).orElseThrow();
        prescriptionRepository.deleteById(id);
        deletedSink.tryEmitNext(prescription.getPatientId());
        return true;
    }

    public List<Prescription> getByPatientId(UUID patientId) {
        return prescriptionRepository.findByPatientId(patientId);
    }

    public Flux<Prescription> getCreatedStream(UUID patientId) {
        return createdSink.asFlux()
                .filter(p -> patientId == null || p.getPatientId().equals(patientId));
    }

    public Flux<UUID> getDeletedStream(UUID patientId) {
        return deletedSink.asFlux(); // Simple for now, ideally filter by some context
    }
}
