package com.example.beautysalonreview;

public class Review {
    private int reviewId;
    private String customerName;
    private String serviceName;
    private int rating;
    private String comment;

    public Review() {
    }

    public Review(int reviewId, String customerName, String serviceName, int rating, String comment) {
        this.reviewId = reviewId;
        this.customerName = customerName;
        this.serviceName = serviceName;
        this.rating = rating;
        this.comment = comment;
    }

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

    public void displayReview() {
        System.out.println("Review ID: " + reviewId);
        System.out.println("Customer Name: " + customerName);
        System.out.println("Service Name: " + serviceName);
        System.out.println("Rating: " + rating);
        System.out.println("Comment: " + comment);
    }
}