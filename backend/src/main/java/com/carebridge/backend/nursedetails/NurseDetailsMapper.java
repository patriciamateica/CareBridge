package com.carebridge.backend.nursedetails;

import com.carebridge.backend.nursedetails.model.NurseDetails;
import com.carebridge.backend.nursedetails.model.NurseDetailsDto;
import com.carebridge.backend.user.model.User;
import org.springframework.stereotype.Component;

@Component
public class NurseDetailsMapper {

    public NurseDetailsDto toDto(NurseDetails details) {
        return new NurseDetailsDto(
            details.getId(),
            details.getUser().getId(),
            details.getSpecialization(),
            details.getHospitalAffiliation(),
            details.getExperienceYears(),
            details.isHireMeStatus()
        );
    }

    public NurseDetails toEntity(NurseDetailsDto dto, User user) {
        return new NurseDetails(
            user,
            dto.specialization(),
            dto.hospitalAffiliation(),
            dto.experienceYears(),
            dto.hireMeStatus()
        );
    }
}
