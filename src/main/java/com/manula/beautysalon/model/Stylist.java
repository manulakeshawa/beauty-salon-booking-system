package com.manula.beautysalon.model;

import jakarta.persistence.Entity;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

@Entity
@Table(name = "stylists", uniqueConstraints = {
        @UniqueConstraint(name = "uk_stylists_email", columnNames = "email")
})
public class Stylist extends StaffMember {

    private String specialty;
    private String level;
    private boolean available;
    private String imageFileName;
    private Boolean active = true;

    public Stylist() {
        super();
    }

    public Stylist(int userId, String name, String email, String password, String specialty, String level, boolean available, String imageFileName) {
        super(userId, name, email, password);
        this.specialty = specialty;
        this.level = level;
        this.available = available;
        this.imageFileName = imageFileName;
        this.active = true;
    }

    @PrePersist
    @PreUpdate
    private void applyDefaults() {
        if (imageFileName == null || imageFileName.isBlank()) {
            imageFileName = "default-stylist.png";
        }
        if (active == null) {
            active = true;
        }
    }

    @Override
    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    @Override
    public String getLevel() {
        return level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
    }

    public boolean isActive() {
        return active == null || active;
    }

    public Boolean getActive() {
        return active;
    }

    public void setActive(Boolean active) {
        this.active = active;
    }

    @Override
    public String getWelcomeMessage() {
        switch (level != null ? level.toLowerCase() : "") {
            case "senior":
                return "Senior stylist ready for premium transformations.";
            case "master":
                return "Master stylist offering elite, award-winning expertise.";
            case "lead":
                return "Lead stylist directing top-tier personalized salon experiences.";
            case "junior":
            default:
                return "Junior stylist ready to deliver fresh, modern looks.";
        }
    }

    @Override
    public String getDisplayBadge() {
        return (level != null && !level.isEmpty())
                ? level.substring(0, 1).toUpperCase() + level.substring(1).toLowerCase()
                : "Junior";
    }

    public String getAvailabilityStatus() {
        if (!isActive()) {
            return "Inactive";
        }
        return available ? "Available" : "Unavailable";
    }

    @Override
    public String getDirectoryLevel() {
        return (level != null && !level.isEmpty()) ? level : "Stylist";
    }
}
