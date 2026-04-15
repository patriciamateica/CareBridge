package com.carebridge.backend.vitals;

import com.carebridge.backend.vitals.model.Vitals;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class VitalsService {
    private final VitalsRepository vitalsRepository;

    public VitalsService(VitalsRepository vitalsRepository) {
        this.vitalsRepository = vitalsRepository;
    }

    public Page<Vitals> findAll(Pageable pageable) {
        return vitalsRepository.findAll(pageable);
    }

    public Vitals getById(UUID id) {
        return vitalsRepository.findById(id).orElseThrow();
    }

    public Vitals create(Vitals vitals) {
        return vitalsRepository.save(vitals);
    }

    public Vitals update(UUID id, Vitals vitals) {
        Vitals oldVitals = vitalsRepository.findById(id).orElseThrow();
        oldVitals.setRespiratoryRate(vitals.getRespiratoryRate());
        oldVitals.setBloodPressure(vitals.getBloodPressure());
        oldVitals.setHeartRate(vitals.getHeartRate());
        oldVitals.setSpO2(vitals.getSpO2());
        return oldVitals;
    }

    public boolean delete(UUID id) {
        Vitals vitals = vitalsRepository.findById(id).orElseThrow();
        vitalsRepository.deleteById(id);
        return true;
    }
}
