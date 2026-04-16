package com.carebridge.backend.vitals;

import com.carebridge.backend.vitals.model.Vitals;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.UUID;

@Service
public class VitalsService {
    private final VitalsRepository vitalsRepository;
    private final Sinks.Many<Vitals> vitalsSink = Sinks.many().multicast().onBackpressureBuffer();

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
        Vitals savedVitals = vitalsRepository.save(vitals);
        vitalsSink.tryEmitNext(savedVitals);
        return savedVitals;
    }

    public Vitals update(UUID id, Vitals vitals) {
        Vitals oldVitals = vitalsRepository.findById(id).orElseThrow();
        oldVitals.setRespiratoryRate(vitals.getRespiratoryRate());
        oldVitals.setBloodPressure(vitals.getBloodPressure());
        oldVitals.setHeartRate(vitals.getHeartRate());
        oldVitals.setSpO2(vitals.getSpO2());
        Vitals savedVitals = vitalsRepository.save(oldVitals);
        vitalsSink.tryEmitNext(savedVitals);
        return savedVitals;
    }

    public boolean delete(UUID id) {
        Vitals vitals = vitalsRepository.findById(id).orElseThrow();
        vitalsRepository.deleteById(id);
        return true;
    }

    public List<Vitals> getByPatientId(UUID patientId) {
        return vitalsRepository.findByPatientId(patientId);
    }

    public void deleteAll() {
        vitalsRepository.deleteAll();
    }

    public Flux<Vitals> getVitalsStream(UUID patientId) {
        return vitalsSink.asFlux()
            .filter(v -> patientId == null || v.getPatientId().equals(patientId));
    }
}
