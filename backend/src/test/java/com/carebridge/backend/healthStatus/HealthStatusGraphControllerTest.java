package com.carebridge.backend.healthStatus;

import com.carebridge.backend.healthStatus.model.HealthStatus;
import com.carebridge.backend.healthStatus.model.Mood;
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
import java.util.List;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureGraphQlTester
class HealthStatusGraphControllerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockitoBean
    private HealthStatusService healthStatusService;

    @Test
    void getHealthStatuses_ShouldReturnStatuses() {
        HealthStatus status = new HealthStatus(UUID.randomUUID(), 5, Mood.Calm,
                Collections.singletonList("None"), "Notes", LocalDate.now(), UUID.randomUUID());
        when(healthStatusService.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(Collections.singletonList(status)));

        String query = """
                query {
                  getHealthStatuses(page: 0, size: 10) {
                    id
                    painScale
                    mood
                  }
                }
                """;

        graphQlTester.document(query)
                .execute()
                .errors()
                .verify()
                .path("getHealthStatuses")
                .entityList(HealthStatus.class)
                .hasSize(1);
    }

    @Test
    void createHealthStatus_ShouldReturnCreatedStatus() {
        UUID patientId = UUID.randomUUID();
        HealthStatus status = new HealthStatus(UUID.randomUUID(), 3, Mood.Calm,
                List.of("Headache"), "Note", LocalDate.parse("2023-10-27"), patientId);
        when(healthStatusService.create(any(HealthStatus.class))).thenReturn(status);

        String mutation = """
                mutation($pId: ID!, $pain: Int!, $mood: Mood!, $symptoms: [String!]!, $notes: String!, $time: String!) {
                  createHealthStatus(patientId: $pId, painScale: $pain, mood: $mood, symptoms: $symptoms, notes: $notes, timestamp: $time) {
                    id
                    painScale
                  }
                }
                """;

        graphQlTester.document(mutation)
                .variable("pId", patientId)
                .variable("pain", 3)
                .variable("mood", "Calm")
                .variable("symptoms", List.of("Headache"))
                .variable("notes", "Note")
                .variable("time", "2023-10-27")
                .execute()
                .errors()
                .verify()
                .path("createHealthStatus")
                .entity(HealthStatus.class)
                .matches(s -> s.getPainScale() == 3);
    }
}
