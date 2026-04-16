package com.carebridge.backend.user;

import com.carebridge.backend.user.model.User;
import org.springframework.data.domain.PageRequest;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.graphql.data.method.annotation.QueryMapping;
import org.springframework.graphql.data.method.annotation.SubscriptionMapping;
import org.springframework.stereotype.Controller;
import reactor.core.publisher.Flux;

import java.util.List;
import java.util.UUID;

@Controller
public class UserGraphController {

    private final UserService userService;

    public UserGraphController(UserService userService) {
        this.userService = userService;
    }

    @QueryMapping
    public List<User> getUsers(@Argument int page, @Argument int size) {
        return userService.getUsersPaginated(PageRequest.of(page, size)).getContent();
    }

    @QueryMapping
    public User getUserById(@Argument UUID id) {
        return userService.getUserById(id);
    }

    @MutationMapping
    public User updateUserStatus(@Argument UUID id, @Argument UserStatus status) {
        return userService.updateStatus(id, status);
    }

    @SubscriptionMapping
    public Flux<User> onUserRegistered() {
        return userService.getUserRegistrationStream();
    }

    @SubscriptionMapping
    public Flux<User> onUserStatusUpdated(@Argument UUID id) {
        return userService.getUserStatusStream(id);
    }
}
