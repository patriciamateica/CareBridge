//package com.carebridge.backend.oldgraphqls;
//
//import com.carebridge.backend.carenotes.CareNotesService;
//import com.carebridge.backend.carenotes.model.CareNotes;
//import org.springframework.data.domain.PageRequest;
//import org.springframework.graphql.data.method.annotation.Argument;
//import org.springframework.graphql.data.method.annotation.MutationMapping;
//import org.springframework.graphql.data.method.annotation.QueryMapping;
//import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
//import org.springframework.stereotype.Controller;
//import reactor.core.publisher.Flux;
//
//import java.time.LocalDateTime;
//import java.time.OffsetDateTime;
//import java.util.List;
//import java.util.UUID;
//
//@Controller
//public class CareNotesGraphController {
//
//    private final CareNotesService careNotesService;
//
//    public CareNotesGraphController(CareNotesService careNotesService) {
//        this.careNotesService = careNotesService;
//    }
//
//    @QueryMapping
//    public List<CareNotes> getCareNotes(@Argument int page, @Argument int size) {
//        return careNotesService.findAll(PageRequest.of(page, size)).getContent();
//    }
//
//    @QueryMapping
//    public CareNotes getCareNoteById(@Argument UUID id) {
//        return careNotesService.getById(id);
//    }
//
//    @QueryMapping
//    public List<CareNotes> getCareNotesByPatientId(
//            @Argument UUID patientId,
//            @Argument Integer page,
//            @Argument Integer size) {
//        int resolvedPage = page != null ? page : 0;
//        int resolvedSize = size != null ? size : 5;
//        return careNotesService.findByPatientId(patientId, PageRequest.of(resolvedPage, resolvedSize)).getContent();
//    }
//
//    @QueryMapping
//    public Integer getCareNotesCountByPatientId(@Argument UUID patientId) {
//        return Math.toIntExact(careNotesService.countByPatientId(patientId));
//    }
//
//    @MutationMapping
//    public CareNotes createCareNote(
//            @Argument UUID patientId, @Argument UUID nurseId,
//            @Argument String content, @Argument String timestamp) {
//
//        CareNotes careNote = new CareNotes(
//                UUID.randomUUID(), content, patientId, nurseId, parseTimestamp(timestamp));
//        return careNotesService.create(careNote);
//    }
//
//    @MutationMapping
//    public CareNotes updateCareNote(@Argument UUID id, @Argument String content, @Argument String timestamp) {
//        CareNotes existing = careNotesService.getById(id);
//        CareNotes updated = new CareNotes(
//                existing.getId(), content, existing.getPatientId(), existing.getNurseId(), parseTimestamp(timestamp));
//        return careNotesService.update(id, updated);
//    }
//
//    @MutationMapping
//    public Boolean deleteCareNote(@Argument UUID id) {
//        return careNotesService.delete(id);
//    }
//
//    @SubscriptionMapping
//    public Flux<CareNotes> onCareNoteAdded(@Argument UUID patientId) {
//        return careNotesService.getNoteStream(patientId);
//    }
//
//    private LocalDateTime parseTimestamp(String rawTimestamp) {
//        try {
//            return LocalDateTime.parse(rawTimestamp);
//        } catch (Exception ignored) {
//            return OffsetDateTime.parse(rawTimestamp).toLocalDateTime();
//        }
//    }
//}
