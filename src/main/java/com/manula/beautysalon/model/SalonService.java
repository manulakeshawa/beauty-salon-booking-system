package com.manula.beautysalon.model;

public abstract class SalonService {

    private int serviceId;
    private String name;
    private String description;
    private double basePrice;
    private String imageFileName;
    private String stylistName; // NEW: Added link to the Stylist

    // Updated Constructor
    public SalonService(int serviceId, String name, String description, double basePrice, String imageFileName, String stylistName) {
        this.serviceId = serviceId;
        this.name = name;
        this.description = description;
        this.basePrice = basePrice;
        this.imageFileName = imageFileName;
        this.stylistName = stylistName; // NEW
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

    // NEW: Getter and Setter for Stylist Name
    public String getStylistName() {
        return stylistName;
    }

    public void setStylistName(String stylistName) {
        this.stylistName = stylistName;
    }

    public abstract double calculateFinalPrice();
}
