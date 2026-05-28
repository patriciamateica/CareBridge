package com.carebridge.backend.security;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.mail.MailProperties;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.JavaMailSenderImpl;

import java.util.Properties;

@Configuration
@EnableConfigurationProperties(MailProperties.class)
public class MailConfig {

    private static final Logger logger = LoggerFactory.getLogger(MailConfig.class);

    @Bean
    public JavaMailSender javaMailSender(MailProperties mailProperties) {
        logger.info("========== MAIL CONFIGURATION ==========");
        logger.info("Configuring JavaMailSender with properties:");
        logger.info("  Host: {}", mailProperties.getHost());
        logger.info("  Port: {}", mailProperties.getPort());
        logger.info("  Username: {}", mailProperties.getUsername() != null ? "***" : "NOT SET");
        logger.info("  Password: {}", mailProperties.getPassword() != null ? "***" : "NOT SET");

        JavaMailSenderImpl mailSender = new JavaMailSenderImpl();
        mailSender.setHost(mailProperties.getHost());
        mailSender.setPort(mailProperties.getPort());
        mailSender.setUsername(mailProperties.getUsername());
        mailSender.setPassword(mailProperties.getPassword());

        Properties props = new Properties();

        if (mailProperties.getProperties() != null) {
            props.putAll(mailProperties.getProperties());
        }

        props.put("mail.transport.protocol", "smtp");
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.starttls.enable", "true");
        props.put("mail.smtp.starttls.required", "true");
        props.put("mail.smtp.connectiontimeout", "15000");
        props.put("mail.smtp.timeout", "15000");
        props.put("mail.smtp.writetimeout", "15000");
        props.put("mail.debug", "false");

        logger.info("Mail properties set:");
        props.forEach((key, value) -> logger.info("  {}: {}", key,
            key.toString().contains("password") || key.toString().contains("username") ? "***" : value));

        mailSender.setJavaMailProperties(props);

        logger.info("✓ JavaMailSender bean created successfully");
        logger.info("========================================");
        return mailSender;
    }
}



