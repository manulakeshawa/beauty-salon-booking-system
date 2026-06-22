package com.manula.beautysalon.model;

import jakarta.persistence.MappedSuperclass;

/**
 * Abstract base class for all salon staff.
 * Bridges the gap between the base User class and specific staff roles,
 * fulfilling the strict OOP hierarchy requirements.
 */
@MappedSuperclass
public abstract class StaffMember extends User {

    protected StaffMember() {
        super();
    }

    public StaffMember(int userId, String name, String email, String password) {
        super(userId, name, email, password);
    }

    public abstract String getSpecialty();

    public abstract String getLevel();

    public abstract String getWelcomeMessage();

    public abstract String getDisplayBadge();

    public abstract String getDirectoryLevel();
}
