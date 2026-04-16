package com.carebridge.backend.prescription;

import com.carebridge.backend.prescription.model.Prescription;
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
public class PrescriptionGraphController {

    private final PrescriptionService prescriptionService;

    public PrescriptionGraphController(PrescriptionService prescriptionService) {
        this.prescriptionService = prescriptionService;
    }

    @QueryMapping
    public List<Prescription> getPrescriptions(@Argument int page, @Argument int size) {
        return prescriptionService.findAll(PageRequest.of(page, size)).getContent();
    }

    @QueryMapping
    public Prescription getPrescriptionById(@Argument UUID id) {
        return prescriptionService.getById(id);
    }

    @QueryMapping
    public List<Prescription> getPrescriptionsByPatientId(@Argument UUID patientId) {
        return prescriptionService.getByPatientId(patientId);
    }

    @MutationMapping
    public Prescription createPrescription(
            @Argument String name, @Argument String dose,
            @Argument String timing, @Argument UUID patientId,
            @Argument UUID nurseId) {

        Prescription prescription = new Prescription(
                UUID.randomUUID(), name, dose, timing, patientId, nurseId);
        return prescriptionService.create(prescription);
    }

    @MutationMapping
    public Boolean deletePrescription(@Argument UUID id) {
        return prescriptionService.delete(id);
    }

    @SubscriptionMapping
    public Flux<Prescription> onPrescriptionCreated(@Argument UUID patientId) {
        return prescriptionService.getCreatedStream(patientId);
    }

    @SubscriptionMapping
    public Flux<UUID> onPrescriptionDeleted(@Argument UUID patientId) {
        return prescriptionService.getDeletedStream(patientId);
    }
}
