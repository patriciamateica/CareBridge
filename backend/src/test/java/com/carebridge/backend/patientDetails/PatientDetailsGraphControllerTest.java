package com.carebridge.backend.patientDetails;

import com.carebridge.backend.patientDetails.model.PatientDetails;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureGraphQlTester
class PatientDetailsGraphControllerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockitoBean
    private PatientDetailsService patientDetailsService;

    @Test
    void getAllPatientDetails_ShouldReturnDetails() {
        PatientDetails details = new PatientDetails(UUID.randomUUID(), UUID.randomUUID(), "Flu", Collections.singletonList("Fever"), Collections.emptyList(), "911", UUID.randomUUID());
        when(patientDetailsService.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(Collections.singletonList(details)));

        String query = """
                query {
                  getAllPatientDetails(page: 0, size: 10) {
                    id
                    primaryDiagnosis
                  }
                }
                """;

        graphQlTester.document(query)
                .execute()
                .errors()
                .verify()
                .path("getAllPatientDetails")
                .entityList(PatientDetails.class)
                .hasSize(1);
    }

    @Test
    void createPatientDetails_ShouldReturnCreatedDetails() {
        UUID userId = UUID.randomUUID();
        UUID nurseId = UUID.randomUUID();
        PatientDetails details = new PatientDetails(UUID.randomUUID(), userId, "Cold", List.of("Cough"), List.of("X-Ray"), "000", nurseId);
        when(patientDetailsService.create(any(PatientDetails.class))).thenReturn(details);

        String mutation = """
                mutation($uId: ID!, $diag: String!, $diags: [String!]!, $scans: [String!]!, $contact: String!, $nId: ID!) {
                  createPatientDetails(userId: $uId, primaryDiagnosis: $diag, diagnostics: $diags, scans: $scans, emergencyContact: $contact, assignedNurseId: $nId) {
                    id
                    primaryDiagnosis
                  }
                }
                """;

        graphQlTester.document(mutation)
                .variable("uId", userId)
                .variable("diag", "Cold")
                .variable("diags", List.of("Cough"))
                .variable("scans", List.of("X-Ray"))
                .variable("contact", "000")
                .variable("nId", nurseId)
                .execute()
                .errors()
                .verify()
                .path("createPatientDetails")
                .entity(PatientDetails.class)
                .matches(d -> d.getPrimaryDiagnosis().equals("Cold"));
    }

    @Test
    void updatePatientDiagnosis_ShouldReturnUpdatedDetails() {
        UUID id = UUID.randomUUID();
        PatientDetails details = new PatientDetails(id, UUID.randomUUID(), "Recovered", Collections.emptyList(), Collections.emptyList(), "911", UUID.randomUUID());
        when(patientDetailsService.updateDiagnosis(eq(id), eq("Recovered"))).thenReturn(details);

        String mutation = """
                mutation($id: ID!, $diag: String!) {
                  updatePatientDiagnosis(id: $id, primaryDiagnosis: $diag) {
                    id
                    primaryDiagnosis
                  }
                }
                """;

        graphQlTester.document(mutation)
                .variable("id", id)
                .variable("diag", "Recovered")
                .execute()
                .errors()
                .verify()
                .path("updatePatientDiagnosis")
                .entity(PatientDetails.class)
                .matches(d -> d.getPrimaryDiagnosis().equals("Recovered"));
    }
}
