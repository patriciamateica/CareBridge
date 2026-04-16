package com.carebridge.backend.nursedetails;

import com.carebridge.backend.nursedetails.model.NurseDetails;
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
public class NurseDetailsGraphController {

    private final NurseDetailsService nurseDetailsService;

    public NurseDetailsGraphController(NurseDetailsService nurseDetailsService) {
        this.nurseDetailsService = nurseDetailsService;
    }

    @QueryMapping
    public List<NurseDetails> getAllNurseDetails(@Argument int page, @Argument int size) {
        return nurseDetailsService.findAll(PageRequest.of(page, size)).getContent();
    }

    @QueryMapping
    public NurseDetails getNurseDetailsById(@Argument UUID id) {
        return nurseDetailsService.getById(id);
    }

    @QueryMapping
    public NurseDetails getNurseDetailsByUserId(@Argument UUID userId) {
        return nurseDetailsService.getByUserId(userId);
    }

    @MutationMapping
    public NurseDetails createNurseDetails(
            @Argument UUID userId, @Argument String specialization,
            @Argument String hospitalAffiliation, @Argument int experienceYears,
            @Argument boolean hireMeStatus) {

        NurseDetails nurseDetails = new NurseDetails(
                UUID.randomUUID(), userId, specialization, hospitalAffiliation,
                experienceYears, hireMeStatus);
        return nurseDetailsService.create(nurseDetails);
    }

    @MutationMapping
    public NurseDetails updateNurseHireStatus(@Argument UUID id, @Argument boolean hireMeStatus) {
        return nurseDetailsService.updateHireStatus(id, hireMeStatus);
    }

    @SubscriptionMapping
    public Flux<NurseDetails> onNurseDetailsCreated(@Argument UUID userId) {
        return nurseDetailsService.getCreatedStream(userId);
    }

    @SubscriptionMapping
    public Flux<NurseDetails> onNurseHireStatusUpdated(@Argument UUID id) {
        return nurseDetailsService.getHireStatusStream(id);
    }
}
