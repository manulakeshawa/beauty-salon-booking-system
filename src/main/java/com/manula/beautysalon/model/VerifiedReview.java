package com.manula.beautysalon.model;


/**
 * A review from a verified customer, displayed with a verification header.
 */
public class VerifiedReview extends Review {

    public VerifiedReview() {
        super();
    }

    public VerifiedReview(int reviewId, String customerName, String serviceName, int rating, String comment) {
        super(reviewId, customerName, serviceName, rating, comment);
    }

    public VerifiedReview(int reviewId, String customerName, String serviceName, String stylistName, int rating, String comment) {
        super(reviewId, customerName, serviceName, stylistName, rating, comment);
    }

    public VerifiedReview(int reviewId, String customerName, String serviceName, String stylistName, int rating, String comment, String ownerToken) {
        super(reviewId, customerName, serviceName, stylistName, rating, comment, ownerToken);
    }

    @Override
    public String getReviewType() {
        return "Verified";
    }

    @Override
    public String getCustomerViewHeader() {
        return "Verified Review";
    }

    @Override
    public String getAdminViewHeader() {
        return "Verified (trusted)";
    }

    // Adds a "Verified Review" header before printing base review details
    @Override
    public void displayReview() {
        System.out.println("=== Verified Review ===");
        super.displayReview();
    }
}
