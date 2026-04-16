package com.carebridge.backend.clinicalLog;

import com.carebridge.backend.clinicalLog.model.ClinicalLog;
import com.carebridge.backend.clinicalLog.model.ClinicalLogStatus;
import com.carebridge.backend.clinicalLog.model.DocumentType;
import org.springframework.data.domain.PageRequest;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Controller
public class ClinicalLogGraphController {

    private final ClinicalLogService clinicalLogService;

    public ClinicalLogGraphController(ClinicalLogService clinicalLogService) {
        this.clinicalLogService = clinicalLogService;
    }

    @QueryMapping
    public List<ClinicalLog> getClinicalLogs(@Argument int page, @Argument int size) {
        return clinicalLogService.findAll(PageRequest.of(page, size)).getContent();
    }

    @QueryMapping
    public ClinicalLog getClinicalLogById(@Argument UUID id) {
        return clinicalLogService.getById(id);
    }

    @QueryMapping
    public List<ClinicalLog> getClinicalLogsByPatientId(@Argument UUID patientId) {
        return clinicalLogService.getByPatientId(patientId);
    }

    @MutationMapping
    public ClinicalLog createClinicalLog(
            @Argument String documentTitle, @Argument DocumentType documentType,
            @Argument String datePerformed, @Argument String fileUrl,
            @Argument UUID patientId, @Argument UUID nurseId) {

        ClinicalLog clinicalLog = new ClinicalLog(
                UUID.randomUUID(), documentTitle, documentType,
                LocalDate.parse(datePerformed), fileUrl, patientId, nurseId,
                LocalDateTime.now(), ClinicalLogStatus.ACTIVE);
        return clinicalLogService.create(clinicalLog);
    }

    @MutationMapping
    public Boolean deleteClinicalLog(@Argument UUID id) {
        return clinicalLogService.delete(id);
    }

    @SubscriptionMapping
    public Flux<ClinicalLog> onClinicalLogAdded(@Argument UUID patientId) {
        return clinicalLogService.getLogStream(patientId);
    }
}
