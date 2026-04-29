package com.carebridge.backend.prescription;

import com.carebridge.backend.prescription.model.Prescription;
import com.carebridge.backend.user.Role;
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
class PrescriptionServiceTest {

    @Mock
    private com.carebridge.backend.prescription.PrescriptionRepository prescriptionRepository;

    @InjectMocks
    private PrescriptionService prescriptionService;

    private Prescription samplePrescription;
    private UUID prescriptionId;
    private User patientUser;
    private User nurseUser;

    @BeforeEach
    void setUp() {
        prescriptionId = UUID.randomUUID();

        patientUser = new User();
        patientUser.setId(UUID.randomUUID());
        patientUser.setRole(Role.PATIENT);
        patientUser.setEmail("patient@test.com");

        nurseUser = new User();
        nurseUser.setId(UUID.randomUUID());
        nurseUser.setRole(Role.NURSE);
        nurseUser.setEmail("nurse@test.com");

        samplePrescription = new Prescription(
            prescriptionId,
            "Ibuprofen",
            "400mg",
            "As needed for pain",
            patientUser,
            nurseUser
        );
    }

    @Test
    void findAll_ShouldReturnPageOfPrescriptions() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Prescription> mockPage = new PageImpl<>(List.of(samplePrescription));
        when(prescriptionRepository.findAll(pageRequest)).thenReturn(mockPage);

        Page<Prescription> result = prescriptionService.findAll(pageRequest);

        assertEquals(1, result.getTotalElements());
        verify(prescriptionRepository, times(1)).findAll(pageRequest);
    }

    @Test
    void getById_ShouldReturnPrescriptionWhenExists() {
        when(prescriptionRepository.findById(prescriptionId)).thenReturn(Optional.of(samplePrescription));

        Prescription result = prescriptionService.getById(prescriptionId);

        assertNotNull(result);
        assertEquals(prescriptionId, result.getId());
    }

    @Test
    void getById_ShouldThrowExceptionWhenNotExists() {
        when(prescriptionRepository.findById(prescriptionId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> prescriptionService.getById(prescriptionId));
    }

    @Test
    void create_ShouldSaveAndReturnPrescription() {
        when(prescriptionRepository.save(samplePrescription)).thenReturn(samplePrescription);

        Prescription result = prescriptionService.create(samplePrescription);

        assertNotNull(result);
        verify(prescriptionRepository, times(1)).save(samplePrescription);
    }

    @Test
    void update_ShouldUpdateAndReturnPrescriptionWhenExists() {
        Prescription updatedData = new Prescription(null, "Ibuprofen",
            "600mg", "Every 6 hours", patientUser, nurseUser);
        when(prescriptionRepository.findById(prescriptionId)).thenReturn(Optional.of(samplePrescription));
        when(prescriptionRepository.save(any(Prescription.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Prescription result = prescriptionService.update(prescriptionId, updatedData);

        assertEquals("600mg", result.getDose());
        assertEquals("Every 6 hours", result.getTiming());
        assertEquals(prescriptionId, result.getId());
    }

    @Test
    void delete_ShouldRemovePrescriptionWhenExists() {
        when(prescriptionRepository.findById(prescriptionId)).thenReturn(Optional.of(samplePrescription));

        boolean result = prescriptionService.delete(prescriptionId);

        assertTrue(result);
        verify(prescriptionRepository, times(1)).deleteById(prescriptionId);
    }
}
