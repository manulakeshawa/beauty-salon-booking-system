package com.example.beautysalonreview.model;

import com.example.beautysalonreview.model.*;
import com.example.beautysalonreview.controller.*;
import com.example.beautysalonreview.repository.*;
import com.example.beautysalonreview.util.*;




public class StandardService extends SalonService  {

    // Updated constructor
    public StandardService(int serviceId, String name, String description, double basePrice, String imageFileName, String stylistName) {

        // Calls the parent class constructor and passes all service details
        super(serviceId, name, description, basePrice, imageFileName, stylistName);
    }

    @Override
    public double calculateFinalPrice() {
        return getBasePrice();
    }

}
