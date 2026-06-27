package com.manula.beautysalon.service;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

@Service
public class EmailService {

    private static final Logger logger = LoggerFactory.getLogger(EmailService.class);
    private static final DateTimeFormatter EXPIRY_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");
    private static final URI BREVO_SEND_EMAIL_URI = URI.create("https://api.brevo.com/v3/smtp/email");
    private static final Duration CONNECT_TIMEOUT = Duration.ofSeconds(5);
    private static final Duration REQUEST_TIMEOUT = Duration.ofSeconds(10);
    private static final String BREVO_PROVIDER = "brevo";

    private final ObjectMapper objectMapper;
    private final HttpClient httpClient;
    private final String appBaseUrl;
    private final String fromAddress;
    private final String fromName;
    private final String emailProvider;
    private final String brevoApiKey;

    // Transactional email credentials are configured through environment variables. Do not
    // hardcode API keys, reset tokens, setup tokens, or full password links in source files.
    public EmailService(
            ObjectMapper objectMapper,
            @Value("${app.base-url:http://localhost:8080}") String appBaseUrl,
            @Value("${app.mail.from:no-reply@lumieresalon.local}") String fromAddress,
            @Value("${app.mail.from-name:Lumiere Salon}") String fromName,
            @Value("${app.email.provider:brevo}") String emailProvider,
            @Value("${app.email.brevo.api-key:}") String brevoApiKey
    ) {
        this.objectMapper = objectMapper;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(CONNECT_TIMEOUT)
                .build();
        this.appBaseUrl = appBaseUrl;
        this.fromAddress = fromAddress;
        this.fromName = fromName;
        this.emailProvider = emailProvider;
        this.brevoApiKey = brevoApiKey;
    }

    public PasswordSetupEmailResult sendPasswordSetupEmail(
            String recipientEmail,
            String recipientName,
            String accountType,
            PasswordSetupToken setupToken
    ) {
        String setupLink = buildPasswordSetupLink(accountType, setupToken.rawToken());
        String subject = "Set up your Lumiere Salon password";
        String textBody = buildPasswordSetupMessage(recipientName, accountType, setupLink, setupToken.expiresAt());
        String htmlBody = buildPasswordSetupHtmlMessage(recipientName, accountType, setupLink, setupToken.expiresAt());

        return new PasswordSetupEmailResult(sendEmail(recipientEmail, recipientName, subject, htmlBody, textBody, "password setup"));
    }

    public boolean sendPasswordResetEmail(
            String recipientEmail,
            String recipientName,
            String accountType,
            PasswordResetToken resetToken
    ) {
        String resetLink = buildPasswordResetLink(resetToken.rawToken());
        String subject = "Reset your Lumiere Salon password";
        String textBody = buildPasswordResetMessage(recipientName, accountType, resetLink, resetToken.expiresAt());
        String htmlBody = buildPasswordResetHtmlMessage(recipientName, accountType, resetLink, resetToken.expiresAt());

        return sendEmail(recipientEmail, recipientName, subject, htmlBody, textBody, "password reset");
    }

    private boolean sendEmail(
            String recipientEmail,
            String recipientName,
            String subject,
            String htmlBody,
            String textBody,
            String emailPurpose
    ) {
        if (!BREVO_PROVIDER.equalsIgnoreCase(normalizedProvider())) {
            logger.warn("{} email could not be sent to {}: unsupported EMAIL_PROVIDER '{}'.", emailPurpose, recipientEmail, emailProvider);
            return false;
        }
        if (!hasText(brevoApiKey)) {
            logger.warn("{} email could not be sent to {}: BREVO_API_KEY is not configured.", emailPurpose, recipientEmail);
            return false;
        }
        if (!hasText(fromAddress)) {
            logger.warn("{} email could not be sent to {}: MAIL_FROM is not configured.", emailPurpose, recipientEmail);
            return false;
        }

        try {
            String requestBody = objectMapper.writeValueAsString(new BrevoEmailRequest(
                    new BrevoSender(displayFromName(), fromAddress.trim()),
                    List.of(new BrevoRecipient(recipientEmail, displayRecipientName(recipientName))),
                    subject,
                    htmlBody,
                    textBody
            ));
            HttpRequest request = HttpRequest.newBuilder(BREVO_SEND_EMAIL_URI)
                    .timeout(REQUEST_TIMEOUT)
                    .header("accept", "application/json")
                    .header("api-key", brevoApiKey.trim())
                    .header("content-type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() >= 200 && response.statusCode() < 300) {
                return true;
            }

            logger.warn("{} email could not be sent to {} via Brevo: HTTP status {}.", emailPurpose, recipientEmail, response.statusCode());
            return false;
        } catch (JacksonException ex) {
            logger.warn("{} email could not be sent to {}: email request could not be prepared.", emailPurpose, recipientEmail);
            return false;
        } catch (IOException ex) {
            logger.warn("{} email could not be sent to {} via Brevo: {}.", emailPurpose, recipientEmail, ex.getClass().getSimpleName());
            return false;
        } catch (InterruptedException ex) {
            Thread.currentThread().interrupt();
            logger.warn("{} email sending was interrupted for {}.", emailPurpose, recipientEmail);
            return false;
        }
    }

    private String buildPasswordSetupLink(String accountType, String rawToken) {
        // This URL carries the only copy of the raw setup token. Avoid writing the returned
        // link to admin pages or logs because anyone with it can set the account password.
        return UriComponentsBuilder.fromUriString(normalizedBaseUrl())
                .path("/password-setup")
                .queryParam("type", accountType.toLowerCase(Locale.ROOT))
                .queryParam("token", rawToken)
                .toUriString();
    }

    private String buildPasswordResetLink(String rawToken) {
        // This URL carries the only copy of the raw reset token. It is intentionally sent by
        // email and validated by hash when the reset page is opened.
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

    private String normalizedProvider() {
        return hasText(emailProvider) ? emailProvider.trim() : BREVO_PROVIDER;
    }

    private String displayFromName() {
        return hasText(fromName) ? fromName.trim() : "Lumiere Salon";
    }

    private String displayRecipientName(String recipientName) {
        return hasText(recipientName) ? recipientName.trim() : "";
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

    private String buildPasswordSetupHtmlMessage(String recipientName, String accountType, String setupLink, LocalDateTime expiresAt) {
        String displayName = recipientName == null || recipientName.isBlank() ? "there" : recipientName.trim();
        String displayAccountType = accountType == null || accountType.isBlank() ? "salon" : accountType.toLowerCase(Locale.ROOT);
        return "<p>Hello " + escapeHtml(displayName) + ",</p>"
                + "<p>Your " + escapeHtml(displayAccountType) + " account for Lumiere Salon has been created.</p>"
                + "<p>Please set your password using this link:<br>"
                + "<a href=\"" + escapeHtml(setupLink) + "\">Set your password</a></p>"
                + "<p>This link expires at " + escapeHtml(EXPIRY_FORMATTER.format(expiresAt)) + ".</p>"
                + "<p>Do not share this link with anyone. Anyone with access to it can set your password until it expires.</p>"
                + "<p>Thank you,<br>Lumiere Salon</p>";
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

    private String buildPasswordResetHtmlMessage(String recipientName, String accountType, String resetLink, LocalDateTime expiresAt) {
        String displayName = recipientName == null || recipientName.isBlank() ? "there" : recipientName.trim();
        String displayAccountType = accountType == null || accountType.isBlank() ? "salon" : accountType.toLowerCase(Locale.ROOT);
        return "<p>Hello " + escapeHtml(displayName) + ",</p>"
                + "<p>We received a request to reset the password for your " + escapeHtml(displayAccountType) + " account at Lumiere Salon.</p>"
                + "<p>Reset your password using this link:<br>"
                + "<a href=\"" + escapeHtml(resetLink) + "\">Reset your password</a></p>"
                + "<p>This link expires at " + escapeHtml(EXPIRY_FORMATTER.format(expiresAt)) + ".</p>"
                + "<p>If you did not request this password reset, you can ignore this email.<br>"
                + "Do not share this link with anyone. Anyone with access to it can reset your password until it expires.</p>"
                + "<p>Thank you,<br>Lumiere Salon</p>";
    }

    private String escapeHtml(String value) {
        if (value == null) {
            return "";
        }
        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private record BrevoEmailRequest(
            BrevoSender sender,
            List<BrevoRecipient> to,
            String subject,
            String htmlContent,
            String textContent
    ) {
    }

    private record BrevoSender(String name, String email) {
    }

    private record BrevoRecipient(String email, String name) {
    }
}
