package com.manula.beautysalon.model;


public class PackageService extends SalonService {

    // Define the discount rate as a constant (No more magic numbers!)
    private static final double PACKAGE_DISCOUNT_RATE = 0.10;

    // Updated constructor
    public PackageService(int serviceId, String name, String description, double basePrice, String imageFileName, String stylistName) {
        super(serviceId, name, description, basePrice, imageFileName, stylistName); // Passes it to SalonService
    }

    @Override
    public double calculateFinalPrice() {
        // Apply the constant discount rate directly
        return getBasePrice() * (1 - PACKAGE_DISCOUNT_RATE);
    }
}
