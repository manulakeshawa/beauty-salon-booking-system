package com.manula.beautysalon.model;

/**
 * Base class representing a customer review for a beauty salon service.
 */
public class Review {
    private int reviewId;
    private String customerName;
    private String serviceName;
    private String stylistName;
    private int rating;                 // Expected range: 1–5
    private String comment;
    private String ownerToken;

    // NEW: Boolean flag to determine if the review is verified by an admin
    private boolean verified;

    // Default constructor
    public Review() {
        this.ownerToken = "";
        this.verified = false; // Defaults to unverified (Public)
    }

    // Parameterized constructor (No Stylist)
    public Review(int reviewId, String customerName, String serviceName, int rating, String comment) {
        this.reviewId = reviewId;
        this.customerName = customerName;
        this.serviceName = serviceName;
        this.stylistName = "";
        this.rating = rating;
        this.comment = comment;
        this.ownerToken = "";
        this.verified = false;
    }

    // Parameterized constructor (With Stylist)
    public Review(int reviewId, String customerName, String serviceName, String stylistName, int rating, String comment) {
        this.reviewId = reviewId;
        this.customerName = customerName;
        this.serviceName = serviceName;
        this.stylistName = stylistName == null ? "" : stylistName;
        this.rating = rating;
        this.comment = comment;
        this.ownerToken = "";
        this.verified = false;
    }

    // Full constructor (Including Token but NO Verified status - for backwards compatibility)
    public Review(int reviewId, String customerName, String serviceName, String stylistName, int rating, String comment, String ownerToken) {
        this.reviewId = reviewId;
        this.customerName = customerName;
        this.serviceName = serviceName;
        this.stylistName = stylistName == null ? "" : stylistName;
        this.rating = rating;
        this.comment = comment;
        this.ownerToken = ownerToken == null ? "" : ownerToken;
        this.verified = false;
    }

    // NEW: Ultimate Full Constructor (Including Token AND Verified status)
    public Review(int reviewId, String customerName, String serviceName, String stylistName, int rating, String comment, String ownerToken, boolean verified) {
        this.reviewId = reviewId;
        this.customerName = customerName;
        this.serviceName = serviceName;
        this.stylistName = stylistName == null ? "" : stylistName;
        this.rating = rating;
        this.comment = comment;
        this.ownerToken = ownerToken == null ? "" : ownerToken;
        this.verified = verified;
    }

    // ==========================================================
    // GETTERS AND SETTERS
    // ==========================================================

    public int getReviewId() {
        return reviewId;
    }

    public void setReviewId(int reviewId) {
        this.reviewId = reviewId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getStylistName() {
        return stylistName;
    }

    public void setStylistName(String stylistName) {
        this.stylistName = stylistName == null ? "" : stylistName;
    }

    public int getRating() {
        return rating;
    }

    public void setRating(int rating) {
        this.rating = rating;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    public String getOwnerToken() {
        return ownerToken;
    }

    public void setOwnerToken(String ownerToken) {
        this.ownerToken = ownerToken == null ? "" : ownerToken;
    }

    // NEW: Getters and Setters for the Verified Badge
    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

    // ==========================================================
    // VIEW HELPERS
    // ==========================================================

    public String getReviewType() {
        return "Review";
    }

    public String getCustomerViewHeader() {
        return "Customer Review";
    }

    public String getAdminViewHeader() {
        return "Admin View";
    }

    public void displayReview() {
        System.out.println("Review ID: " + reviewId);
        System.out.println("Customer Name: " + customerName);
        System.out.println("Service Name: " + serviceName);
        System.out.println("Stylist Name: " + stylistName);
        System.out.println("Rating: " + rating);
        System.out.println("Comment: " + comment);
        System.out.println("Verified: " + verified); // Added to display helper
    }
}
