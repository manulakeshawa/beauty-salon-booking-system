package com.example.beautysalonreview;

public class VerifiedReview extends Review {

    public VerifiedReview() {
        super();
    }

    public VerifiedReview(int reviewId, String customerName, String serviceName, int rating, String comment) {
        super(reviewId, customerName, serviceName, rating, comment);
    }

    @Override
    public void displayReview() {
        System.out.println("=== Verified Review ===");
        super.displayReview();
    }
}