package com.carebridge.backend.security;

import com.carebridge.backend.user.model.User;
import org.springframework.graphql.data.method.annotation.Argument;
import org.springframework.graphql.data.method.annotation.MutationMapping;
import org.springframework.stereotype.Controller;

@Controller
public class AuthGraphController {

    private final AuthService authService;

    public AuthGraphController(AuthService authService) {
        this.authService = authService;
    }

    @MutationMapping
    public User registerUser(@Argument("input") RegisterRequest input) {
        return authService.register(input);
    }

    @MutationMapping
    public String loginUser(@Argument("input") LoginRequest input) {
        return authService.login(input);
    }
}
