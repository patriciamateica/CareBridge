package com.carebridge.backend.vitals;

import com.carebridge.backend.vitals.model.Vitals;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class VitalsServiceTest {

    @Mock
    private com.carebridge.backend.vitals.VitalsRepository vitalsRepository;

    @InjectMocks
    private VitalsService vitalsService;

    private Vitals sampleVitals;
    private UUID vitalsId;

    @BeforeEach
    void setUp() {
        vitalsId = UUID.randomUUID();
        sampleVitals = new Vitals(vitalsId, LocalDate.now(), 80, 120,
            16, 98, UUID.randomUUID());
    }

    @Test
    void findAll_ShouldReturnPageOfVitals() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Vitals> mockPage = new PageImpl<>(List.of(sampleVitals));
        when(vitalsRepository.findAll(pageRequest)).thenReturn(mockPage);

        Page<Vitals> result = vitalsService.findAll(pageRequest);

        assertEquals(1, result.getTotalElements());
        verify(vitalsRepository, times(1)).findAll(pageRequest);
    }

    @Test
    void getById_ShouldReturnVitalsWhenExists() {
        when(vitalsRepository.findById(vitalsId)).thenReturn(Optional.of(sampleVitals));

        Vitals result = vitalsService.getById(vitalsId);

        assertNotNull(result);
        assertEquals(vitalsId, result.getId());
    }

    @Test
    void getById_ShouldThrowExceptionWhenNotExists() {
        when(vitalsRepository.findById(vitalsId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> vitalsService.getById(vitalsId));
    }

    @Test
    void create_ShouldSaveAndReturnVitals() {
        when(vitalsRepository.save(sampleVitals)).thenReturn(sampleVitals);

        Vitals result = vitalsService.create(sampleVitals);

        assertNotNull(result);
        verify(vitalsRepository, times(1)).save(sampleVitals);
    }

    @Test
    void update_ShouldUpdateAndReturnVitalsWhenExists() {
        Vitals updatedData = new Vitals(null, null, 90, 130,
            18, 99, null);
        when(vitalsRepository.findById(vitalsId)).thenReturn(Optional.of(sampleVitals));
        when(vitalsRepository.save(any(Vitals.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Vitals result = vitalsService.update(vitalsId, updatedData);

        assertNotNull(result);
        assertEquals(90, result.getHeartRate());
        assertEquals(130, result.getBloodPressure());
        assertEquals(18, result.getRespiratoryRate());
        assertEquals(99, result.getSpO2());
        assertEquals(vitalsId, result.getId());
    }

    @Test
    void delete_ShouldRemoveVitalsWhenExists() {
        when(vitalsRepository.findById(vitalsId)).thenReturn(Optional.of(sampleVitals));

        boolean result = vitalsService.delete(vitalsId);

        assertTrue(result);
        verify(vitalsRepository, times(1)).deleteById(vitalsId);
    }
}
