package com.manula.beautysalon.service;

import com.manula.beautysalon.model.Stylist;
import com.manula.beautysalon.repository.AppointmentRepository;
import com.manula.beautysalon.repository.ReviewRepository;
import com.manula.beautysalon.repository.SalonServiceRepository;
import com.manula.beautysalon.repository.StylistRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class StylistService {

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    public static final String PASSWORD_SETUP_PENDING_LOGIN_MESSAGE =
            "Your account exists, but a password has not been set yet. Please use your password setup link.";

    private final StylistRepository stylistRepository;
    private final AppointmentRepository appointmentRepository;
    private final ReviewRepository reviewRepository;
    private final SalonServiceRepository salonServiceRepository;
    private final AccountEmailService accountEmailService;
    private final PasswordService passwordService;
    private final PasswordSetupTokenService passwordSetupTokenService;

    public StylistService(StylistRepository stylistRepository, AppointmentRepository appointmentRepository, ReviewRepository reviewRepository, SalonServiceRepository salonServiceRepository, AccountEmailService accountEmailService, PasswordService passwordService, PasswordSetupTokenService passwordSetupTokenService) {
        this.stylistRepository = stylistRepository;
        this.appointmentRepository = appointmentRepository;
        this.reviewRepository = reviewRepository;
        this.salonServiceRepository = salonServiceRepository;
        this.accountEmailService = accountEmailService;
        this.passwordService = passwordService;
        this.passwordSetupTokenService = passwordSetupTokenService;
    }

    @Transactional
    public Stylist saveStylist(Stylist stylist) {
        stylist.setUserId(0);
        stylist.setEmail(accountEmailService.normalize(stylist.getEmail()));
        stylist.setPassword(passwordService.hashIfPlainText(stylist.getPassword()));
        stylist.setActive(true);
        accountEmailService.assertStylistEmailAvailable(stylist.getEmail(), stylist.getUserId());
        try {
            return stylistRepository.save(stylist);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateEmailException(AccountEmailService.DUPLICATE_EMAIL_MESSAGE, ex);
        }
    }

    @Transactional
    public PasswordSetupToken saveAdminCreatedStylist(Stylist stylist) {
        stylist.setUserId(0);
        validateProfile(stylist.getName(), stylist.getEmail());
        stylist.setEmail(accountEmailService.normalize(stylist.getEmail()));
        // Admin-created stylist accounts use first-time setup instead of temporary passwords,
        // so no shared or emailed plain-text password is stored here.
        stylist.setPassword("");
        stylist.setActive(true);
        accountEmailService.assertStylistEmailAvailable(stylist.getEmail(), stylist.getUserId());
        PasswordSetupToken token = assignPasswordSetupToken(stylist);
        try {
            stylistRepository.save(stylist);
            return token;
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateEmailException(AccountEmailService.DUPLICATE_EMAIL_MESSAGE, ex);
        }
    }

    public List<Stylist> readAllStylists() {
        return readActiveStylists();
    }

    public List<Stylist> readActiveStylists() {
        return stylistRepository.findActiveOrderByUserIdAsc();
    }

    public List<Stylist> readActiveAvailableStylists() {
        return stylistRepository.findActiveAndAvailableOrderByUserIdAsc();
    }

    public List<Stylist> readAllStylistsIncludingInactive() {
        return stylistRepository.findAllByOrderByUserIdAsc();
    }

    public Stylist authenticate(String identifier, String password) {
        if (identifier == null || password == null) {
            return null;
        }

        Stylist stylist = findActiveByIdentifier(identifier);
        if (stylist != null && passwordService.matches(password, stylist.getPassword())) {
            return stylist;
        }
        return null;
    }

    @Transactional
    public void updateStylist(Stylist updatedStylist) {
        stylistRepository.findById(updatedStylist.getUserId()).ifPresent(existing -> {
            String previousName = existing.getName();
            String normalizedEmail = accountEmailService.normalize(updatedStylist.getEmail());
            accountEmailService.assertStylistEmailAvailable(normalizedEmail, updatedStylist.getUserId());
            existing.setName(updatedStylist.getName());
            existing.setEmail(normalizedEmail);
            existing.setPassword(passwordService.hashIfPlainText(updatedStylist.getPassword()));
            existing.setSpecialty(updatedStylist.getSpecialty());
            existing.setLevel(updatedStylist.getLevel());
            existing.setAvailable(updatedStylist.isAvailable());
            existing.setImageFileName(updatedStylist.getImageFileName());
            try {
                stylistRepository.save(existing);
            } catch (DataIntegrityViolationException ex) {
                throw new DuplicateEmailException(AccountEmailService.DUPLICATE_EMAIL_MESSAGE, ex);
            }

            if (hasText(previousName) && hasText(updatedStylist.getName())
                    && !previousName.equalsIgnoreCase(updatedStylist.getName())) {
                // The booking and review records keep stylist names as business history, and
                // service listings also show the assigned stylist by name.
                appointmentRepository.updateStylistNameIgnoreCase(previousName, updatedStylist.getName());
                reviewRepository.updateStylistNameIgnoreCase(previousName, updatedStylist.getName());
                salonServiceRepository.updateStylistNameIgnoreCase(previousName, updatedStylist.getName());
            }
        });
    }

    @Transactional
    public Stylist updateStylistProfile(String currentEmail, String name, String email) {
        Stylist existing = findByEmail(currentEmail);
        if (existing == null) {
            throw new IllegalArgumentException("Your stylist account could not be found. Please sign in again.");
        }
        validateProfile(name, email);

        String previousName = existing.getName();
        String normalizedEmail = accountEmailService.normalize(email);
        accountEmailService.assertStylistEmailAvailable(normalizedEmail, existing.getUserId());

        existing.setName(name.trim());
        existing.setEmail(normalizedEmail);
        try {
            stylistRepository.save(existing);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateEmailException(AccountEmailService.DUPLICATE_EMAIL_MESSAGE, ex);
        }

        if (hasText(previousName) && !previousName.equalsIgnoreCase(existing.getName())) {
            // Keep past appointments, reviews, and assigned service cards aligned when a
            // stylist updates their public display name.
            appointmentRepository.updateStylistNameIgnoreCase(previousName, existing.getName());
            reviewRepository.updateStylistNameIgnoreCase(previousName, existing.getName());
            salonServiceRepository.updateStylistNameIgnoreCase(previousName, existing.getName());
        }

        return existing;
    }

    @Transactional
    public void changeStylistPassword(String email, String currentPassword, String newPassword, String confirmPassword) {
        Stylist stylist = findByEmail(email);
        if (stylist == null) {
            throw new IllegalArgumentException("Your stylist account could not be found. Please sign in again.");
        }
        validatePasswordChange(currentPassword, newPassword, confirmPassword, stylist.getPassword());
        stylist.setPassword(passwordService.hash(newPassword));
        stylistRepository.save(stylist);
    }

    @Transactional
    public void deleteStylist(int userId) {
        stylistRepository.findById(userId).ifPresent(stylist -> {
            if (hasText(stylist.getName())) {
                // Services can remain bookable even after a stylist leaves, so detach the
                // assignment instead of deleting the service with the stylist.
                salonServiceRepository.updateStylistNameIgnoreCase(stylist.getName(), "Unassigned");
            }

            if (hasStylistHistory(stylist.getName())) {
                // Preserve stylist records that appear in past appointments or reviews; hiding
                // them from active lists keeps history intact without offering new bookings.
                stylist.setActive(false);
                stylist.setAvailable(false);
                stylistRepository.save(stylist);
                return;
            }

            stylistRepository.delete(stylist);
        });
    }

    public Stylist findById(int userId) {
        return stylistRepository.findById(userId).orElse(null);
    }

    public Stylist findByEmail(String email) {
        if (email == null) {
            return null;
        }
        return stylistRepository.findByEmailIgnoreCase(accountEmailService.normalize(email)).orElse(null);
    }

    public boolean isPasswordSetupPending(String identifier) {
        Stylist stylist = findActiveByIdentifier(identifier);
        return stylist != null && stylist.isPasswordSetupRequired();
    }

    public Stylist previewPasswordSetup(String rawToken) {
        return requireValidPasswordSetupStylist(rawToken);
    }

    @Transactional
    public Stylist setupPassword(String rawToken, String newPassword, String confirmPassword) {
        Stylist stylist = requireValidPasswordSetupStylist(rawToken);
        validateNewPassword(newPassword, confirmPassword);
        stylist.setPassword(passwordService.hash(newPassword));
        stylist.clearPasswordSetupToken();
        return stylistRepository.save(stylist);
    }

    @Transactional
    public PasswordSetupToken regeneratePasswordSetupToken(int userId) {
        Stylist stylist = stylistRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Stylist account could not be found."));
        if (stylist.isPasswordSet()) {
            throw new IllegalArgumentException("This stylist already has a password set.");
        }
        PasswordSetupToken token = assignPasswordSetupToken(stylist);
        stylistRepository.save(stylist);
        return token;
    }

    public int generateNextStylistId() {
        return stylistRepository.findTopByOrderByUserIdDesc()
                .map(stylist -> stylist.getUserId() + 1)
                .orElse(1);
    }

    private boolean hasStylistHistory(String stylistName) {
        // Historical appointments/reviews are the boundary between hard delete and inactivation.
        return hasText(stylistName)
                && (appointmentRepository.existsByStylistNameIgnoreCase(stylistName)
                || reviewRepository.existsByStylistNameIgnoreCase(stylistName));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private void validateProfile(String name, String email) {
        if (!hasText(name)) {
            throw new IllegalArgumentException("Please enter your name.");
        }
        if (!hasText(email) || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("Please enter a valid email address.");
        }
    }

    private void validatePasswordChange(String currentPassword, String newPassword, String confirmPassword, String storedPasswordHash) {
        if (!hasText(currentPassword)) {
            throw new IllegalArgumentException("Please enter your current password.");
        }
        if (!passwordService.matches(currentPassword, storedPasswordHash)) {
            throw new IllegalArgumentException("The current password you entered is incorrect.");
        }
        validateNewPassword(newPassword, confirmPassword);
    }

    private void validateNewPassword(String newPassword, String confirmPassword) {
        if (!hasText(newPassword) || newPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("New password must be at least " + MIN_PASSWORD_LENGTH + " characters long.");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("New password and confirm password must match.");
        }
    }

    private PasswordSetupToken assignPasswordSetupToken(Stylist stylist) {
        PasswordSetupToken token = passwordSetupTokenService.generateToken();
        // Persist the hash and expiry, not the raw setup token. The raw token belongs only in
        // the email link and is invalidated when setupPassword clears it after use.
        stylist.setPasswordSetupTokenHash(passwordSetupTokenService.hashToken(token.rawToken()));
        stylist.setPasswordSetupTokenExpiresAt(token.expiresAt());
        return token;
    }

    private Stylist requireValidPasswordSetupStylist(String rawToken) {
        if (!hasText(rawToken)) {
            throw new IllegalArgumentException("This password setup link is invalid or has already been used.");
        }
        String tokenHash = passwordSetupTokenService.hashToken(rawToken);
        Stylist stylist = stylistRepository.findByPasswordSetupTokenHash(tokenHash).orElse(null);
        if (stylist == null || stylist.isPasswordSet()) {
            throw new IllegalArgumentException("This password setup link is invalid or has already been used.");
        }
        if (passwordSetupTokenService.isExpired(stylist.getPasswordSetupTokenExpiresAt())) {
            throw new IllegalArgumentException("This password setup link has expired. Please ask an administrator to regenerate it.");
        }
        return stylist;
    }

    private Stylist findActiveByIdentifier(String identifier) {
        if (!hasText(identifier)) {
            return null;
        }

        String trimmedIdentifier = identifier.trim();
        for (Stylist stylist : readActiveStylists()) {
            String idString = String.valueOf(stylist.getUserId());
            boolean identifierMatches = equalsIgnoreCase(stylist.getName(), trimmedIdentifier)
                    || equalsIgnoreCase(stylist.getEmail(), trimmedIdentifier)
                    || idString.equals(trimmedIdentifier);

            if (identifierMatches && stylist.isActive()) {
                return stylist;
            }
        }
        return null;
    }

    private boolean equalsIgnoreCase(String left, String right) {
        return left != null && right != null && left.equalsIgnoreCase(right);
    }
}
