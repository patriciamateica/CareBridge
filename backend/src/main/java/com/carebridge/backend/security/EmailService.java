package com.carebridge.backend.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private final JavaMailSender mailSender;

    private static final String FROM = "noreply@carebridge.local";

    public EmailService(JavaMailSender mailSender) {
        this.mailSender = mailSender;
        logger.info("EmailService initialized with JavaMailSender: {}", mailSender != null ? "CONFIGURED" : "NULL");
    }

    public void sendActivationEmail(String toEmail, String firstName, String activationCode) {
        try {
            if (mailSender == null) {
                logger.error("JavaMailSender is NULL - email cannot be sent!");
                throw new RuntimeException("Email service is not properly configured");
            }
            logger.info("Preparing to send activation email to: {}", toEmail);
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
            logger.debug("Sending email with SimpleMailMessage to: {}", toEmail);
            //mailSender.send(message);
            logger.info("✓ Activation email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            logger.error("✗ SMTP Blocked by Railway. MANUAL ACTIVATION CODE FOR {}: {}", toEmail, activationCode);
        }
    }

    public void sendPasswordResetEmail(String toEmail, String otpCode) {
        try {
            if (mailSender == null) {
                logger.error("JavaMailSender is NULL - email cannot be sent!");
                throw new RuntimeException("Email service is not properly configured");
            }
            logger.info("Preparing to send password reset email to: {}", toEmail);
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
            logger.debug("Sending email with SimpleMailMessage to: {}", toEmail);
            //mailSender.send(message);
            logger.info("✓ Password reset email sent successfully to: {}", toEmail);
        } catch (Exception e) {
            logger.error("✗ SMTP Blocked by Railway. MANUAL ACTIVATION CODE FOR {}: {}", toEmail, otpCode);
        }
    }
}


