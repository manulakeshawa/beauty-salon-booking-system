package com.manula.beautysalon.service;

import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class PasswordSetupTokenService {

    private static final int TOKEN_BYTES = 32;
    private static final long EXPIRY_HOURS = 48;

    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordSetupToken generateToken() {
        // First-time setup links are bearer-token links for admin-created accounts that do
        // not have passwords yet. Treat the raw token like a password while it is valid.
        byte[] tokenBytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(tokenBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        return new PasswordSetupToken(rawToken, LocalDateTime.now().plusHours(EXPIRY_HOURS));
    }

    public String hashToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return null;
        }
        try {
            // Only the token hash is stored in the database; the raw token appears only in
            // the email link and should not be exposed in admin screens or logs.
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Password setup token hashing is unavailable.", ex);
        }
    }

    public boolean isExpired(LocalDateTime expiresAt) {
        return expiresAt == null || expiresAt.isBefore(LocalDateTime.now());
    }
}
