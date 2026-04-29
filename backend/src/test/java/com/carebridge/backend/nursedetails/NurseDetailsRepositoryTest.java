package com.carebridge.backend.nursedetails;

import com.carebridge.backend.nursedetails.model.NurseDetails;
import com.carebridge.backend.user.Role;
import com.carebridge.backend.user.UserRepository;
import com.carebridge.backend.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class NurseDetailsRepositoryTest {

    @Autowired
    private NurseDetailsRepository repository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private TestEntityManager entityManager;

    private User nurseUser;

    @BeforeEach
    void setUp() {
        nurseUser = new User();
        nurseUser.setEmail("nurse@test.com");
        nurseUser.setFirstName("Test");
        nurseUser.setLastName("Nurse");
        nurseUser.setPassword("password");
        nurseUser.setRole(Role.NURSE);
        nurseUser = userRepository.saveAndFlush(nurseUser);
    }

    private NurseDetails createNurseDetails(User user, String spec, String hospital, int years, boolean hire) {
        NurseDetails details = new NurseDetails();
        details.setUser(user);
        details.setSpecialization(spec);
        details.setHospitalAffiliation(hospital);
        details.setExperienceYears(years);
        details.setHireMeStatus(hire);
        entityManager.persist(details);
        entityManager.flush();
        return details;
    }

    @Test
    void save_ShouldGenerateIdAndStoreNurseDetails() {
        NurseDetails saved = createNurseDetails(nurseUser, "Pediatrics", "Central Hospital", 5, true);

        assertNotNull(saved.getId());
        assertEquals("Pediatrics", saved.getSpecialization());
        assertTrue(repository.findById(saved.getId()).isPresent());
    }

    @Test
    void findById_ShouldReturnNurseDetailsWhenExists() {
        NurseDetails saved = createNurseDetails(nurseUser, "Pediatrics", "Central Hospital", 5, true);

        Optional<NurseDetails> found = repository.findById(saved.getId());

        assertTrue(found.isPresent());
        assertEquals(saved.getId(), found.get().getId());
    }

    @Test
    void findById_ShouldReturnEmptyWhenNotExists() {
        Optional<NurseDetails> found = repository.findById(UUID.randomUUID());

        assertTrue(found.isEmpty());
    }

    @Test
    void findAll_ShouldReturnCorrectPage() {
        for (int i = 0; i < 5; i++) {
            User user = new User();
            user.setEmail("nurse" + i + "@test.com");
            user.setFirstName("Nurse");
            user.setLastName("Test" + i);
            user.setPassword("password");
            user.setRole(Role.NURSE);
            user = userRepository.saveAndFlush(user);
            createNurseDetails(user, "Spec " + i, "Hospital", 2, false);
        }

        Page<NurseDetails> page = repository.findAll(PageRequest.of(0, 2));

        assertEquals(2, page.getContent().size());
        assertEquals(5, page.getTotalElements());
        assertEquals(3, page.getTotalPages());
    }

    @Test
    void findAll_ShouldReturnEmptyIfOutOfBounds() {
        createNurseDetails(nurseUser, "Pediatrics", "Central Hospital", 5, true);

        Page<NurseDetails> page = repository.findAll(PageRequest.of(5, 10));

        assertTrue(page.getContent().isEmpty());
    }

    @Test
    void deleteById_ShouldRemoveNurseDetails() {
        NurseDetails saved = createNurseDetails(nurseUser, "Pediatrics", "Central Hospital", 5, true);

        repository.deleteById(saved.getId());
        entityManager.flush();
        entityManager.clear();

        assertTrue(repository.findById(saved.getId()).isEmpty());
    }

    @Test
    void findByUserId_ShouldReturnNurseDetailsWhenExists() {
        createNurseDetails(nurseUser, "Pediatrics", "Central Hospital", 5, true);

        java.util.Optional<NurseDetails> found = repository.findByUserId(nurseUser.getId());

        assertTrue(found.isPresent());
        assertEquals("Pediatrics", found.get().getSpecialization());
        assertEquals(nurseUser.getId(), found.get().getUser().getId());
    }

    @Test
    void findByUserId_ShouldReturnEmptyWhenNotExists() {
        java.util.Optional<NurseDetails> found = repository.findByUserId(UUID.randomUUID());

        assertTrue(found.isEmpty());
    }
}
