package com.carebridge.backend.user;

import com.carebridge.backend.user.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Collections;
import java.util.UUID;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureGraphQlTester
class UserGraphControllerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockitoBean
    private UserService userService;

    @Test
    void getUsers_ShouldReturnUsers() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("test@example.com");
        when(userService.getUsersPaginated(any(PageRequest.class))).thenReturn(new PageImpl<>(Collections.singletonList(user)));

        String query = """
                query {
                  getUsers(page: 0, size: 10) {
                    id
                    email
                  }
                }
                """;

        graphQlTester.document(query)
                .execute()
                .errors()
                .verify()
                .path("getUsers")
                .entityList(User.class)
                .hasSize(1);
    }

    @Test
    void updateUserStatus_ShouldReturnUpdatedUser() {
        UUID id = UUID.randomUUID();
        User user = new User();
        user.setId(id);
        user.setUserStatus(UserStatus.ACTIVE);
        when(userService.updateStatus(any(UUID.class), any(UserStatus.class))).thenReturn(user);

        String mutation = """
                mutation($id: ID!, $status: UserStatus!) {
                  updateUserStatus(id: $id, status: $status) {
                    id
                    userStatus
                  }
                }
                """;

        graphQlTester.document(mutation)
                .variable("id", id)
                .variable("status", "ACTIVE")
                .execute()
                .errors()
                .verify()
                .path("updateUserStatus")
                .entity(User.class)
                .matches(u -> u.getUserStatus() == UserStatus.ACTIVE);
    }
}
