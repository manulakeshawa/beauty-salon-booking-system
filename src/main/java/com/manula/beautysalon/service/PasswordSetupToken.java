package com.manula.beautysalon.service;

import java.time.LocalDateTime;

public record PasswordSetupToken(String rawToken, LocalDateTime expiresAt) {
}
