package com.carebridge.backend.nursedetails;

import com.carebridge.backend.nursedetails.model.NurseDetails;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class NurseDetailsServiceTest {

    @Mock
    private NurseDetailsRepository repository;

    @InjectMocks
    private NurseDetailsService service;

    private NurseDetails sampleDetails;
    private UUID detailsId;

    @BeforeEach
    void setUp() {
        detailsId = UUID.randomUUID();
        sampleDetails = new NurseDetails(
            detailsId,
            UUID.randomUUID(),
            "Cardiology",
            "Heart Care Center",
            10,
            true
        );
    }

    @Test
    void findAll_ShouldReturnPageOfNurseDetails() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<NurseDetails> mockPage = new PageImpl<>(List.of(sampleDetails));
        when(repository.findAll(pageRequest)).thenReturn(mockPage);

        Page<NurseDetails> result = service.findAll(pageRequest);

        assertEquals(1, result.getTotalElements());
        verify(repository, times(1)).findAll(pageRequest);
    }

    @Test
    void getById_ShouldReturnNurseDetailsWhenExists() {
        when(repository.findById(detailsId)).thenReturn(Optional.of(sampleDetails));

        NurseDetails result = service.getById(detailsId);

        assertNotNull(result);
        assertEquals(detailsId, result.getId());
    }

    @Test
    void getById_ShouldThrowExceptionWhenNotExists() {
        when(repository.findById(detailsId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.getById(detailsId));
    }

    @Test
    void create_ShouldSaveAndReturnNurseDetails() {
        when(repository.save(sampleDetails)).thenReturn(sampleDetails);

        NurseDetails result = service.create(sampleDetails);

        assertNotNull(result);
        verify(repository, times(1)).save(sampleDetails);
    }

    @Test
    void update_ShouldUpdateAndReturnNurseDetailsWhenExists() {
        NurseDetails updatedData = new NurseDetails(null, UUID.randomUUID(), "Neurology", "Brain Institute", 12, false);
        when(repository.findById(detailsId)).thenReturn(Optional.of(sampleDetails));

        NurseDetails result = service.update(detailsId, updatedData);

        assertEquals("Neurology", result.getSpecialization());
        assertFalse(result.isHireMeStatus());
        assertEquals(12, result.getExperienceYears());
        assertEquals(detailsId, result.getId());
    }

    @Test
    void delete_ShouldRemoveNurseDetailsWhenExists() {
        when(repository.findById(detailsId)).thenReturn(Optional.of(sampleDetails));

        boolean result = service.delete(detailsId);

        assertTrue(result);
        verify(repository, times(1)).deleteById(detailsId);
    }
}
