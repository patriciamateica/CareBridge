package com.carebridge.backend.carenotes;

import com.carebridge.backend.carenotes.model.CareNotes;
import com.carebridge.backend.carenotes.model.CareNotesDto;
import com.carebridge.backend.user.Role;
import com.carebridge.backend.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CareNotesMapperTest {

    private final com.carebridge.backend.carenotes.CareNotesMapper mapper = new CareNotesMapper();
    private User patientUser;
    private User nurseUser;

    @BeforeEach
    void setUp() {
        patientUser = new User();
        patientUser.setId(UUID.randomUUID());
        patientUser.setRole(Role.PATIENT);
        patientUser.setEmail("patient@test.com");

        nurseUser = new User();
        nurseUser.setId(UUID.randomUUID());
        nurseUser.setRole(Role.NURSE);
        nurseUser.setEmail("nurse@test.com");
    }

    @Test
    void toDto_ShouldMapAllFields() {
        UUID id = UUID.randomUUID();
        LocalDateTime timestamp = LocalDateTime.of(2026, 4, 20, 18, 30);
        CareNotes entity = new CareNotes(id, "note-content", patientUser, nurseUser, timestamp);

        CareNotesDto dto = mapper.toDto(entity);

        assertEquals(id, dto.id());
        assertEquals("note-content", dto.content());
        assertEquals(patientUser.getId(), dto.patientId());
        assertEquals(nurseUser.getId(), dto.nurseId());
        assertEquals(timestamp, dto.timestamp());
    }

    @Test
    void toEntity_ShouldMapAllFields() {
        UUID id = UUID.randomUUID();
        LocalDateTime timestamp = LocalDateTime.of(2026, 4, 20, 19, 0);
        CareNotesDto dto = new CareNotesDto(id, "dto-content", patientUser.getId(), nurseUser.getId(), timestamp);

        CareNotes entity = mapper.toEntity(dto, nurseUser, patientUser);

        assertEquals(id, entity.getId());
        assertEquals("dto-content", entity.getContent());
        assertEquals(patientUser.getId(), entity.getPatient().getId());
        assertEquals(nurseUser.getId(), entity.getNurse().getId());
        assertEquals(timestamp, entity.getTimestamp());
    }
}

