package com.manula.beautysalon.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private static final DateTimeFormatter EXPIRY_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    private final JavaMailSender mailSender;
    private final String appBaseUrl;
    private final String fromAddress;

    public EmailService(
            JavaMailSender mailSender,
            @Value("${app.base-url:http://localhost:8080}") String appBaseUrl,
            @Value("${app.mail.from:no-reply@lumieresalon.local}") String fromAddress
    ) {
        this.mailSender = mailSender;
        this.appBaseUrl = appBaseUrl;
        this.fromAddress = fromAddress;
    }

    public PasswordSetupEmailResult sendPasswordSetupEmail(
            String recipientEmail,
            String recipientName,
            String accountType,
            PasswordSetupToken setupToken
    ) {
        String setupLink = buildPasswordSetupLink(accountType, setupToken.rawToken());
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(recipientEmail);
            message.setSubject("Set up your Lumiere Salon password");
            message.setText(buildPasswordSetupMessage(recipientName, accountType, setupLink, setupToken.expiresAt()));
            mailSender.send(message);
            return new PasswordSetupEmailResult(true);
        } catch (MailException ex) {
            logger.warn("Password setup email could not be sent to {}: {}", recipientEmail, ex.getMessage());
            return new PasswordSetupEmailResult(false);
        }
    }

    public boolean sendPasswordResetEmail(
            String recipientEmail,
            String recipientName,
            String accountType,
            PasswordResetToken resetToken
    ) {
        String resetLink = buildPasswordResetLink(resetToken.rawToken());
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromAddress);
            message.setTo(recipientEmail);
            message.setSubject("Reset your Lumiere Salon password");
            message.setText(buildPasswordResetMessage(recipientName, accountType, resetLink, resetToken.expiresAt()));
            mailSender.send(message);
            return true;
        } catch (MailException ex) {
            logger.warn("Password reset email could not be sent to {}: {}", recipientEmail, ex.getMessage());
            return false;
        }
    }

    private String buildPasswordSetupLink(String accountType, String rawToken) {
        return UriComponentsBuilder.fromUriString(normalizedBaseUrl())
                .path("/password-setup")
                .queryParam("type", accountType.toLowerCase(Locale.ROOT))
                .queryParam("token", rawToken)
                .toUriString();
    }

    private String buildPasswordResetLink(String rawToken) {
        return UriComponentsBuilder.fromUriString(normalizedBaseUrl())
                .path("/reset-password")
                .queryParam("token", rawToken)
                .toUriString();
    }

    private String normalizedBaseUrl() {
        if (appBaseUrl == null || appBaseUrl.isBlank()) {
            return "http://localhost:8080";
        }
        return appBaseUrl.endsWith("/") ? appBaseUrl.substring(0, appBaseUrl.length() - 1) : appBaseUrl;
    }

    private String buildPasswordSetupMessage(String recipientName, String accountType, String setupLink, LocalDateTime expiresAt) {
        String displayName = recipientName == null || recipientName.isBlank() ? "there" : recipientName.trim();
        String displayAccountType = accountType == null || accountType.isBlank() ? "salon" : accountType.toLowerCase(Locale.ROOT);
        return "Hello " + displayName + ",\n\n"
                + "Your " + displayAccountType + " account for Lumiere Salon has been created.\n\n"
                + "Please set your password using this link:\n"
                + setupLink + "\n\n"
                + "This link expires at " + EXPIRY_FORMATTER.format(expiresAt) + ".\n\n"
                + "Do not share this link with anyone. Anyone with access to it can set your password until it expires.\n\n"
                + "Thank you,\n"
                + "Lumiere Salon";
    }

    private String buildPasswordResetMessage(String recipientName, String accountType, String resetLink, LocalDateTime expiresAt) {
        String displayName = recipientName == null || recipientName.isBlank() ? "there" : recipientName.trim();
        String displayAccountType = accountType == null || accountType.isBlank() ? "salon" : accountType.toLowerCase(Locale.ROOT);
        return "Hello " + displayName + ",\n\n"
                + "We received a request to reset the password for your " + displayAccountType + " account at Lumiere Salon.\n\n"
                + "Reset your password using this link:\n"
                + resetLink + "\n\n"
                + "This link expires at " + EXPIRY_FORMATTER.format(expiresAt) + ".\n\n"
                + "If you did not request this password reset, you can ignore this email.\n"
                + "Do not share this link with anyone. Anyone with access to it can reset your password until it expires.\n\n"
                + "Thank you,\n"
                + "Lumiere Salon";
    }
}
