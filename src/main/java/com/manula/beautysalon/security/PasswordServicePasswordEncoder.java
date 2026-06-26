package com.manula.beautysalon.security;

import com.manula.beautysalon.service.PasswordService;
import org.springframework.security.crypto.password.PasswordEncoder;

public class PasswordServicePasswordEncoder implements PasswordEncoder {

    private final PasswordService passwordService;

    // Adapter used by Spring Security so framework login checks use the application's
    // PBKDF2 password format instead of a separate encoder configuration.
    public PasswordServicePasswordEncoder(PasswordService passwordService) {
        this.passwordService = passwordService;
    }

    @Override
    public String encode(CharSequence rawPassword) {
        return passwordService.hash(rawPassword == null ? null : rawPassword.toString());
    }

    @Override
    public boolean matches(CharSequence rawPassword, String encodedPassword) {
        return passwordService.matches(rawPassword == null ? null : rawPassword.toString(), encodedPassword);
    }
}
