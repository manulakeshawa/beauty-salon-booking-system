package com.manula.beautysalon.model;

/**
 * A publicly visible review — displayed without any verification badge.
 */
public class PublicReview extends Review {

    public PublicReview() {
        super();
    }

    public PublicReview(int reviewId, String customerName, String serviceName, int rating, String comment) {
        super(reviewId, customerName, serviceName, rating, comment);
    }

    public PublicReview(int reviewId, String customerName, String serviceName, String stylistName, int rating, String comment) {
        super(reviewId, customerName, serviceName, stylistName, rating, comment);
    }

    public PublicReview(int reviewId, String customerName, String serviceName, String stylistName, int rating, String comment, String ownerToken) {
        super(reviewId, customerName, serviceName, stylistName, rating, comment, ownerToken);
    }

    @Override
    public String getReviewType() {
        return "Public";
    }

    @Override
    public String getCustomerViewHeader() {
        return "Public Review";
    }

    @Override
    public String getAdminViewHeader() {
        return "Public (unverified)";
    }

    // Overrides display method to show type of review
    @Override
    public void displayReview() {
        System.out.println("=== Public Review ===");
        super.displayReview();
    }
}
