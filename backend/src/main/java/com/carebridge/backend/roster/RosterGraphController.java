package com.carebridge.backend.roster;

import com.carebridge.backend.roster.model.Roster;
import com.carebridge.backend.roster.model.RosterStatus;
import org.springframework.data.domain.PageRequest;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

@Controller
public class RosterGraphController {

    private final RosterService rosterService;

    public RosterGraphController(RosterService rosterService) {
        this.rosterService = rosterService;
    }

    @QueryMapping
    public List<Roster> getRosters(@Argument int page, @Argument int size) {
        return rosterService.findAll(PageRequest.of(page, size)).getContent();
    }

    @QueryMapping
    public Roster getRosterById(@Argument UUID id) {
        return rosterService.getById(id);
    }

    @QueryMapping
    public List<Roster> getRostersByNurseId(@Argument UUID nurseId) {
        return rosterService.getByNurseId(nurseId);
    }

    @MutationMapping
    public Roster createRoster(@Argument UUID patientId, @Argument UUID nurseId, @Argument RosterStatus status) {
        Roster roster = new Roster(UUID.randomUUID(), patientId, nurseId, status);
        return rosterService.create(roster);
    }

    @MutationMapping
    public Roster updateRosterStatus(@Argument UUID id, @Argument RosterStatus status) {
        return rosterService.updateStatus(id, status);
    }

    @MutationMapping
    public Boolean deleteRoster(@Argument UUID id) {
        return rosterService.delete(id);
    }

    @SubscriptionMapping
    public Flux<Roster> onRosterUpdated(@Argument UUID nurseId) {
        return rosterService.getUpdateStream(nurseId);
    }
}
