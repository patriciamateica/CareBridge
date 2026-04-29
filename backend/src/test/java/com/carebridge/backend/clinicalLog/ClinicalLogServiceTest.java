package com.carebridge.backend.clinicalLog;

import com.carebridge.backend.clinicalLog.model.ClinicalLog;
import com.carebridge.backend.clinicalLog.model.ClinicalLogStatus;
import com.carebridge.backend.clinicalLog.model.DocumentType;
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

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ClinicalLogServiceTest {

    @Mock
    private ClinicalLogRepository repository;

    @InjectMocks
    private ClinicalLogService service;

    private ClinicalLog sampleLog;
    private UUID logId;
    private User patientUser;
    private User nurseUser;

    @BeforeEach
    void setUp() {
        logId = UUID.randomUUID();

        patientUser = new User();
        patientUser.setId(UUID.randomUUID());
        patientUser.setRole(Role.PATIENT);
        patientUser.setEmail("patient@test.com");

        nurseUser = new User();
        nurseUser.setId(UUID.randomUUID());
        nurseUser.setRole(Role.NURSE);
        nurseUser.setEmail("nurse@test.com");

        sampleLog = new ClinicalLog(
            logId,
            "Head CT Scan",
            DocumentType.CT_SCAN,
            LocalDate.now(),
            "https://storage.carebridge.com/ct.pdf",
            patientUser,
            nurseUser,
            LocalDateTime.now(),
            ClinicalLogStatus.ACTIVE
        );
    }

    @Test
    void findAll_ShouldReturnPageOfLogs() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<ClinicalLog> mockPage = new PageImpl<>(List.of(sampleLog));
        when(repository.findAll(pageRequest)).thenReturn(mockPage);

        Page<ClinicalLog> result = service.findAll(pageRequest);

        assertEquals(1, result.getTotalElements());
        verify(repository, times(1)).findAll(pageRequest);
    }

    @Test
    void getById_ShouldReturnLogWhenExists() {
        when(repository.findById(logId)).thenReturn(Optional.of(sampleLog));

        ClinicalLog result = service.getById(logId);

        assertNotNull(result);
        assertEquals(logId, result.getId());
    }

    @Test
    void create_ShouldSetStatusActiveAndSave() {
        ClinicalLog newLog = new ClinicalLog(null, "Test", DocumentType.MRI, LocalDate.now(), "url", patientUser, nurseUser, LocalDateTime.now(), null);
        when(repository.save(newLog)).thenReturn(newLog);

        ClinicalLog result = service.create(newLog);

        assertEquals(ClinicalLogStatus.ACTIVE, result.getStatus());
        verify(repository, times(1)).save(newLog);
    }

    @Test
    void delete_ShouldPerformSoftDelete() {
        when(repository.findById(logId)).thenReturn(Optional.of(sampleLog));
        when(repository.save(any(ClinicalLog.class))).thenAnswer(invocation -> invocation.getArgument(0));

        boolean result = service.delete(logId);

        assertTrue(result);
        assertEquals(ClinicalLogStatus.DELETED, sampleLog.getStatus()); // Verify soft delete
        verify(repository, never()).deleteById(logId); // Ensure hard delete is NOT called
    }
}
