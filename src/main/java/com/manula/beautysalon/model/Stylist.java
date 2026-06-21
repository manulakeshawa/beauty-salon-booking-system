package com.manula.beautysalon.model;

public class Stylist extends StaffMember {

    private String specialty;
    private String level;
    private boolean available;
    private String imageFileName;

    public Stylist(int userId, String name, String email, String password, String specialty, String level, boolean available, String imageFileName) {
        super(userId, name, email, password);
        this.specialty = specialty;
        this.level = level;
        this.available = available;
        this.imageFileName = imageFileName;
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

    // FIXED: Stripped raw HTML. Now returns pure string data for the View to handle.
    @Override
    public String getDisplayBadge() {
        return (level != null && !level.isEmpty())
                ? level.substring(0, 1).toUpperCase() + level.substring(1).toLowerCase()
                : "Junior";
    }

    public String getAvailabilityStatus() {
        return available ? "Available" : "Unavailable";
    }

    // FIXED: Stripped HTML. Now returns pure string data.
    @Override
    public String getDirectoryLevel() {
        return (level != null && !level.isEmpty()) ? level : "Stylist";
    }
}
