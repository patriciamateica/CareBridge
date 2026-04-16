package com.carebridge.backend.nursedetails;

import com.carebridge.backend.nursedetails.model.NurseDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.UUID;

@Service
public class NurseDetailsService {
    private final NurseDetailsRepository repository;
    private final Sinks.Many<NurseDetails> createdSink = Sinks.many().multicast().onBackpressureBuffer();
    private final Sinks.Many<NurseDetails> hireStatusSink = Sinks.many().multicast().onBackpressureBuffer();

    public NurseDetailsService(NurseDetailsRepository repository) {
        this.repository = repository;
    }

    public Page<NurseDetails> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    public NurseDetails getById(UUID id) {
        return repository.findById(id).orElseThrow();
    }

    public NurseDetails create(NurseDetails nurseDetails) {
        NurseDetails saved = repository.save(nurseDetails);
        createdSink.tryEmitNext(saved);
        return saved;
    }

    public NurseDetails update(UUID id, NurseDetails updatedDetails) {
        NurseDetails oldDetails = repository.findById(id).orElseThrow();

        oldDetails.setUserId(updatedDetails.getUserId());
        oldDetails.setSpecialization(updatedDetails.getSpecialization());
        oldDetails.setHospitalAffiliation(updatedDetails.getHospitalAffiliation());
        oldDetails.setExperienceYears(updatedDetails.getExperienceYears());
        oldDetails.setHireMeStatus(updatedDetails.isHireMeStatus());

        return oldDetails;
    }

    public boolean delete(UUID id) {
        repository.findById(id).orElseThrow();
        repository.deleteById(id);
        return true;
    }

    public NurseDetails getByUserId(UUID userId) {
        return repository.findByUserId(userId).orElseThrow();
    }

    public NurseDetails updateHireStatus(UUID id, boolean hireMeStatus) {
        NurseDetails details = repository.findById(id).orElseThrow();
        details.setHireMeStatus(hireMeStatus);
        hireStatusSink.tryEmitNext(details);
        return details;
    }

    public Flux<NurseDetails> getCreatedStream(UUID userId) {
        return createdSink.asFlux()
                .filter(nd -> userId == null || nd.getUserId().equals(userId));
    }

    public Flux<NurseDetails> getHireStatusStream(UUID id) {
        return hireStatusSink.asFlux()
                .filter(nd -> id == null || nd.getId().equals(id));
    }
}
