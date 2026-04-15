package com.carebridge.backend.healthStatus;

import com.carebridge.backend.healthStatus.model.HealthStatus;
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
class HealthStatusServiceTest {

    @Mock
    private com.carebridge.backend.healthStatus.HealthStatusRepository healthStatusRepository;

    @InjectMocks
    private HealthStatusService healthStatusService;

    private HealthStatus sampleStatus;
    private UUID statusId;

    @BeforeEach
    void setUp() {
        statusId = UUID.randomUUID();
        sampleStatus = new HealthStatus(
            statusId,
            3,
            null,
            List.of("Fatigue"),
            "Resting well",
            LocalDate.now(),
            UUID.randomUUID()
        );
    }

    @Test
    void findAll_ShouldReturnPageOfHealthStatus() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<HealthStatus> mockPage = new PageImpl<>(List.of(sampleStatus));
        when(healthStatusRepository.findAll(pageRequest)).thenReturn(mockPage);

        Page<HealthStatus> result = healthStatusService.findAll(pageRequest);

        assertEquals(1, result.getTotalElements());
        verify(healthStatusRepository, times(1)).findAll(pageRequest);
    }

    @Test
    void getById_ShouldReturnHealthStatusWhenExists() {
        when(healthStatusRepository.findById(statusId)).thenReturn(Optional.of(sampleStatus));

        HealthStatus result = healthStatusService.getById(statusId);

        assertNotNull(result);
        assertEquals(statusId, result.getId());
    }

    @Test
    void getById_ShouldThrowExceptionWhenNotExists() {
        when(healthStatusRepository.findById(statusId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> healthStatusService.getById(statusId));
    }

    @Test
    void create_ShouldSaveAndReturnHealthStatus() {
        when(healthStatusRepository.save(sampleStatus)).thenReturn(sampleStatus);

        HealthStatus result = healthStatusService.create(sampleStatus);

        assertNotNull(result);
        verify(healthStatusRepository, times(1)).save(sampleStatus);
    }

    @Test
    void update_ShouldUpdateAndReturnHealthStatusWhenExists() {
        HealthStatus updatedData = new HealthStatus(null, 8, null,
            List.of("Fever", "Chills"), "Getting worse", null, null);
        when(healthStatusRepository.findById(statusId)).thenReturn(Optional.of(sampleStatus));

        HealthStatus result = healthStatusService.update(statusId, updatedData);

        assertEquals(8, result.getPainScale());
        assertEquals("Getting worse", result.getNotes());
        assertTrue(result.getSymptoms().contains("Chills"));
        assertEquals(statusId, result.getId());
    }

    @Test
    void delete_ShouldRemoveHealthStatusWhenExists() {
        when(healthStatusRepository.findById(statusId)).thenReturn(Optional.of(sampleStatus));

        boolean result = healthStatusService.delete(statusId);

        assertTrue(result);
        verify(healthStatusRepository, times(1)).deleteById(statusId);
    }
}
