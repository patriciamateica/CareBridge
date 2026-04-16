package com.carebridge.backend.prescription;

import com.carebridge.backend.prescription.model.Prescription;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureGraphQlTester
class PrescriptionGraphControllerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockitoBean
    private PrescriptionService prescriptionService;

    @Test
    void getPrescriptions_ShouldReturnPrescriptions() {
        Prescription prescription = new Prescription(UUID.randomUUID(), "Aspirin", "100mg", "Daily", UUID.randomUUID(), UUID.randomUUID());
        when(prescriptionService.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(Collections.singletonList(prescription)));

        String query = """
                query {
                  getPrescriptions(page: 0, size: 10) {
                    id
                    name
                  }
                }
                """;

        graphQlTester.document(query)
                .execute()
                .errors()
                .verify()
                .path("getPrescriptions")
                .entityList(Prescription.class)
                .hasSize(1);
    }

    @Test
    void createPrescription_ShouldReturnCreatedPrescription() {
        UUID patientId = UUID.randomUUID();
        UUID nurseId = UUID.randomUUID();
        Prescription prescription = new Prescription(UUID.randomUUID(), "Tylenol", "500mg", "Every 6h", patientId, nurseId);
        when(prescriptionService.create(any(Prescription.class))).thenReturn(prescription);

        String mutation = """
                mutation($name: String!, $dose: String!, $timing: String!, $pId: ID!, $nId: ID!) {
                  createPrescription(name: $name, dose: $dose, timing: $timing, patientId: $pId, nurseId: $nId) {
                    id
                    name
                  }
                }
                """;

        graphQlTester.document(mutation)
                .variable("name", "Tylenol")
                .variable("dose", "500mg")
                .variable("timing", "Every 6h")
                .variable("pId", patientId)
                .variable("nId", nurseId)
                .execute()
                .errors()
                .verify()
                .path("createPrescription")
                .entity(Prescription.class)
                .matches(p -> p.getName().equals("Tylenol"));
    }

    @Test
    void deletePrescription_ShouldReturnTrue() {
        UUID id = UUID.randomUUID();
        when(prescriptionService.delete(eq(id))).thenReturn(true);

        String mutation = """
                mutation($id: ID!) {
                  deletePrescription(id: $id)
                }
                """;

        graphQlTester.document(mutation)
                .variable("id", id)
                .execute()
                .errors()
                .verify()
                .path("deletePrescription")
                .entity(Boolean.class)
                .isEqualTo(true);
    }
}
