package com.carebridge.backend.user;

import com.carebridge.backend.user.model.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Repository
public class UserRepository {
    private final Map<UUID, User> users = new ConcurrentHashMap<>();

    public List<User> findAll() {
        return new ArrayList<>(users.values());
    }

    public Optional<User> findByEmail(String email) {
        return users.values().stream()
            .filter(user -> user.getEmail().equalsIgnoreCase(email))
            .findFirst();
    }

    public User save(User user) {
        if (user.getId() == null) {
            user.setId(UUID.randomUUID());
        }

        users.put(user.getId(), user);

        return user;
    }


    public Optional<User> findById(UUID id) {
        return Optional.ofNullable(users.get(id));
    }

    public void deleteById(UUID id) {
        users.remove(id);
    }

    public Page<User> findAllPaginated(Pageable pageable) {
        List<User> allUsers = new ArrayList<>(users.values());
        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), allUsers.size());

        List<User> content = (start >= allUsers.size())
            ? new ArrayList<>()
            : allUsers.subList(start, end);

        return new PageImpl<>(content, pageable, allUsers.size());
    }
}
