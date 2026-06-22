package com.manula.beautysalon.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("Package")
public class PackageService extends SalonService {

    private static final double PACKAGE_DISCOUNT_RATE = 0.10;

    public PackageService() {
        super();
    }

    public PackageService(int serviceId, String name, String description, double basePrice, String imageFileName, String stylistName) {
        super(serviceId, name, description, basePrice, imageFileName, stylistName);
    }

    @Override
    public double calculateFinalPrice() {
        return getBasePrice() * (1 - PACKAGE_DISCOUNT_RATE);
    }
}
