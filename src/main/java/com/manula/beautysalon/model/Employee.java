package com.manula.beautysalon.model;

public class Employee extends StaffMember {

    private String username;
    private String role;           // MANAGER or STYLIST
    private String level;          // Junior, Senior, Lead, etc.
    private String specialty;
    private String welcomeMessage;
    private String availabilityStatus;

    public Employee(int userId, String username, String password, String fullName,
                    String email, String role, String level, String specialty,
                    String welcomeMessage, String availabilityStatus) {

        super(userId, fullName, email, password);

        this.username = username;
        this.role = role;
        this.level = level;
        this.specialty = specialty;
        this.welcomeMessage = welcomeMessage;
        this.availabilityStatus = availabilityStatus;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getFullName() {
        return super.getName();
    }

    public void setFullName(String fullName) {
        super.setName(fullName);
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }

    public String getAvailabilityStatus() {
        return availabilityStatus;
    }

    public void setAvailabilityStatus(String availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    @Override
    public String getLevel() {
        return (level == null || level.trim().isEmpty()) ? "Stylist" : level;
    }

    public void setLevel(String level) {
        this.level = level;
    }

    @Override
    public String getSpecialty() {
        return specialty;
    }

    public void setSpecialty(String specialty) {
        this.specialty = specialty;
    }

    @Override
    public String getWelcomeMessage() {
        return welcomeMessage;
    }

    public void setWelcomeMessage(String welcomeMessage) {
        this.welcomeMessage = welcomeMessage;
    }

    // FIXED: Stripped HTML. Now returns pure string data for the View to handle.
    @Override
    public String getDisplayBadge() {
        if ("MANAGER".equalsIgnoreCase(role)) {
            return "Manager";
        }
        return getLevel();
    }

    // FIXED: Stripped HTML. Now returns pure string data for the View to handle.
    @Override
    public String getDirectoryLevel() {
        if ("MANAGER".equalsIgnoreCase(role)) {
            return "Manager";
        }
        return getLevel();
    }
}
