package com.manula.beautysalon.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Lob;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

/**
 * Base class representing a customer review for a beauty salon service.
 */
@Entity
@Table(name = "reviews")
public class Review {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "review_id")
    private Integer reviewId;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private String serviceName;

    private String stylistName;
    private int rating;

    @Lob
    private String comment;

    @Column(length = 1000)
    private String ownerToken;

    private boolean verified;

    public Review() {
        this.ownerToken = "";
        this.verified = false;
    }

    public Review(Integer reviewId, String customerName, String serviceName, int rating, String comment) {
        this.reviewId = reviewId;
        this.customerName = customerName;
        this.serviceName = serviceName;
        this.stylistName = "";
        this.rating = rating;
        this.comment = comment;
        this.ownerToken = "";
        this.verified = false;
    }

    public Review(Integer reviewId, String customerName, String serviceName, String stylistName, int rating, String comment) {
        this.reviewId = reviewId;
        this.customerName = customerName;
        this.serviceName = serviceName;
        this.stylistName = stylistName == null ? "" : stylistName;
        this.rating = rating;
        this.comment = comment;
        this.ownerToken = "";
        this.verified = false;
    }

    public Review(Integer reviewId, String customerName, String serviceName, String stylistName, int rating, String comment, String ownerToken) {
        this.reviewId = reviewId;
        this.customerName = customerName;
        this.serviceName = serviceName;
        this.stylistName = stylistName == null ? "" : stylistName;
        this.rating = rating;
        this.comment = comment;
        this.ownerToken = ownerToken == null ? "" : ownerToken;
        this.verified = false;
    }

    public Review(Integer reviewId, String customerName, String serviceName, String stylistName, int rating, String comment, String ownerToken, boolean verified) {
        this.reviewId = reviewId;
        this.customerName = customerName;
        this.serviceName = serviceName;
        this.stylistName = stylistName == null ? "" : stylistName;
        this.rating = rating;
        this.comment = comment;
        this.ownerToken = ownerToken == null ? "" : ownerToken;
        this.verified = verified;
    }

    @PrePersist
    private void applyDefaults() {
        if (stylistName == null) {
            stylistName = "";
        }
        if (ownerToken == null) {
            ownerToken = "";
        }
    }

    public Integer getReviewId() {
        return reviewId;
    }

    public void setReviewId(Integer reviewId) {
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

    public boolean isVerified() {
        return verified;
    }

    public void setVerified(boolean verified) {
        this.verified = verified;
    }

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
        System.out.println("Verified: " + verified);
    }
}
