package com.carebridge.backend.security;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private final JavaMailSender mailSender;

    private static final String FROM = "noreply@carebridge.local";

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    public void sendActivationEmail(String toEmail, String firstName, String activationCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(FROM);
        message.setTo(toEmail);
        message.setSubject("CareBridge — Your Activation Code");
        message.setText(
            "Hello " + firstName + ",\n\n" +
                "Your CareBridge account has been created.\n\n" +
                "Your activation code is:\n\n" +
                "    " + activationCode + "\n\n" +
                "Enter this code on the Activate Account page to complete your registration.\n\n" +
                "If you did not expect this email, please ignore it.\n\n" +
                "— The CareBridge Team"
        );
        mailSender.send(message);
    }

    public void sendPasswordResetEmail(String toEmail, String otpCode) {
        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(FROM);
        message.setTo(toEmail);
        message.setSubject("CareBridge — Password Reset Code");
        message.setText(
            "Hello,\n\n" +
                "We received a request to reset your CareBridge password.\n\n" +
                "Your reset code is:\n\n" +
                "    " + otpCode + "\n\n" +
                "This code expires in 15 minutes.\n\n" +
                "If you did not request a password reset, you can safely ignore this email.\n\n" +
                "— The CareBridge Team"
        );
        mailSender.send(message);
    }
}
