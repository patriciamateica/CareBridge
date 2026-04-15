package com.carebridge.backend.roster;

import com.carebridge.backend.roster.model.Roster;
import com.carebridge.backend.roster.model.RosterDto;
import org.springframework.stereotype.Component;

@Component
public class RosterMapper {

    public RosterDto toDto(Roster roster) {
        return new RosterDto(
            roster.getId(),
            roster.getPatientId(),
            roster.getNurseId(),
            roster.getStatus()
        );
    }

    public Roster toEntity(RosterDto dto) {
        return new Roster(
            dto.id(),
            dto.patientId(),
            dto.nurseId(),
            dto.status()
        );
    }
}
