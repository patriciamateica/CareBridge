package com.carebridge.backend.roster;

import com.carebridge.backend.roster.model.Roster;
import com.carebridge.backend.roster.model.RosterStatus;
import com.carebridge.backend.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RosterServiceTest {

    @Mock
    private RosterRepository rosterRepository;

    @InjectMocks
    private RosterService rosterService;

    private Roster sampleRoster;
    private UUID rosterId;
    private User patientUser;
    private User nurseUser;

    @BeforeEach
    void setUp() {
        rosterId = UUID.randomUUID();

        patientUser = new User();
        patientUser.setId(UUID.randomUUID());
        patientUser.setEmail("patient@test.com");

        nurseUser = new User();
        nurseUser.setId(UUID.randomUUID());
        nurseUser.setEmail("nurse@test.com");

        sampleRoster = new Roster(
            rosterId,
            patientUser,
            nurseUser,
            RosterStatus.PENDING
        );
    }

    @Test
    void findAll_ShouldReturnPageOfRosters() {
        PageRequest pageRequest = PageRequest.of(0, 10);
        Page<Roster> mockPage = new PageImpl<>(List.of(sampleRoster));
        when(rosterRepository.findAll(pageRequest)).thenReturn(mockPage);

        Page<Roster> result = rosterService.findAll(pageRequest);

        assertEquals(1, result.getTotalElements());
        verify(rosterRepository, times(1)).findAll(pageRequest);
    }

    @Test
    void getById_ShouldReturnRosterWhenExists() {
        when(rosterRepository.findById(rosterId)).thenReturn(Optional.of(sampleRoster));

        Roster result = rosterService.getById(rosterId);

        assertNotNull(result);
        assertEquals(rosterId, result.getId());
    }

    @Test
    void getById_ShouldThrowExceptionWhenNotExists() {
        when(rosterRepository.findById(rosterId)).thenReturn(Optional.empty());

        assertThrows(NoSuchElementException.class, () -> rosterService.getById(rosterId));
    }

    @Test
    void create_ShouldSaveAndReturnRoster() {
        when(rosterRepository.save(sampleRoster)).thenReturn(sampleRoster);

        Roster result = rosterService.create(sampleRoster);

        assertNotNull(result);
        verify(rosterRepository, times(1)).save(sampleRoster);
    }

    @Test
    void update_ShouldUpdateAndReturnRosterWhenExists() {
        User another_patient = new User();
        another_patient.setId(UUID.randomUUID());
        another_patient.setEmail("patient2@test.com");

        User another_nurse = new User();
        another_nurse.setId(UUID.randomUUID());
        another_nurse.setEmail("nurse2@test.com");

        Roster updatedData = new Roster(null, another_patient, another_nurse, RosterStatus.ACTIVE);
        when(rosterRepository.findById(rosterId)).thenReturn(Optional.of(sampleRoster));
        when(rosterRepository.save(any(Roster.class))).thenAnswer(invocation -> invocation.getArgument(0));


        Roster result = rosterService.update(rosterId, updatedData);

        assertEquals(RosterStatus.ACTIVE, result.getStatus());
        assertEquals(rosterId, result.getId());
        assertEquals(another_patient, result.getPatient());
        assertEquals(another_nurse, result.getNurse());
    }

    @Test
    void delete_ShouldRemoveRosterWhenExists() {
        when(rosterRepository.findById(rosterId)).thenReturn(Optional.of(sampleRoster));

        boolean result = rosterService.delete(rosterId);

        assertTrue(result);
        verify(rosterRepository, times(1)).deleteById(rosterId);
    }
}
