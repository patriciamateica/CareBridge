package com.carebridge.backend.carenotes;

import com.carebridge.backend.carenotes.model.CareNotes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CareNotesServiceTest {

    @Mock
    private CareNotesRepository repository;

    @InjectMocks
    private CareNotesService service;

    private CareNotes sampleNotes;
    private UUID notesId;

    @BeforeEach
    void setUp() {
        notesId = UUID.randomUUID();
        sampleNotes = new CareNotes(
            notesId,
            "Patient had trouble sleeping.",
            UUID.randomUUID(),
            UUID.randomUUID(),
            LocalDateTime.now()
        );
    }

    @Test
    void findAll_ShouldReturnPageOfCareNotes() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<CareNotes> mockPage = new PageImpl<>(List.of(sampleNotes));
        when(repository.findAll(pageRequest)).thenReturn(mockPage);

        Page<CareNotes> result = service.findAll(pageRequest);

        assertEquals(1, result.getTotalElements());
        verify(repository, times(1)).findAll(pageRequest);
    }

    @Test
    void getById_ShouldReturnCareNotesWhenExists() {
        when(repository.findById(notesId)).thenReturn(Optional.of(sampleNotes));

        CareNotes result = service.getById(notesId);

        assertNotNull(result);
        assertEquals(notesId, result.getId());
    }

    @Test
    void getById_ShouldThrowExceptionWhenNotExists() {
        when(repository.findById(notesId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.getById(notesId));
    }

    @Test
    void create_ShouldSaveAndReturnCareNotes() {
        when(repository.save(sampleNotes)).thenReturn(sampleNotes);

        CareNotes result = service.create(sampleNotes);

        assertNotNull(result);
        verify(repository, times(1)).save(sampleNotes);
    }

    @Test
    void update_ShouldUpdateAndReturnCareNotesWhenExists() {
        CareNotes updatedData = new CareNotes(null, "Patient slept well after medication.", UUID.randomUUID(), UUID.randomUUID(), LocalDateTime.now());
        when(repository.findById(notesId)).thenReturn(Optional.of(sampleNotes));

        CareNotes result = service.update(notesId, updatedData);

        assertEquals("Patient slept well after medication.", result.getContent());
        assertEquals(notesId, result.getId());
    }

    @Test
    void delete_ShouldRemoveCareNotesWhenExists() {
        when(repository.findById(notesId)).thenReturn(Optional.of(sampleNotes));

        boolean result = service.delete(notesId);

        assertTrue(result);
        verify(repository, times(1)).deleteById(notesId);
    }
}
