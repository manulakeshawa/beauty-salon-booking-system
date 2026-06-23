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

    private final StylistRepository stylistRepository;
    private final AppointmentRepository appointmentRepository;
    private final ReviewRepository reviewRepository;
    private final SalonServiceRepository salonServiceRepository;
    private final AccountEmailService accountEmailService;
    private final PasswordService passwordService;

    public StylistService(StylistRepository stylistRepository, AppointmentRepository appointmentRepository, ReviewRepository reviewRepository, SalonServiceRepository salonServiceRepository, AccountEmailService accountEmailService, PasswordService passwordService) {
        this.stylistRepository = stylistRepository;
        this.appointmentRepository = appointmentRepository;
        this.reviewRepository = reviewRepository;
        this.salonServiceRepository = salonServiceRepository;
        this.accountEmailService = accountEmailService;
        this.passwordService = passwordService;
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

        String trimmedIdentifier = identifier.trim();
        for (Stylist stylist : readActiveStylists()) {
            String idString = String.valueOf(stylist.getUserId());
            boolean identifierMatches = stylist.getName().equalsIgnoreCase(trimmedIdentifier)
                    || stylist.getEmail().equalsIgnoreCase(trimmedIdentifier)
                    || idString.equals(trimmedIdentifier);

            if (identifierMatches && stylist.isActive() && passwordService.matches(password, stylist.getPassword())) {
                return stylist;
            }
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
                salonServiceRepository.updateStylistNameIgnoreCase(stylist.getName(), "Unassigned");
            }

            if (hasStylistHistory(stylist.getName())) {
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

    public int generateNextStylistId() {
        return stylistRepository.findTopByOrderByUserIdDesc()
                .map(stylist -> stylist.getUserId() + 1)
                .orElse(1);
    }

    private boolean hasStylistHistory(String stylistName) {
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
        if (!hasText(newPassword) || newPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("New password must be at least " + MIN_PASSWORD_LENGTH + " characters long.");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("New password and confirm password must match.");
        }
    }
}
