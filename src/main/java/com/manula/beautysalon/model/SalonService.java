package com.manula.beautysalon.model;

import jakarta.persistence.Column;
import jakarta.persistence.DiscriminatorColumn;
import jakarta.persistence.DiscriminatorType;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Inheritance;
import jakarta.persistence.InheritanceType;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

@Entity
@Table(name = "salon_services")
// Service type is stored in one table so standard services and packages share the same
// listing/booking workflow while still calculating prices differently.
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "service_type", discriminatorType = DiscriminatorType.STRING)
public abstract class SalonService {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "service_id")
    private int serviceId;

    @Column(nullable = false)
    private String name;

    @Lob
    private String description;

    private double basePrice;
    private String imageFileName;
    // Stored as a name because the storefront shows a human assignment and old services may
    // outlive the stylist account that originally provided them.
    private String stylistName;
    // Inactive services are hidden from new bookings but retained when history references them.
    private Boolean active = true;

    protected SalonService() {
    }

    public SalonService(int serviceId, String name, String description, double basePrice, String imageFileName, String stylistName) {
        this.serviceId = serviceId;
        this.name = name;
        this.description = description;
        this.basePrice = basePrice;
        this.imageFileName = imageFileName;
        this.stylistName = stylistName;
        this.active = true;
    }

    @PrePersist
    @PreUpdate
    private void applyDefaults() {
        if (active == null) {
            active = true;
        }
    }

    public int getServiceId() {
        return serviceId;
    }

    public void setServiceId(int serviceId) {
        this.serviceId = serviceId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getBasePrice() {
        return basePrice;
    }

    public void setBasePrice(double basePrice) {
        this.basePrice = basePrice;
    }

    public String getImageFileName() {
        return imageFileName;
    }

    public void setImageFileName(String imageFileName) {
        this.imageFileName = imageFileName;
    }

    public String getStylistName() {
        return stylistName;
    }

    public void setStylistName(String stylistName) {
        this.stylistName = stylistName;
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

    public String getServiceType() {
        return this instanceof PackageService ? "Package" : "Standard";
    }

    public abstract double calculateFinalPrice();
}
