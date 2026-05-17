package com.carebridge.backend.password;

import com.carebridge.backend.security.EmailService;
import com.carebridge.backend.user.UserRepository;
import com.carebridge.backend.user.model.User;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.security.SecureRandom;
import java.time.LocalDateTime;

@Service
public class PasswordResetService {

    private static final int OTP_EXPIRY_MINUTES = 15;

    private final PasswordResetTokenRepository tokenRepository;
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;

    public PasswordResetService(
        PasswordResetTokenRepository tokenRepository,
        UserRepository userRepository,
        PasswordEncoder passwordEncoder,
        EmailService emailService) {
        this.tokenRepository = tokenRepository;
        this.userRepository = userRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }


    @Transactional
    public void initiateReset(String email) {
        userRepository.findByEmailIgnoreCase(email).ifPresent(user -> {
            tokenRepository.deleteAllByEmail(email.toLowerCase());

            String otp = generateOtp();
            LocalDateTime expiry = LocalDateTime.now().plusMinutes(OTP_EXPIRY_MINUTES);
            tokenRepository.save(new PasswordResetToken(otp, email.toLowerCase(), expiry));

            emailService.sendPasswordResetEmail(email, otp);
        });
    }

    /**
     * Step 2: validate OTP and update password.
     */
    @Transactional
    public void confirmReset(String email, String otp, String newPassword) {
        PasswordResetToken prt = tokenRepository.findByToken(otp)
            .orElseThrow(() ->
                new IllegalArgumentException("Invalid code. Please check and try again."));

        if (!prt.getEmail().equalsIgnoreCase(email)) {
            throw new IllegalArgumentException("Invalid code. Please check and try again.");
        }
        if (prt.isUsed()) {
            throw new IllegalArgumentException("This code has already been used.");
        }
        if (prt.isExpired()) {
            throw new IllegalArgumentException("This code has expired. Please request a new one.");
        }

        User user = userRepository.findByEmailIgnoreCase(email)
            .orElseThrow(() ->
                new IllegalArgumentException("No account found for that email."));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        prt.setUsed(true);
        tokenRepository.save(prt);
        tokenRepository.deleteAllByEmail(email.toLowerCase());
    }

    private String generateOtp() {
        int code = new SecureRandom().nextInt(900000) + 100000;
        return String.valueOf(code);
    }
}
