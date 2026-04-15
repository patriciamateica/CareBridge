package com.carebridge.backend.carenotes;

import com.carebridge.backend.carenotes.model.CareNotes;
import com.carebridge.backend.carenotes.model.CareNotesDto;
import org.springframework.stereotype.Component;

@Component
public class CareNotesMapper {

    public CareNotesDto toDto(CareNotes notes) {
        return new CareNotesDto(
            notes.getId(),
            notes.getContent(),
            notes.getPatientId(),
            notes.getNurseId(),
            notes.getTimestamp()
        );
    }

    public CareNotes toEntity(CareNotesDto dto) {
        return new CareNotes(
            dto.id(),
            dto.content(),
            dto.patientId(),
            dto.nurseId(),
            dto.timestamp()
        );
    }
}
