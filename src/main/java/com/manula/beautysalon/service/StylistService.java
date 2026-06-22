package com.manula.beautysalon.service;

import com.manula.beautysalon.model.Stylist;
import com.manula.beautysalon.repository.StylistRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class StylistService {

    private final StylistRepository stylistRepository;

    public StylistService(StylistRepository stylistRepository) {
        this.stylistRepository = stylistRepository;
    }

    public Stylist saveStylist(Stylist stylist) {
        stylist.setUserId(0);
        return stylistRepository.save(stylist);
    }

    public List<Stylist> readAllStylists() {
        return stylistRepository.findAllByOrderByUserIdAsc();
    }

    public Stylist authenticate(String identifier, String password) {
        if (identifier == null || password == null) {
            return null;
        }

        String trimmedIdentifier = identifier.trim();
        for (Stylist stylist : readAllStylists()) {
            String idString = String.valueOf(stylist.getUserId());
            boolean identifierMatches = stylist.getName().equalsIgnoreCase(trimmedIdentifier)
                    || stylist.getEmail().equalsIgnoreCase(trimmedIdentifier)
                    || idString.equals(trimmedIdentifier);

            if (identifierMatches && stylist.getPassword().equals(password)) {
                return stylist;
            }
        }
        return null;
    }

    public void updateStylist(Stylist updatedStylist) {
        stylistRepository.findById(updatedStylist.getUserId()).ifPresent(existing -> {
            existing.setName(updatedStylist.getName());
            existing.setEmail(updatedStylist.getEmail());
            existing.setPassword(updatedStylist.getPassword());
            existing.setSpecialty(updatedStylist.getSpecialty());
            existing.setLevel(updatedStylist.getLevel());
            existing.setAvailable(updatedStylist.isAvailable());
            existing.setImageFileName(updatedStylist.getImageFileName());
            stylistRepository.save(existing);
        });
    }

    public void deleteStylist(int userId) {
        if (stylistRepository.existsById(userId)) {
            stylistRepository.deleteById(userId);
        }
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
}
