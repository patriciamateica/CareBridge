package com.carebridge.backend.carenotes;

import com.carebridge.backend.carenotes.model.CareNotes;
import com.carebridge.backend.carenotes.model.CareNotesDto;
import com.carebridge.backend.user.model.User;
import org.springframework.stereotype.Component;

@Component
public class CareNotesMapper {

    public CareNotesDto toDto(CareNotes notes) {
        return new CareNotesDto(
            notes.getId(),
            notes.getContent(),
            notes.getPatient().getId(),
            notes.getNurse().getId(),
            notes.getTimestamp()
        );
    }

    public CareNotes toEntity(CareNotesDto dto, User nurse, User patient) {
        return new CareNotes(
            dto.id(),
            dto.content(),
            patient,
            nurse,
            dto.timestamp()
        );
    }
}
