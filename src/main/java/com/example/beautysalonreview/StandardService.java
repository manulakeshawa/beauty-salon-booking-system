package com.example.beautysalonreview;

public class StandardService extends SalonService {

    // Updated constructor
    public StandardService(int serviceId, String name, String description, double basePrice, String imageFileName) {
        super(serviceId, name, description, basePrice, imageFileName); // Passes it to SalonService
    }

    @Override
    public double calculateFinalPrice() {
        return getBasePrice();
    }
}