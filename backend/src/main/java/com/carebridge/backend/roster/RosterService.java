package com.carebridge.backend.roster;

import com.carebridge.backend.roster.model.Roster;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class RosterService {
    private final RosterRepository rosterRepository;

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
        return rosterRepository.save(roster);
    }

    public Roster update(UUID id, Roster updatedRoster) {
        Roster oldRoster = rosterRepository.findById(id).orElseThrow();

        oldRoster.setPatientId(updatedRoster.getPatientId());
        oldRoster.setNurseId(updatedRoster.getNurseId());
        oldRoster.setStatus(updatedRoster.getStatus());

        return oldRoster;
    }

    public boolean delete(UUID id) {
        rosterRepository.findById(id).orElseThrow();
        rosterRepository.deleteById(id);
        return true;
    }
}
