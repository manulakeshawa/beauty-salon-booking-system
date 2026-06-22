package com.manula.beautysalon.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;

@Entity
@DiscriminatorValue("Standard")
public class StandardService extends SalonService {

    public StandardService() {
        super();
    }

    public StandardService(int serviceId, String name, String description, double basePrice, String imageFileName, String stylistName) {
        super(serviceId, name, description, basePrice, imageFileName, stylistName);
    }

    @Override
    public double calculateFinalPrice() {
        return getBasePrice();
    }
}
