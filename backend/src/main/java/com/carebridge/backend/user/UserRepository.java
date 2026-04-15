package com.carebridge.backend.user;

import com.carebridge.backend.user.model.User;
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

    public List<User> findAllPaginated(int page, int size) {
        List<User> allUsers = new ArrayList<>(users.values());
        int start = Math.min(page * size, allUsers.size());
        int end = Math.min((page + 1) * size, allUsers.size());
        return allUsers.subList(start, end);
    }
}
