package com.example.beautysalonreview;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewController {
    private List<Review> reviews;
    private final String FILE_NAME = "reviews.txt";

    public ReviewController() {
        reviews = new ArrayList<>();
        loadReviewsFromFile();
    }

    public void addReview(Review review) {
        reviews.add(review);
        saveReviewToFile(review);
        System.out.println("Review added successfully.");
    }

    private void saveReviewToFile(Review review) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME, true))) {
            writer.write(review.getReviewId() + "," +
                    review.getCustomerName() + "," +
                    review.getServiceName() + "," +
                    review.getRating() + "," +
                    review.getComment());
            writer.newLine();
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }

    public void loadReviewsFromFile() {
        reviews.clear();

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_NAME))) {
            String line;

            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");

                if (data.length == 5) {
                    int id = Integer.parseInt(data[0]);
                    String customer = data[1];
                    String service = data[2];
                    int rating = Integer.parseInt(data[3]);
                    String comment = data[4];

                    Review review = new Review(id, customer, service, rating, comment);
                    reviews.add(review);
                }
            }

        } catch (IOException e) {
            System.out.println("Error reading file: " + e.getMessage());
        }
    }

    public void viewAllReviews() {
        if (reviews.isEmpty()) {
            System.out.println("No reviews found.");
            return;
        }

        for (Review review : reviews) {
            review.displayReview();
            System.out.println("--------------------");
        }
    }

    public void updateReview(int reviewId, int newRating, String newComment) {
        for (Review review : reviews) {
            if (review.getReviewId() == reviewId) {
                review.setRating(newRating);
                review.setComment(newComment);
                rewriteFile();
                System.out.println("Review updated successfully.");
                return;
            }
        }

        System.out.println("Review not found.");
    }

    public void deleteReview(int reviewId) {
        for (int i = 0; i < reviews.size(); i++) {
            if (reviews.get(i).getReviewId() == reviewId) {
                reviews.remove(i);
                rewriteFile();
                System.out.println("Review deleted successfully.");
                return;
            }
        }

        System.out.println("Review not found.");
    }

    private void rewriteFile() {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_NAME))) {
            for (Review review : reviews) {
                writer.write(review.getReviewId() + "," +
                        review.getCustomerName() + "," +
                        review.getServiceName() + "," +
                        review.getRating() + "," +
                        review.getComment());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Error rewriting file: " + e.getMessage());
        }
    }

    public List<Review> getAllReviews() {
        return reviews;
    }

    public Review getReviewById(int reviewId) {
        for (Review review : reviews) {
            if (review.getReviewId() == reviewId) {
                return review;
            }
        }
        return null;
    }
}