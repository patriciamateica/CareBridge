package com.carebridge.backend.vitals;

import com.carebridge.backend.vitals.model.Vitals;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDate;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureGraphQlTester
class VitalsGraphControllerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockitoBean
    private VitalsService vitalsService;

    @Test
    void getVitals_ShouldReturnVitals() {
        Vitals vitals = new Vitals(UUID.randomUUID(), LocalDate.now(), 70, 120, 16, 98, UUID.randomUUID());
        when(vitalsService.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(Collections.singletonList(vitals)));

        String query = """
                query {
                  getVitals(page: 0, size: 10) {
                    id
                    heartRate
                  }
                }
                """;

        graphQlTester.document(query)
                .execute()
                .errors()
                .verify()
                .path("getVitals")
                .entityList(Vitals.class)
                .hasSize(1);
    }

    @Test
    void createVitals_ShouldReturnCreatedVitals() {
        UUID patientId = UUID.randomUUID();
        Vitals vitals = new Vitals(UUID.randomUUID(), LocalDate.of(2023, 10, 27), 70, 120, 16, 98, patientId);
        when(vitalsService.create(any(Vitals.class))).thenReturn(vitals);

        String mutation = """
                mutation($patientId: ID!, $heartRate: Int!, $bloodPressure: Int!, $respiratoryRate: Int!, $spO2: Int!, $timestamp: String!) {
                  createVitals(patientId: $patientId, heartRate: $heartRate, bloodPressure: $bloodPressure, respiratoryRate: $respiratoryRate, spO2: $spO2, timestamp: $timestamp) {
                    id
                    heartRate
                  }
                }
                """;

        graphQlTester.document(mutation)
                .variable("patientId", patientId)
                .variable("heartRate", 70)
                .variable("bloodPressure", 120)
                .variable("respiratoryRate", 16)
                .variable("spO2", 98)
                .variable("timestamp", "2023-10-27")
                .execute()
                .errors()
                .verify()
                .path("createVitals")
                .entity(Vitals.class)
                .matches(v -> v.getHeartRate() == 70);
    }
}
