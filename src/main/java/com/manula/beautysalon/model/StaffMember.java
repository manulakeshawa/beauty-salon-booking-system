package com.manula.beautysalon.model;

/**
 * Abstract base class for all salon staff.
 * Bridges the gap between the base User class and specific staff roles,
 * fulfilling the strict OOP hierarchy requirements.
 */
public abstract class StaffMember extends User {

    // Passes the core data up to the User class
    public StaffMember(int userId, String name, String email, String password) {
        super(userId, name, email, password);
    }

    // Abstract methods force all child classes to implement these exact features
    public abstract String getSpecialty();

    public abstract String getLevel();

    public abstract String getWelcomeMessage();

    public abstract String getDisplayBadge();

    public abstract String getDirectoryLevel();
}
