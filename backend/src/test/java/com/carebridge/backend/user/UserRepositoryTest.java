package com.carebridge.backend.user;

import com.carebridge.backend.user.model.User;
import com.carebridge.backend.user.model.Role;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class UserRepositoryTest {

    @Autowired
    private UserRepository repository;

    @Autowired
    private RoleRepository roleRepository;

    private User sampleUser;
    private Role patientRole;

    @BeforeEach
    void setUp() {
        // Create and save the patient role first
        patientRole = new Role("PATIENT");
        patientRole = roleRepository.save(patientRole);

        sampleUser = new User();
        sampleUser.setFirstName("John");
        sampleUser.setLastName("Doe");
        sampleUser.setEmail("john@example.com");
        sampleUser.setPassword("hashedpassword");
        sampleUser.setPhoneNumber("0744123456");
        sampleUser.addRole(patientRole);
        sampleUser.setUserStatus(UserStatus.ACTIVE);
    }

    @Test
    void save_ShouldGenerateIdAndStoreUser() {
        User saved = repository.save(sampleUser);

        assertNotNull(saved.getId());
        assertEquals("john@example.com", saved.getEmail());
        assertTrue(repository.findById(saved.getId()).isPresent());
    }

    @Test
    void findByEmailIgnoreCase_ShouldReturnUserWhenExists() {
        repository.save(sampleUser);

        Optional<User> found = repository.findByEmailIgnoreCase("john@example.com");

        assertTrue(found.isPresent());
        assertEquals("John", found.get().getFirstName());
    }

    @Test
    void findByEmailIgnoreCase_ShouldReturnEmptyWhenNotExists() {
        Optional<User> found = repository.findByEmailIgnoreCase("nobody@example.com");

        assertTrue(found.isEmpty());
    }

    @Test
    void findAll_ShouldReturnCorrectPage() {
        for (int i = 0; i < 5; i++) {
            User u = new User();
            u.setEmail("user" + i + "@example.com");
            u.setFirstName("User" + i);
            u.setLastName("Test" + i);
            u.setPassword("password");
            u.addRole(patientRole);
            repository.save(u);
        }

        Page<User> page = repository.findAll(PageRequest.of(0, 2));

        assertEquals(2, page.getContent().size());
        assertEquals(5, page.getTotalElements());
    }
}
