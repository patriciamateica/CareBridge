package com.carebridge.backend.nursedetails;

import com.carebridge.backend.nursedetails.model.NurseDetails;
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
class NurseDetailsGraphControllerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockitoBean
    private NurseDetailsService nurseDetailsService;

    @Test
    void getAllNurseDetails_ShouldReturnDetails() {
        NurseDetails details = new NurseDetails(UUID.randomUUID(), UUID.randomUUID(), "ICU", "City Hospital", 5, true);
        when(nurseDetailsService.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(Collections.singletonList(details)));

        String query = """
                query {
                  getAllNurseDetails(page: 0, size: 10) {
                    id
                    specialization
                  }
                }
                """;

        graphQlTester.document(query)
                .execute()
                .errors()
                .verify()
                .path("getAllNurseDetails")
                .entityList(NurseDetails.class)
                .hasSize(1);
    }

    @Test
    void createNurseDetails_ShouldReturnCreatedDetails() {
        UUID userId = UUID.randomUUID();
        NurseDetails details = new NurseDetails(UUID.randomUUID(), userId, "ER", "General Hospital", 3, false);
        when(nurseDetailsService.create(any(NurseDetails.class))).thenReturn(details);

        String mutation = """
                mutation($uId: ID!, $spec: String!, $hosp: String!, $exp: Int!, $hire: Boolean!) {
                  createNurseDetails(userId: $uId, specialization: $spec, hospitalAffiliation: $hosp, experienceYears: $exp, hireMeStatus: $hire) {
                    id
                    specialization
                  }
                }
                """;

        graphQlTester.document(mutation)
                .variable("uId", userId)
                .variable("spec", "ER")
                .variable("hosp", "General Hospital")
                .variable("exp", 3)
                .variable("hire", false)
                .execute()
                .errors()
                .verify()
                .path("createNurseDetails")
                .entity(NurseDetails.class)
                .matches(d -> d.getSpecialization().equals("ER"));
    }

    @Test
    void updateNurseHireStatus_ShouldReturnUpdatedDetails() {
        UUID id = UUID.randomUUID();
        NurseDetails details = new NurseDetails(id, UUID.randomUUID(), "ER", "General Hospital", 3, true);
        when(nurseDetailsService.updateHireStatus(eq(id), eq(true))).thenReturn(details);

        String mutation = """
                mutation($id: ID!, $hire: Boolean!) {
                  updateNurseHireStatus(id: $id, hireMeStatus: $hire) {
                    id
                    hireMeStatus
                  }
                }
                """;

        graphQlTester.document(mutation)
                .variable("id", id)
                .variable("hire", true)
                .execute()
                .errors()
                .verify()
                .path("updateNurseHireStatus")
                .entity(NurseDetails.class)
                .matches(d -> d.isHireMeStatus());
    }
}
