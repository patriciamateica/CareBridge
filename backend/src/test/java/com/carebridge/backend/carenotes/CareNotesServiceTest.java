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
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

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
        when(repository.save(any(CareNotes.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CareNotes result = service.update(notesId, updatedData);

        assertEquals("Patient slept well after medication.", result.getContent());
        assertEquals(notesId, result.getId());
        verify(repository, times(1)).save(any(CareNotes.class));
    }

    @Test
    void delete_ShouldRemoveCareNotesWhenExists() {
        when(repository.findById(notesId)).thenReturn(Optional.of(sampleNotes));

        boolean result = service.delete(notesId);

        assertTrue(result);
        verify(repository, times(1)).deleteById(notesId);
    }

    @Test
    void findByPatientId_Pageable_ShouldDelegateToRepository() {
        UUID patientId = UUID.randomUUID();
        PageRequest pageRequest = PageRequest.of(0, 5);
        Page<CareNotes> mockPage = new PageImpl<>(List.of(sampleNotes));
        when(repository.findByPatientId(patientId, pageRequest)).thenReturn(mockPage);

        Page<CareNotes> result = service.findByPatientId(patientId, pageRequest);

        assertEquals(1, result.getTotalElements());
        verify(repository, times(1)).findByPatientId(patientId, pageRequest);
    }

    @Test
    void getByPatientId_ShouldDelegateToRepository() {
        UUID patientId = UUID.randomUUID();
        when(repository.findByPatientId(patientId)).thenReturn(List.of(sampleNotes));

        List<CareNotes> result = service.getByPatientId(patientId);

        assertEquals(1, result.size());
        verify(repository, times(1)).findByPatientId(patientId);
    }

    @Test
    void countByPatientId_ShouldDelegateToRepository() {
        UUID patientId = UUID.randomUUID();
        when(repository.countByPatientId(patientId)).thenReturn(3L);

        long count = service.countByPatientId(patientId);

        assertEquals(3L, count);
        verify(repository, times(1)).countByPatientId(patientId);
    }

    @Test
    void deleteAll_ShouldDelegateToRepository() {
        service.deleteAll();

        verify(repository, times(1)).deleteAll();
    }

    @Test
    void create_ShouldNotEmitWhenRepositoryReturnsNull() {
        when(repository.save(sampleNotes)).thenReturn(null);

        CareNotes result = service.create(sampleNotes);

        assertNull(result);
        verify(repository, times(1)).save(sampleNotes);
    }

    @Test
    void getNoteStream_ShouldEmitOnlyMatchingPatientNotes() throws InterruptedException {
        UUID watchedPatientId = UUID.randomUUID();
        UUID otherPatientId = UUID.randomUUID();
        UUID nurseId = UUID.randomUUID();
        when(repository.save(any(CareNotes.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<CareNotes> received = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        service.getNoteStream(watchedPatientId)
            .subscribe(note -> {
                received.add(note);
                latch.countDown();
            });

        service.create(new CareNotes(UUID.randomUUID(), "other", otherPatientId, nurseId, LocalDateTime.now()));
        CareNotes matching = new CareNotes(UUID.randomUUID(), "matching", watchedPatientId, nurseId, LocalDateTime.now());
        service.create(matching);

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertEquals(1, received.size());
        assertEquals(matching.getPatientId(), received.getFirst().getPatientId());
    }

    @Test
    void getNoteStream_WhenPatientIdNull_ShouldEmitAnyPatientNote() throws InterruptedException {
        UUID patientId = UUID.randomUUID();
        UUID nurseId = UUID.randomUUID();
        when(repository.save(any(CareNotes.class))).thenAnswer(invocation -> invocation.getArgument(0));

        List<CareNotes> received = new ArrayList<>();
        CountDownLatch latch = new CountDownLatch(1);
        service.getNoteStream(null)
            .subscribe(note -> {
                received.add(note);
                latch.countDown();
            });

        CareNotes created = new CareNotes(UUID.randomUUID(), "broadcast", patientId, nurseId, LocalDateTime.now());
        service.create(created);

        assertTrue(latch.await(2, TimeUnit.SECONDS));
        assertEquals(1, received.size());
        assertEquals(created.getId(), received.getFirst().getId());
    }
}
