package com.manula.beautysalon.service;

import org.springframework.stereotype.Service;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.LocalDateTime;
import java.util.Base64;

@Service
public class PasswordResetTokenService {

    private static final int TOKEN_BYTES = 32;
    private static final long EXPIRY_MINUTES = 60;

    private final SecureRandom secureRandom = new SecureRandom();

    public PasswordResetToken generateToken() {
        // Reset links are short-lived bearer-token links for users who already had a password
        // but cannot sign in. The raw token should only travel in the email URL.
        byte[] tokenBytes = new byte[TOKEN_BYTES];
        secureRandom.nextBytes(tokenBytes);
        String rawToken = Base64.getUrlEncoder().withoutPadding().encodeToString(tokenBytes);
        return new PasswordResetToken(rawToken, LocalDateTime.now().plusMinutes(EXPIRY_MINUTES));
    }

    public String hashToken(String rawToken) {
        if (rawToken == null || rawToken.isBlank()) {
            return null;
        }
        try {
            // Storing a hash lets the application validate a clicked link without keeping the
            // emailed bearer token itself in the database.
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hash = digest.digest(rawToken.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hash);
        } catch (NoSuchAlgorithmException ex) {
            throw new IllegalStateException("Password reset token hashing is unavailable.", ex);
        }
    }

    public boolean isExpired(LocalDateTime expiresAt) {
        return expiresAt == null || expiresAt.isBefore(LocalDateTime.now());
    }
}
