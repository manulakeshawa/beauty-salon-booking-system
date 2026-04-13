package com.example.beautysalonreview;

public class PublicReview extends Review {

    public PublicReview() {
        super();
    }

    public PublicReview(int reviewId, String customerName, String serviceName, int rating, String comment) {
        super(reviewId, customerName, serviceName, rating, comment);
    }

    @Override
    public void displayReview() {
        System.out.println("=== Public Review ===");
        super.displayReview();
    }
}