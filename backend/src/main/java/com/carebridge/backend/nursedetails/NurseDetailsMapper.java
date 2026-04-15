package com.carebridge.backend.nursedetails;

import com.carebridge.backend.nursedetails.model.NurseDetails;
import com.carebridge.backend.nursedetails.model.NurseDetailsDto;
import org.springframework.stereotype.Component;

@Component
public class NurseDetailsMapper {

    public NurseDetailsDto toDto(NurseDetails details) {
        return new NurseDetailsDto(
            details.getId(),
            details.getUserId(),
            details.getSpecialization(),
            details.getHospitalAffiliation(),
            details.getExperienceYears(),
            details.isHireMeStatus()
        );
    }

    public NurseDetails toEntity(NurseDetailsDto dto) {
        return new NurseDetails(
            dto.id(),
            dto.userId(),
            dto.specialization(),
            dto.hospitalAffiliation(),
            dto.experienceYears(),
            dto.hireMeStatus()
        );
    }
}
