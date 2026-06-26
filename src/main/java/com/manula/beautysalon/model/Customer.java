package com.manula.beautysalon.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;

import java.time.LocalDateTime;

@Entity
@Table(name = "customers", uniqueConstraints = {
        @UniqueConstraint(name = "uk_customers_email", columnNames = "email")
})
public class Customer extends User {

    private String customerType;

    // Admin-created customers do not receive temporary passwords; this stores the hashed
    // first-time setup token until the customer chooses their own password.
    @Column(name = "password_setup_token_hash", length = 128)
    private String passwordSetupTokenHash;

    // Setup links expire so an unused account invitation cannot remain valid forever.
    @Column(name = "password_setup_token_expires_at")
    private LocalDateTime passwordSetupTokenExpiresAt;

    public Customer() {
        super();
    }

    public Customer(int userId, String name, String email, String password, String customerType) {
        super(userId, name, email, password);
        this.customerType = customerType;
    }

    public String getCustomerType() {
        return customerType;
    }

    public void setCustomerType(String customerType) {
        this.customerType = customerType;
    }

    public String getPasswordSetupTokenHash() {
        return passwordSetupTokenHash;
    }

    public void setPasswordSetupTokenHash(String passwordSetupTokenHash) {
        this.passwordSetupTokenHash = passwordSetupTokenHash;
    }

    public LocalDateTime getPasswordSetupTokenExpiresAt() {
        return passwordSetupTokenExpiresAt;
    }

    public void setPasswordSetupTokenExpiresAt(LocalDateTime passwordSetupTokenExpiresAt) {
        this.passwordSetupTokenExpiresAt = passwordSetupTokenExpiresAt;
    }

    public boolean isPasswordSetupRequired() {
        return !isPasswordSet();
    }

    public void clearPasswordSetupToken() {
        this.passwordSetupTokenHash = null;
        this.passwordSetupTokenExpiresAt = null;
    }

    @Override
    public String getWelcomeMessage() {
        if ("Premium".equalsIgnoreCase(customerType)) {
            return "Welcome back, Premium Client! Enjoy your Luxe Priority service.";
        }
        return "Welcome, Regular Client! Thank you for choosing Lumiere Salon.";
    }

    public String getStatusBadge() {
        if ("Premium".equalsIgnoreCase(customerType)) {
            return "Premium";
        }
        return "Regular";
    }
}
