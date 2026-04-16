package com.carebridge.backend.user;

import com.carebridge.backend.user.model.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

class UserRepositoryTest {

    private UserRepository repository;
    private User sampleUser;

    @BeforeEach
    void setUp() {
        repository = new com.carebridge.backend.user.UserRepository();
        sampleUser = new User();
        sampleUser.setFirstName("John");
        sampleUser.setLastName("Doe");
        sampleUser.setEmail("john@example.com");
        sampleUser.setPassword("hashedpassword");
        sampleUser.setPhoneNumber(0744123456);
        sampleUser.setRole(Role.PATIENT);
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
    void findByEmail_ShouldReturnUserWhenExists() {
        repository.save(sampleUser);

        Optional<User> found = repository.findByEmail("john@example.com");

        assertTrue(found.isPresent());
        assertEquals("John", found.get().getFirstName());
    }

    @Test
    void findByEmail_ShouldReturnEmptyWhenNotExists() {
        Optional<User> found = repository.findByEmail("nobody@example.com");

        assertTrue(found.isEmpty());
    }

    @Test
    void findAllPaginated_ShouldReturnCorrectPage() {
        for (int i = 0; i < 5; i++) {
            User u = new User();
            u.setEmail("user" + i + "@example.com");
            repository.save(u);
        }

        Page<User> page = repository.findAllPaginated(PageRequest.of(0, 2));

        assertEquals(2, page.getContent().size());
        assertEquals(5, page.getTotalElements());
    }
}
