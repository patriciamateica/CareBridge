package com.carebridge.backend.nursedetails;

import com.carebridge.backend.nursedetails.model.NurseDetails;
import com.carebridge.backend.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.UUID;

@Service
@Transactional
public class NurseDetailsService {
    private final NurseDetailsRepository repository;
    private final Sinks.Many<NurseDetails> createdSink = Sinks.many().multicast().onBackpressureBuffer();
    private final Sinks.Many<NurseDetails> hireStatusSink = Sinks.many().multicast().onBackpressureBuffer();

    public NurseDetailsService(NurseDetailsRepository repository) {
        this.repository = repository;
    }

    @Transactional(readOnly = true)
    public Page<NurseDetails> findAll(Pageable pageable) {
        return repository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public NurseDetails getById(UUID id) {
        return repository.findById(id).orElseThrow();
    }

    @Transactional
    public NurseDetails create(NurseDetails nurseDetails) {
        if (nurseDetails.getUser() != null) {
            UUID userId = nurseDetails.getUser().getId();
            
            return repository.findByUserId(userId)
                .map(existing -> {
                    existing.setSpecialization(nurseDetails.getSpecialization());
                    existing.setHospitalAffiliation(nurseDetails.getHospitalAffiliation());
                    existing.setExperienceYears(nurseDetails.getExperienceYears());
                    existing.setHireMeStatus(nurseDetails.isHireMeStatus());
                    return repository.save(existing);
                })
                .orElseGet(() -> {
                    User user = nurseDetails.getUser();
                    if (user != null) {
                        user.setNurseDetails(nurseDetails);
                    }
                    NurseDetails saved = repository.save(nurseDetails);
                    createdSink.tryEmitNext(saved);
                    return saved;
                });
        }
        
        NurseDetails saved = repository.save(nurseDetails);
        createdSink.tryEmitNext(saved);
        return saved;
    }

    @Transactional
    public NurseDetails update(UUID id, NurseDetails updatedDetails) {
        NurseDetails oldDetails = repository.findById(id).orElseThrow();

        oldDetails.setUser(updatedDetails.getUser());
        oldDetails.setSpecialization(updatedDetails.getSpecialization());
        oldDetails.setHospitalAffiliation(updatedDetails.getHospitalAffiliation());
        oldDetails.setExperienceYears(updatedDetails.getExperienceYears());
        oldDetails.setHireMeStatus(updatedDetails.isHireMeStatus());

        return repository.save(oldDetails);
    }

    @Transactional
    public boolean delete(UUID id) {
        repository.findById(id).orElseThrow();
        repository.deleteById(id);
        return true;
    }

    @Transactional
    public void deleteAll() {
        repository.deleteAll();
    }

    @Transactional(readOnly = true)
    public NurseDetails getByUserId(UUID userId) {
        return repository.findByUserId(userId).orElseThrow();
    }

    @Transactional
    public NurseDetails updateHireStatus(UUID id, boolean hireMeStatus) {
        NurseDetails details = repository.findById(id).orElseThrow();
        details.setHireMeStatus(hireMeStatus);

        NurseDetails saved = repository.save(details);
        hireStatusSink.tryEmitNext(saved);
        return saved;
    }

    @Transactional(readOnly = true)
    public Flux<NurseDetails> getCreatedStream(UUID userId) {
        return createdSink.asFlux()
            .filter(nd -> userId == null || nd.getUser().getId().equals(userId));
    }

    @Transactional(readOnly = true)
    public Flux<NurseDetails> getHireStatusStream(UUID id) {
        return hireStatusSink.asFlux()
            .filter(nd -> id == null || nd.getId().equals(id));
    }
}
