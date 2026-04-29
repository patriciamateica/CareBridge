package com.carebridge.backend.roster;

import com.carebridge.backend.roster.model.Roster;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface RosterRepository extends JpaRepository<Roster, UUID> {
    List<Roster> findByNurseId(UUID nurseId);
}
