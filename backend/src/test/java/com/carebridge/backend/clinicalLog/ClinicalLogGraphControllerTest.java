package com.carebridge.backend.clinicalLog;

import com.carebridge.backend.clinicalLog.model.ClinicalLog;
import com.carebridge.backend.clinicalLog.model.ClinicalLogStatus;
import com.carebridge.backend.clinicalLog.model.DocumentType;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureGraphQlTester
class ClinicalLogGraphControllerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockitoBean
    private ClinicalLogService clinicalLogService;

    @Test
    void getClinicalLogs_ShouldReturnLogs() {
        ClinicalLog log = new ClinicalLog(UUID.randomUUID(), "Title", DocumentType.X_RAY,
                LocalDate.now(), "url", UUID.randomUUID(), UUID.randomUUID(),
                LocalDateTime.now(), ClinicalLogStatus.ACTIVE);
        when(clinicalLogService.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(Collections.singletonList(log)));

        String query = """
                query {
                  getClinicalLogs(page: 0, size: 10) {
                    id
                    documentTitle
                  }
                }
                """;

        graphQlTester.document(query)
                .execute()
                .errors()
                .verify()
                .path("getClinicalLogs")
                .entityList(ClinicalLog.class)
                .hasSize(1);
    }

    @Test
    void createClinicalLog_ShouldReturnCreatedLog() {
        UUID patientId = UUID.randomUUID();
        UUID nurseId = UUID.randomUUID();
        ClinicalLog log = new ClinicalLog(UUID.randomUUID(), "Title", DocumentType.X_RAY,
                LocalDate.parse("2023-10-27"), "url", patientId, nurseId,
                LocalDateTime.now(), ClinicalLogStatus.ACTIVE);
        when(clinicalLogService.create(any(ClinicalLog.class))).thenReturn(log);

        String mutation = """
                mutation($title: String!, $type: DocumentType!, $date: String!, $url: String!, $pId: ID!, $nId: ID!) {
                  createClinicalLog(documentTitle: $title, documentType: $type, datePerformed: $date, fileUrl: $url, patientId: $pId, nurseId: $nId) {
                    id
                    documentTitle
                  }
                }
                """;

        graphQlTester.document(mutation)
                .variable("title", "Title")
                .variable("type", "X_RAY")
                .variable("date", "2023-10-27")
                .variable("url", "url")
                .variable("pId", patientId)
                .variable("nId", nurseId)
                .execute()
                .errors()
                .verify()
                .path("createClinicalLog")
                .entity(ClinicalLog.class)
                .matches(l -> l.getDocumentTitle().equals("Title"));
    }
}
