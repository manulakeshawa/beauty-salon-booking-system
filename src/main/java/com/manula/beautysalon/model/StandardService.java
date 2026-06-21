package com.manula.beautysalon.model;

public class StandardService extends SalonService {

    // Updated constructor
    public StandardService(int serviceId, String name, String description, double basePrice, String imageFileName, String stylistName) {
        super(serviceId, name, description, basePrice, imageFileName, stylistName); // Passes it to SalonService
    }

    @Override
    public double calculateFinalPrice() {
        return getBasePrice();
    }
}
