package com.example.beautysalonreview;

public class PackageService extends SalonService {

    // Updated constructor
    public PackageService(int serviceId, String name, String description, double basePrice, String imageFileName) {
        super(serviceId, name, description, basePrice, imageFileName); // Passes it to SalonService
    }

    @Override
    public double calculateFinalPrice() {
        double discountRate = 0.10;
        return getBasePrice() * (1 - discountRate);
    }
}