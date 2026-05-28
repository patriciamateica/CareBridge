package com.carebridge.backend.security;

import com.carebridge.backend.audit.LogAction;
import com.carebridge.backend.password.PasswordResetService;
import com.carebridge.backend.user.CustomUserDetails;
import com.carebridge.backend.user.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseCookie;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.web.bind.annotation.*;

import java.time.Duration;
import java.util.Map;

@RestController
@RequestMapping("/api")
public class AuthController {

    private static final Logger logger = LoggerFactory.getLogger(AuthController.class);

    private final AuthService authService;
    private final JwtService jwtService;
    private final AuthenticationManager authenticationManager;
    private final CookieProperties cookieProperties;
    private final PasswordResetService passwordResetService;
    private final UserRepository userRepository;
    private final EmailService emailService;

    public AuthController(
        AuthService authService,
        JwtService jwtService,
        AuthenticationManager authenticationManager,
        CookieProperties cookieProperties,
        PasswordResetService passwordResetService,
        UserRepository userRepository,
        EmailService emailService
    ) {
        this.authService = authService;
        this.jwtService = jwtService;
        this.authenticationManager = authenticationManager;
        this.cookieProperties = cookieProperties;
        this.passwordResetService = passwordResetService;
        this.userRepository = userRepository;
        this.emailService = emailService;
    }

    @PostMapping("/login")
    @LogAction("User Login")
    public ResponseEntity<HttpStatus> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                    loginRequest.email(), loginRequest.password()));

            if (authentication == null || authentication.getPrincipal() == null) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }

            CustomUserDetails authenticatedUser = (CustomUserDetails) authentication.getPrincipal();
            String jwtToken = jwtService.generateToken(
                authenticatedUser.getUsername(),
                authenticatedUser.getAuthorities());

            return ResponseEntity.ok()
                .header(HttpHeaders.SET_COOKIE, buildJwtCookie(jwtToken))
                .build();

        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }
    }

    @PostMapping("/logout")
    @LogAction("User Logout")
    public ResponseEntity<HttpStatus> logout() {
        ResponseCookie clearCookie = ResponseCookie.from("jwt", "")
            .httpOnly(cookieProperties.isHttpOnly())
            .secure(cookieProperties.isSecure())
            .sameSite(cookieProperties.getSameSite())
            .path(cookieProperties.getPath())
            .maxAge(0)
            .build();
        return ResponseEntity.ok()
            .header(HttpHeaders.SET_COOKIE, clearCookie.toString())
            .build();
    }

    @PostMapping("/register")
    public ResponseEntity<HttpStatus> register(@RequestBody RegisterRequest registerRequest) {
        authService.register(registerRequest);
        return ResponseEntity.status(HttpStatus.CREATED).build();
    }

    @PostMapping("/auth/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
        @RequestBody ForgotPasswordRequest request
    ) {
        try {
            passwordResetService.initiateReset(request.email());
            return ResponseEntity.ok(Map.of(
                "message", "If that email is registered you will receive a reset code shortly."));
        } catch (Exception e) {
            return ResponseEntity.ok(Map.of(
                "message", "If that email is registered you will receive a reset code shortly."));
        }
    }

    @PostMapping("/auth/reset-password")
    public ResponseEntity<Map<String, String>> resetPassword(
        @RequestBody ResetPasswordRequest request
    ) {
        try {
            passwordResetService.confirmReset(
                request.email(), request.otp(), request.newPassword());
            return ResponseEntity.ok(Map.of("message", "Password updated successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/auth/check-email")
    public ResponseEntity<Map<String, Boolean>> checkEmail(@RequestParam String email) {
        boolean exists = userRepository.findByEmailIgnoreCase(email).isPresent();
        return ResponseEntity.ok(Map.of("exists", exists));
    }

    private String buildJwtCookie(String jwtToken) {
        return ResponseCookie.from("jwt", jwtToken)
            .httpOnly(cookieProperties.isHttpOnly())
            .secure(cookieProperties.isSecure())
            .sameSite(cookieProperties.getSameSite())
            .path(cookieProperties.getPath())
            .maxAge(Duration.ofHours(cookieProperties.getMaxAgeHours()))
            .build()
            .toString();
    }

    @PostMapping("/auth/activate")
    public ResponseEntity<Map<String, String>> activate(
        @RequestBody ActivateRequest request
    ) {
        try {
            authService.activateAccount(request.email(), request.activationNumber());
            return ResponseEntity.ok(Map.of("message", "Account activated successfully."));
        } catch (IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body(Map.of("error", e.getMessage()));
        }
    }
}
