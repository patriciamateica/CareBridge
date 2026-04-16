package com.carebridge.backend.roster;

import com.carebridge.backend.roster.model.Roster;
import com.carebridge.backend.roster.model.RosterStatus;
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
class RosterGraphControllerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockitoBean
    private RosterService rosterService;

    @Test
    void getRosters_ShouldReturnRosters() {
        Roster roster = new Roster(UUID.randomUUID(), UUID.randomUUID(), UUID.randomUUID(), RosterStatus.ACTIVE);
        when(rosterService.findAll(any(PageRequest.class))).thenReturn(new PageImpl<>(Collections.singletonList(roster)));

        String query = """
                query {
                  getRosters(page: 0, size: 10) {
                    id
                    status
                  }
                }
                """;

        graphQlTester.document(query)
                .execute()
                .errors()
                .verify()
                .path("getRosters")
                .entityList(Roster.class)
                .hasSize(1);
    }

    @Test
    void createRoster_ShouldReturnCreatedRoster() {
        UUID patientId = UUID.randomUUID();
        UUID nurseId = UUID.randomUUID();
        Roster roster = new Roster(UUID.randomUUID(), patientId, nurseId, RosterStatus.ACTIVE);
        when(rosterService.create(any(Roster.class))).thenReturn(roster);

        String mutation = """
                mutation($pId: ID!, $nId: ID!, $status: RosterStatus!) {
                  createRoster(patientId: $pId, nurseId: $nId, status: $status) {
                    id
                    status
                  }
                }
                """;

        graphQlTester.document(mutation)
                .variable("pId", patientId)
                .variable("nId", nurseId)
                .variable("status", "ACTIVE")
                .execute()
                .errors()
                .verify()
                .path("createRoster")
                .entity(Roster.class)
                .matches(r -> r.getStatus() == RosterStatus.ACTIVE);
    }

    @Test
    void updateRosterStatus_ShouldReturnUpdatedRoster() {
        UUID id = UUID.randomUUID();
        Roster roster = new Roster(id, UUID.randomUUID(), UUID.randomUUID(), RosterStatus.INACTIVE);
        when(rosterService.updateStatus(eq(id), eq(RosterStatus.INACTIVE))).thenReturn(roster);

        String mutation = """
                mutation($id: ID!, $status: RosterStatus!) {
                  updateRosterStatus(id: $id, status: $status) {
                    id
                    status
                  }
                }
                """;

        graphQlTester.document(mutation)
                .variable("id", id)
                .variable("status", "INACTIVE")
                .execute()
                .errors()
                .verify()
                .path("updateRosterStatus")
                .entity(Roster.class)
                .matches(r -> r.getStatus() == RosterStatus.INACTIVE);
    }
}
