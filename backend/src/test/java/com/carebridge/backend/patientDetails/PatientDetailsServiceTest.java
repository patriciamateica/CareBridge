package com.carebridge.backend.patientDetails;

import com.carebridge.backend.patientDetails.model.PatientDetails;
import com.carebridge.backend.user.model.User;
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
class PatientDetailsServiceTest {

    @Mock
    private com.carebridge.backend.patientDetails.PatientDetailsRepository repository;

    @InjectMocks
    private PatientDetailsService service;

    private PatientDetails sampleDetails;
    private UUID detailsId;
    private User testUser;

    @BeforeEach
    void setUp() {
        detailsId = UUID.randomUUID();
        testUser = new User();
        testUser.setId(detailsId);
        testUser.setEmail("patient@test.com");

        sampleDetails = new PatientDetails(
            testUser,
            "Diabetes Type 2",
            List.of("A1C Test"),
            List.of(),
            "Son: 0744111222",
            UUID.randomUUID()
        );
    }

    @Test
    void findAll_ShouldReturnPageOfPatientDetails() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<PatientDetails> mockPage = new PageImpl<>(List.of(sampleDetails));
        when(repository.findAll(pageRequest)).thenReturn(mockPage);

        Page<PatientDetails> result = service.findAll(pageRequest);

        assertEquals(1, result.getTotalElements());
        verify(repository, times(1)).findAll(pageRequest);
    }

    @Test
    void getById_ShouldReturnPatientDetailsWhenExists() {
        when(repository.findById(detailsId)).thenReturn(Optional.of(sampleDetails));

        PatientDetails result = service.getById(detailsId);

        assertNotNull(result);
        assertEquals(detailsId, result.getId());
    }

    @Test
    void getById_ShouldThrowExceptionWhenNotExists() {
        when(repository.findById(detailsId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> service.getById(detailsId));
    }

    @Test
    void create_ShouldSaveAndReturnPatientDetails() {
        when(repository.save(sampleDetails)).thenReturn(sampleDetails);

        PatientDetails result = service.create(sampleDetails);

        assertNotNull(result);
        verify(repository, times(1)).save(sampleDetails);
    }

    @Test
    void update_ShouldUpdateAndReturnPatientDetailsWhenExists() {
        User updatedUser = new User();
        updatedUser.setId(detailsId);
        updatedUser.setEmail("patient@test.com");

        PatientDetails updatedData = new PatientDetails(updatedUser, "Updated Diagnosis", List.of("MRI"), List.of("/scans/mri.png"), "Updated Contact", UUID.randomUUID());
        when(repository.findById(detailsId)).thenReturn(Optional.of(sampleDetails));
        when(repository.save(any(PatientDetails.class))).thenAnswer(invocation -> invocation.getArgument(0));

        PatientDetails result = service.update(detailsId, updatedData);

        assertEquals("Updated Diagnosis", result.getPrimaryDiagnosis());
        assertTrue(result.getScans().contains("/scans/mri.png"));
        assertEquals(detailsId, result.getId());
    }

    @Test
    void delete_ShouldRemovePatientDetailsWhenExists() {
        when(repository.findById(detailsId)).thenReturn(Optional.of(sampleDetails));

        boolean result = service.delete(detailsId);

        assertTrue(result);
        verify(repository, times(1)).deleteById(detailsId);
    }
}
