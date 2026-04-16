package com.carebridge.backend.carenotes;

import com.carebridge.backend.carenotes.model.CareNotes;
import org.springframework.data.domain.PageRequest;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Controller
public class CareNotesGraphController {

    private final CareNotesService careNotesService;

    public CareNotesGraphController(CareNotesService careNotesService) {
        this.careNotesService = careNotesService;
    }

    @QueryMapping
    public List<CareNotes> getCareNotes(@Argument int page, @Argument int size) {
        return careNotesService.findAll(PageRequest.of(page, size)).getContent();
    }

    @QueryMapping
    public CareNotes getCareNoteById(@Argument UUID id) {
        return careNotesService.getById(id);
    }

    @QueryMapping
    public List<CareNotes> getCareNotesByPatientId(@Argument UUID patientId) {
        return careNotesService.getByPatientId(patientId);
    }

    @MutationMapping
    public CareNotes createCareNote(
            @Argument UUID patientId, @Argument UUID nurseId,
            @Argument String content, @Argument String timestamp) {

        CareNotes careNote = new CareNotes(
                UUID.randomUUID(), content, patientId, nurseId, LocalDateTime.parse(timestamp));
        return careNotesService.create(careNote);
    }

    @MutationMapping
    public Boolean deleteCareNote(@Argument UUID id) {
        return careNotesService.delete(id);
    }

    @SubscriptionMapping
    public Flux<CareNotes> onCareNoteAdded(@Argument UUID patientId) {
        return careNotesService.getNoteStream(patientId);
    }
}
