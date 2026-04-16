package com.carebridge.backend.carenotes;

import com.carebridge.backend.carenotes.model.CareNotes;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureGraphQlTester
class CareNotesGraphControllerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockitoBean
    private CareNotesService careNotesService;

    @Test
    void getCareNotes_ShouldReturnCareNotes() {
        CareNotes note = new CareNotes(UUID.randomUUID(), "Content", UUID.randomUUID(), UUID.randomUUID(), LocalDateTime.now());
        when(careNotesService.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(Collections.singletonList(note)));

        String query = """
                query {
                  getCareNotes(page: 0, size: 10) {
                    id
                    content
                  }
                }
                """;

        graphQlTester.document(query)
                .execute()
                .errors()
                .verify()
                .path("getCareNotes")
                .entityList(CareNotes.class)
                .hasSize(1);
    }

    @Test
    void createCareNote_ShouldReturnCreatedNote() {
        UUID patientId = UUID.randomUUID();
        UUID nurseId = UUID.randomUUID();
        CareNotes note = new CareNotes(UUID.randomUUID(), "Content", patientId, nurseId, LocalDateTime.of(2023, 10, 27, 10, 0));
        when(careNotesService.create(any(CareNotes.class))).thenReturn(note);

        String mutation = """
                mutation($patientId: ID!, $nurseId: ID!, $content: String!, $timestamp: String!) {
                  createCareNote(patientId: $patientId, nurseId: $nurseId, content: $content, timestamp: $timestamp) {
                    id
                    content
                  }
                }
                """;

        graphQlTester.document(mutation)
                .variable("patientId", patientId)
                .variable("nurseId", nurseId)
                .variable("content", "Content")
                .variable("timestamp", "2023-10-27T10:00:00")
                .execute()
                .errors()
                .verify()
                .path("createCareNote")
                .entity(CareNotes.class)
                .matches(n -> n.getContent().equals("Content"));
    }
}
