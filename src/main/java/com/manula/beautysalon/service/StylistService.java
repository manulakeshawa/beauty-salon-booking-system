package com.manula.beautysalon.service;

import com.manula.beautysalon.model.Stylist;
import com.manula.beautysalon.repository.AppointmentRepository;
import com.manula.beautysalon.repository.ReviewRepository;
import com.manula.beautysalon.repository.SalonServiceRepository;
import com.manula.beautysalon.repository.StylistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class StylistService {

    private final StylistRepository stylistRepository;
    private final AppointmentRepository appointmentRepository;
    private final ReviewRepository reviewRepository;
    private final SalonServiceRepository salonServiceRepository;

    public StylistService(StylistRepository stylistRepository, AppointmentRepository appointmentRepository, ReviewRepository reviewRepository, SalonServiceRepository salonServiceRepository) {
        this.stylistRepository = stylistRepository;
        this.appointmentRepository = appointmentRepository;
        this.reviewRepository = reviewRepository;
        this.salonServiceRepository = salonServiceRepository;
    }

    public Stylist saveStylist(Stylist stylist) {
        stylist.setUserId(0);
        stylist.setActive(true);
        return stylistRepository.save(stylist);
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

            if (identifierMatches && stylist.isActive() && stylist.getPassword().equals(password)) {
                return stylist;
            }
        }
        return null;
    }

    @Transactional
    public void updateStylist(Stylist updatedStylist) {
        stylistRepository.findById(updatedStylist.getUserId()).ifPresent(existing -> {
            String previousName = existing.getName();
            existing.setName(updatedStylist.getName());
            existing.setEmail(updatedStylist.getEmail());
            existing.setPassword(updatedStylist.getPassword());
            existing.setSpecialty(updatedStylist.getSpecialty());
            existing.setLevel(updatedStylist.getLevel());
            existing.setAvailable(updatedStylist.isAvailable());
            existing.setImageFileName(updatedStylist.getImageFileName());
            stylistRepository.save(existing);

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
        return stylistRepository.findByEmailIgnoreCase(email).orElse(null);
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
