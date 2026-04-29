package com.carebridge.backend.roster;

import com.carebridge.backend.roster.model.Roster;
import com.carebridge.backend.roster.model.RosterStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Sinks;

import java.util.List;
import java.util.UUID;

@Service
@Transactional
public class RosterService {
    private final RosterRepository rosterRepository;
    private final Sinks.Many<Roster> updateSink = Sinks.many().multicast().onBackpressureBuffer();

    public RosterService(RosterRepository rosterRepository) {
        this.rosterRepository = rosterRepository;
    }

    @Transactional(readOnly = true)
    public Page<Roster> findAll(Pageable pageable) {
        return rosterRepository.findAll(pageable);
    }

    @Transactional(readOnly = true)
    public Roster getById(UUID id) {
        return rosterRepository.findById(id).orElseThrow();
    }

    @Transactional
    public Roster create(Roster roster) {
        Roster saved = rosterRepository.save(roster);
        updateSink.tryEmitNext(saved);
        return saved;
    }

    @Transactional
    public Roster update(UUID id, Roster updatedRoster) {
        Roster oldRoster = rosterRepository.findById(id).orElseThrow();

        oldRoster.setPatient(updatedRoster.getPatient());
        oldRoster.setNurse(updatedRoster.getNurse());
        oldRoster.setStatus(updatedRoster.getStatus());

        Roster saved = rosterRepository.save(oldRoster);
        updateSink.tryEmitNext(saved);
        return saved;
    }

    @Transactional
    public Roster updateStatus(UUID id, RosterStatus status) {
        Roster roster = rosterRepository.findById(id).orElseThrow();
        roster.setStatus(status);

        Roster saved = rosterRepository.save(roster);
        updateSink.tryEmitNext(saved);
        return saved;
    }

    @Transactional
    public boolean delete(UUID id) {
        rosterRepository.findById(id).orElseThrow();
        rosterRepository.deleteById(id);
        return true;
    }

    @Transactional
    public void deleteAll() {
        rosterRepository.deleteAll();
    }

    @Transactional(readOnly = true)
    public List<Roster> getByNurseId(UUID nurseId) {
        return rosterRepository.findByNurseId(nurseId);
    }

    @Transactional
    public Flux<Roster> getUpdateStream(UUID nurseId) {
        return updateSink.asFlux()
            .filter(r -> nurseId == null || r.getNurse().getId().equals(nurseId));
    }
}
