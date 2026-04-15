package com.carebridge.backend.clinicalLog;

import com.carebridge.backend.clinicalLog.model.ClinicalLog;
import com.carebridge.backend.clinicalLog.model.ClinicalLogStatus;
import com.carebridge.backend.clinicalLog.model.DocumentType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class ClinicalLogRepositoryTest {

    private ClinicalLogRepository repository;
    private ClinicalLog sampleLog;

    @BeforeEach
    void setUp() {
        repository = new ClinicalLogRepository();
        sampleLog = new ClinicalLog(
            null,
            "Annual Blood Work",
            DocumentType.BLOOD_WORK,
            LocalDate.now(),
            "https://storage.carebridge.com/blood_work_123.pdf",
            UUID.randomUUID(),
            UUID.randomUUID(),
            LocalDateTime.now(),
            ClinicalLogStatus.ACTIVE
        );
    }

    @Test
    void save_ShouldGenerateIdAndStoreLog() {
        ClinicalLog saved = repository.save(sampleLog);

        assertNotNull(saved.getId());
        assertEquals("Annual Blood Work", saved.getDocumentTitle());
        assertTrue(repository.findById(saved.getId()).isPresent());
    }

    @Test
    void findById_ShouldReturnLogWhenExists() {
        ClinicalLog saved = repository.save(sampleLog);

        Optional<ClinicalLog> found = repository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    void findById_ShouldReturnEmptyWhenNotExists() {
        Optional<ClinicalLog> found = repository.findById(UUID.randomUUID());

        assertTrue(found.isEmpty());
    }

    @Test
    void findAll_ShouldReturnCorrectPage() {
        for (int i = 0; i < 5; i++) {
            repository.save(new ClinicalLog(null, "Log " + i, DocumentType.MRI, LocalDate.now(), "url", UUID.randomUUID(), UUID.randomUUID(), LocalDateTime.now(), ClinicalLogStatus.ACTIVE));
        }

        Page<ClinicalLog> page = repository.findAll(PageRequest.of(0, 2));

        assertEquals(2, page.getContent().size());
        assertEquals(5, page.getTotalElements());
        assertEquals(3, page.getTotalPages());
    }

    @Test
    void deleteById_ShouldRemoveLogFromMap() {
        ClinicalLog saved = repository.save(sampleLog);

        repository.deleteById(saved.getId());

        assertTrue(repository.findById(saved.getId()).isEmpty());
    }
}
