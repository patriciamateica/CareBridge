package com.carebridge.backend.healthStatus;

import com.carebridge.backend.healthStatus.model.HealthStatus;
import com.carebridge.backend.healthStatus.model.Mood;
import org.springframework.data.domain.PageRequest;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

@Controller
public class HealthStatusGraphController {

    private final HealthStatusService healthStatusService;

    public HealthStatusGraphController(HealthStatusService healthStatusService) {
        this.healthStatusService = healthStatusService;
    }

    @QueryMapping
    public List<HealthStatus> getHealthStatuses(@Argument int page, @Argument int size) {
        return healthStatusService.findAll(PageRequest.of(page, size)).getContent();
    }

    @QueryMapping
    public HealthStatus getHealthStatusById(@Argument UUID id) {
        return healthStatusService.getById(id);
    }

    @QueryMapping
    public List<HealthStatus> getHealthStatusesByPatientId(@Argument UUID patientId) {
        return healthStatusService.getByPatientId(patientId);
    }

    @MutationMapping
    public HealthStatus createHealthStatus(
            @Argument UUID patientId, @Argument int painScale,
            @Argument Mood mood, @Argument List<String> symptoms,
            @Argument String notes, @Argument String timestamp) {

        HealthStatus healthStatus = new HealthStatus(
                UUID.randomUUID(), painScale, mood, symptoms, notes,
                LocalDate.parse(timestamp), patientId);
        return healthStatusService.create(healthStatus);
    }

    @MutationMapping
    public Boolean deleteHealthStatus(@Argument UUID id) {
        return healthStatusService.delete(id);
    }

    @SubscriptionMapping
    public Flux<HealthStatus> onHealthStatusAdded(@Argument UUID patientId) {
        return healthStatusService.getStatusStream(patientId);
    }
}
