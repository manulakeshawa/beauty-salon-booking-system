package com.example.beautysalonreview;

public class Stylist extends User {

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

    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

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
        // Uses a switch statement to give each level a unique, premium description
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

    public String getDisplayBadge() {
        // Dynamically injects whatever the level is straight into the HTML badge
        // We use a quick check to make sure it's capitalized properly (e.g., "Master Stylist")
        String displayLevel = (level != null && !level.isEmpty()) 
                ? level.substring(0, 1).toUpperCase() + level.substring(1).toLowerCase() 
                : "Junior";
                
        return "<span class='badge rounded-pill badge-lumiere'>" + displayLevel + " Stylist</span>";
    }

    public String getAvailabilityStatus() {
        return available ? "Available" : "Unavailable";
    }
}