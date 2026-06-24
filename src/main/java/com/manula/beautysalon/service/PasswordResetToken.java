package com.manula.beautysalon.service;

import java.time.LocalDateTime;

public record PasswordResetToken(String rawToken, LocalDateTime expiresAt) {
}
