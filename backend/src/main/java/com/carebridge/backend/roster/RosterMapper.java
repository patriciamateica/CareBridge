package com.carebridge.backend.roster;

import com.carebridge.backend.roster.model.Roster;
import com.carebridge.backend.roster.model.RosterDto;
import com.carebridge.backend.user.model.User;
import org.springframework.stereotype.Component;

@Component
public class RosterMapper {

    public RosterDto toDto(Roster roster) {
        return new RosterDto(
            roster.getId(),
            roster.getPatient().getId(),
            roster.getNurse().getId(),
            roster.getStatus()
        );
    }

    public Roster toEntity(RosterDto dto, User nurse, User patient) {
        return new Roster(
            dto.id(),
            patient,
            nurse,
            dto.status()
        );
    }
}
