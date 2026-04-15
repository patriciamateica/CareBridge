package com.carebridge.backend.nursedetails;

import com.carebridge.backend.nursedetails.model.NurseDetails;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class NurseDetailsService {
    private final NurseDetailsRepository repository;

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
        return repository.save(nurseDetails);
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
}
