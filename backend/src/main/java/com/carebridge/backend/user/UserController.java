package com.carebridge.backend.user;

import com.carebridge.backend.security.RegisterRequest;
import com.carebridge.backend.user.model.User;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/users")
public class UserController {

    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @PostMapping("/register")
    public ResponseEntity<User> register(@Valid @RequestBody RegisterRequest request) {
        User registeredUser = userService.register(request);
        return new ResponseEntity<>(registeredUser, HttpStatus.CREATED);
    }

    @GetMapping
    public ResponseEntity<List<User>> getUsers(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(userService.getUsersPaginated(page, size));
    }
}
