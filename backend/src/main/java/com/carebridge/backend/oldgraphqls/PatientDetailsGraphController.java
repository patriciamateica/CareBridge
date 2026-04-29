//package com.carebridge.backend.oldgraphqls;
//
//import com.carebridge.backend.carenotes.CareNotesService;
//import com.carebridge.backend.carenotes.model.CareNotes;
//import com.carebridge.backend.patientDetails.PatientDetailsService;
//import com.carebridge.backend.patientDetails.model.PatientDetails;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.graphql.data.method.annotation.Argument;
//import org.springframework.graphql.data.method.annotation.MutationMapping;
//import org.springframework.graphql.data.method.annotation.QueryMapping;
//import org.springframework.graphql.data.method.annotation.SchemaMapping;
//import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
//import org.springframework.stereotype.Controller;
//import reactor.core.publisher.Flux;
//
//import java.util.List;
//import java.util.UUID;
//
//@Controller
//public class PatientDetailsGraphController {
//
//    private final PatientDetailsService patientDetailsService;
//    private final CareNotesService careNotesService;
//
//    public PatientDetailsGraphController(PatientDetailsService patientDetailsService, CareNotesService careNotesService) {
//        this.patientDetailsService = patientDetailsService;
//        this.careNotesService = careNotesService;
//    }
//
//    @QueryMapping
//    public List<PatientDetails> getAllPatientDetails(@Argument int page, @Argument int size) {
//        return patientDetailsService.findAll(PageRequest.of(page, size)).getContent();
//    }
//
//    @QueryMapping
//    public PatientDetails getPatientDetailsById(@Argument UUID id) {
//        return patientDetailsService.getById(id);
//    }
//
//    @QueryMapping
//    public PatientDetails getPatientDetailsByUserId(@Argument UUID userId) {
//        return patientDetailsService.getByUserId(userId);
//    }
//
//    @MutationMapping
//    public PatientDetails createPatientDetails(
//            @Argument UUID userId, @Argument String primaryDiagnosis,
//            @Argument List<String> diagnostics, @Argument List<String> scans,
//            @Argument String emergencyContact, @Argument UUID assignedNurseId) {
//
//        PatientDetails patientDetails = new PatientDetails(
//                UUID.randomUUID(), userId, primaryDiagnosis, diagnostics, scans,
//                emergencyContact, assignedNurseId);
//        return patientDetailsService.create(patientDetails);
//    }
//
//    @MutationMapping
//    public PatientDetails updatePatientDiagnosis(@Argument UUID id, @Argument String primaryDiagnosis) {
//        return patientDetailsService.updateDiagnosis(id, primaryDiagnosis);
//    }
//
//    @SubscriptionMapping
//    public Flux<PatientDetails> onPatientDetailsCreated(@Argument UUID userId) {
//        return patientDetailsService.getCreatedStream(userId);
//    }
//
//    @SubscriptionMapping
//    public Flux<PatientDetails> onPatientDiagnosisUpdated(@Argument UUID id) {
//        return patientDetailsService.getDiagnosisStream(id);
//    }
//
//    @SchemaMapping(typeName = "PatientDetails", field = "careNotes")
//    public List<CareNotes> careNotes(PatientDetails patientDetails) {
//        return careNotesService.getByPatientId(patientDetails.getUserId());
//    }
//}
