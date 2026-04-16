package com.carebridge.backend.roster;

import com.carebridge.backend.roster.model.Roster;
import com.carebridge.backend.roster.model.RosterStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.UUID;

@Service
public class RosterService {
    private final RosterRepository rosterRepository;
    private final Sinks.Many<Roster> updateSink = Sinks.many().multicast().onBackpressureBuffer();

    public RosterService(RosterRepository rosterRepository) {
        this.rosterRepository = rosterRepository;
    }

    public Page<Roster> findAll(Pageable pageable) {
        return rosterRepository.findAll(pageable);
    }

    public Roster getById(UUID id) {
        return rosterRepository.findById(id).orElseThrow();
    }

    public Roster create(Roster roster) {
        Roster saved = rosterRepository.save(roster);
        updateSink.tryEmitNext(saved);
        return saved;
    }

    public Roster update(UUID id, Roster updatedRoster) {
        Roster oldRoster = rosterRepository.findById(id).orElseThrow();

        oldRoster.setPatientId(updatedRoster.getPatientId());
        oldRoster.setNurseId(updatedRoster.getNurseId());
        oldRoster.setStatus(updatedRoster.getStatus());

        updateSink.tryEmitNext(oldRoster);
        return oldRoster;
    }

    public Roster updateStatus(UUID id, RosterStatus status) {
        Roster roster = rosterRepository.findById(id).orElseThrow();
        roster.setStatus(status);
        updateSink.tryEmitNext(roster);
        return roster;
    }

    public boolean delete(UUID id) {
        rosterRepository.findById(id).orElseThrow();
        rosterRepository.deleteById(id);
        return true;
    }

    public List<Roster> getByNurseId(UUID nurseId) {
        return rosterRepository.findByNurseId(nurseId);
    }

    public Flux<Roster> getUpdateStream(UUID nurseId) {
        return updateSink.asFlux()
                .filter(r -> nurseId == null || r.getNurseId().equals(nurseId));
    }
}
