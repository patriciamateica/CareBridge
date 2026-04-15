package com.carebridge.backend.prescription;

import com.carebridge.backend.prescription.model.Prescription;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class PrescriptionService {
    private final PrescriptionRepository prescriptionRepository;

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
        return prescriptionRepository.save(prescription);
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
        prescriptionRepository.findById(id).orElseThrow();
        prescriptionRepository.deleteById(id);
        return true;
    }
}
