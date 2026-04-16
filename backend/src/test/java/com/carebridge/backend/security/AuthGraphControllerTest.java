package com.carebridge.backend.security;

import com.carebridge.backend.user.model.User;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.graphql.tester.AutoConfigureGraphQlTester;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.graphql.test.tester.GraphQlTester;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

@SpringBootTest
@AutoConfigureGraphQlTester
class AuthGraphControllerTest {

    @Autowired
    private GraphQlTester graphQlTester;

    @MockitoBean
    private AuthService authService;

    @Test
    void registerUser_ShouldReturnUser() {
        User user = new User();
        user.setEmail("test@example.com");
        when(authService.register(any(RegisterRequest.class))).thenReturn(user);

        String mutation = """
                mutation($input: RegisterRequestInput!) {
                  registerUser(input: $input) {
                    email
                  }
                }
                """;

        graphQlTester.document(mutation)
                .variable("input", java.util.Map.of(
                        "firstName", "First",
                        "lastName", "Last",
                        "email", "test@example.com",
                        "phoneNumber", "123456",
                        "password", "pass"
                ))
                .execute()
                .path("registerUser")
                .entity(User.class)
                .matches(u -> u.getEmail().equals("test@example.com"));
    }

    @Test
    void loginUser_ShouldReturnToken() {
        when(authService.login(any(LoginRequest.class))).thenReturn("mock-token");

        String mutation = """
                mutation($input: LoginRequestInput!) {
                  loginUser(input: $input)
                }
                """;

        graphQlTester.document(mutation)
                .variable("input", java.util.Map.of(
                        "email", "test@example.com",
                        "password", "pass"
                ))
                .execute()
                .path("loginUser")
                .entity(String.class)
                .isEqualTo("mock-token");
    }
}
