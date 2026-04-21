package com.carebridge.backend.carenotes;

import com.carebridge.backend.carenotes.model.CareNotes;
import com.carebridge.backend.carenotes.model.CareNotesDto;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CareNotesMapperTest {

    private final CareNotesMapper mapper = new CareNotesMapper();

    @Test
    void toDto_ShouldMapAllFields() {
        UUID id = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID nurseId = UUID.randomUUID();
        LocalDateTime timestamp = LocalDateTime.of(2026, 4, 20, 18, 30);
        CareNotes entity = new CareNotes(id, "note-content", patientId, nurseId, timestamp);

        CareNotesDto dto = mapper.toDto(entity);

        assertEquals(id, dto.id());
        assertEquals("note-content", dto.content());
        assertEquals(patientId, dto.patientId());
        assertEquals(nurseId, dto.nurseId());
        assertEquals(timestamp, dto.timestamp());
    }

    @Test
    void toEntity_ShouldMapAllFields() {
        UUID id = UUID.randomUUID();
        UUID patientId = UUID.randomUUID();
        UUID nurseId = UUID.randomUUID();
        LocalDateTime timestamp = LocalDateTime.of(2026, 4, 20, 19, 0);
        CareNotesDto dto = new CareNotesDto(id, "dto-content", patientId, nurseId, timestamp);

        CareNotes entity = mapper.toEntity(dto);

        assertEquals(id, entity.getId());
        assertEquals("dto-content", entity.getContent());
        assertEquals(patientId, entity.getPatientId());
        assertEquals(nurseId, entity.getNurseId());
        assertEquals(timestamp, entity.getTimestamp());
    }
}

