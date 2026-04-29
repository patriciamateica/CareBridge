//package com.carebridge.backend.oldgraphqls;
//
//import com.carebridge.backend.vitals.VitalsService;
//import com.carebridge.backend.vitals.model.Vitals;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.graphql.data.method.annotation.Argument;
//import org.springframework.graphql.data.method.annotation.MutationMapping;
//import org.springframework.graphql.data.method.annotation.QueryMapping;
//import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
//import org.springframework.stereotype.Controller;
//import reactor.core.publisher.Flux;
//
//import java.time.LocalDate;
//import java.util.List;
//import java.util.UUID;
//
//@Controller
//public class VitalsGraphController {
//
//    private final VitalsService vitalsService;
//
//    public VitalsGraphController(VitalsService vitalsService) {
//        this.vitalsService = vitalsService;
//    }
//
//    @QueryMapping
//    public List<Vitals> getVitals(@Argument int page, @Argument int size) {
//        return vitalsService.findAll(PageRequest.of(page, size)).getContent();
//    }
//
//    @QueryMapping
//    public Vitals getVitalsById(@Argument UUID id) {
//        return vitalsService.getById(id);
//    }
//
//    @QueryMapping
//    public List<Vitals> getVitalsByPatientId(@Argument UUID patientId) {
//        return vitalsService.getByPatientId(patientId);
//    }
//
//    @MutationMapping
//    public Vitals createVitals(
//        @Argument UUID patientId, @Argument int heartRate,
//        @Argument int bloodPressure, @Argument int respiratoryRate,
//        @Argument int spO2, @Argument String timestamp) {
//
//        Vitals newVitals = new Vitals(UUID.randomUUID(), LocalDate.parse(timestamp),
//            heartRate, bloodPressure, respiratoryRate, spO2, patientId);
//        return vitalsService.create(newVitals);
//    }
//
//    @MutationMapping
//    public Boolean deleteVitals(@Argument UUID id) {
//        return vitalsService.delete(id);
//    }
//
//    @SubscriptionMapping
//    public Flux<Vitals> onVitalsAdded(@Argument UUID patientId) {
//        return vitalsService.getVitalsStream(patientId);
//    }
//}
