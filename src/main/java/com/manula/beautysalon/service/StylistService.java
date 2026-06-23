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

@Service
public class StylistService {

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
}
