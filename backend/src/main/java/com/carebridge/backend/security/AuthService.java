package com.carebridge.backend.security;

import com.carebridge.backend.user.UserRepository;
import com.carebridge.backend.user.UserService;
import com.carebridge.backend.user.RoleRepository;
import com.carebridge.backend.user.UserStatus;
import com.carebridge.backend.user.model.User;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;
    private final UserService userService;
    private final RoleRepository roleRepository;

    public AuthService(
            UserRepository userRepository,
            PasswordEncoder passwordEncoder,
            AuthenticationManager authenticationManager,
            JwtService jwtService,
            UserService userService,
            RoleRepository roleRepository
    ) {
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.authenticationManager = authenticationManager;
        this.jwtService = jwtService;
        this.userService = userService;
        this.roleRepository = roleRepository;
    }

    public User register(RegisterRequest request) {
        if (userRepository.findByEmailIgnoreCase(request.email()).isPresent()) {
            throw new IllegalArgumentException("Email already exists");
        }

        User user = new User();
        user.setFirstName(request.firstName());
        user.setLastName(request.lastName());
        user.setEmail(request.email());
        user.setPhoneNumber(request.phoneNumber() != null ? request.phoneNumber() : "");
        user.setPassword(passwordEncoder.encode(request.password()));

        roleRepository.findByName("PATIENT").ifPresent(user::addRole);

        user.setUserStatus(UserStatus.PENDING_ACTIVATION);

        User savedUser = userRepository.save(user);
        userService.emitRegistration(savedUser);
        return savedUser;
    }

    public String login(LoginRequest request) {
        Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(request.email(), request.password())
        );
        return jwtService.generateToken(authentication.getName(), authentication.getAuthorities());
    }

    public void activateAccount(String email, String activationNumber) {
        User user = userRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() -> new IllegalArgumentException("Account not found."));

        if (!activationNumber.equals(user.getActivationNumber())) {
            throw new IllegalArgumentException("Invalid activation code.");
        }

        if (UserStatus.ACTIVE.equals(user.getUserStatus())) {
            throw new IllegalArgumentException("Account is already activated.");
        }

        user.setUserStatus(UserStatus.ACTIVE);
        userRepository.save(user);
    }
}
