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
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PatientDetailsServiceTest {

    @Mock
    private com.carebridge.backend.patientDetails.PatientDetailsRepository repository;

    @Mock
    private com.carebridge.backend.user.UserRepository userRepository;

    @InjectMocks
    private PatientDetailsService service;

    private PatientDetails sampleDetails;
    private UUID detailsId;
    private UUID userId;
    private User testUser;

    @BeforeEach
    void setUp() {
        detailsId = UUID.randomUUID();
        userId = UUID.randomUUID();
        testUser = new User();
        testUser.setId(userId);
        testUser.setEmail("patient@test.com");

        sampleDetails = new PatientDetails(
            testUser,
            "Diabetes Type 2",
            List.of("A1C Test"),
            List.of(),
            "Son: 0744111222",
            UUID.randomUUID(),
            "ACTIVE"
        );
        sampleDetails.setId(detailsId);
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
        PatientDetails newDetails = new PatientDetails(testUser, "Diabetes Type 2", List.of("A1C Test"), List.of(),
            "Son: 0744111222", UUID.randomUUID(), "ACTIVE");
        when(repository.findByUserId(userId)).thenReturn(Optional.empty());
        when(repository.saveAndFlush(any(PatientDetails.class))).thenReturn(sampleDetails);

        PatientDetails result = service.create(newDetails);

        assertNotNull(result);
        assertEquals(detailsId, result.getId());
        verify(repository, times(1)).saveAndFlush(any(PatientDetails.class));
    }

    @Test
    void update_ShouldUpdateAndReturnPatientDetailsWhenExists() {
        User updatedUser = new User();
        updatedUser.setId(userId);
        updatedUser.setEmail("patient@test.com");

        PatientDetails updatedData = new PatientDetails(updatedUser, "Updated Diagnosis", List.of("MRI"),
            List.of("/scans/mri.png"), "Updated Contact", UUID.randomUUID(), "ACTIVE");
        when(repository.findById(detailsId)).thenReturn(Optional.of(sampleDetails));
        when(repository.save(any(PatientDetails.class))).thenAnswer(invocation -> {
            PatientDetails arg = invocation.getArgument(0);
            arg.setId(detailsId);
            return arg;
        });

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
        verify(repository, times(1)).delete(sampleDetails);
    }
}
