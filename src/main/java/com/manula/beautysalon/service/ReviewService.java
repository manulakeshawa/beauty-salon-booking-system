package com.manula.beautysalon.service;

import com.manula.beautysalon.model.Review;
import com.manula.beautysalon.repository.file.ReviewFileManager;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReviewService {

    private final ReviewFileManager reviewFileManager = new ReviewFileManager();

    public void addReview(Review review) {
        List<Review> reviews = reviewFileManager.readAllReviews();
        reviews.add(review);
        reviewFileManager.writeAllReviews(reviews);
    }

    public void updateReview(int reviewId, int newRating, String newComment) {
        List<Review> reviews = reviewFileManager.readAllReviews();

        for (Review review : reviews) {
            if (review.getReviewId() == reviewId) {
                review.setRating(newRating);
                review.setComment(newComment);
                break;
            }
        }

        reviewFileManager.writeAllReviews(reviews);
    }

    public void deleteReview(int reviewId) {
        List<Review> reviews = reviewFileManager.readAllReviews();
        reviews.removeIf(review -> review.getReviewId() == reviewId);
        reviewFileManager.writeAllReviews(reviews);
    }

    public List<Review> getAllReviews() {
        return reviewFileManager.readAllReviews();
    }

    public Review getReviewById(int reviewId) {
        return reviewFileManager.readAllReviews()
                .stream()
                .filter(review -> review.getReviewId() == reviewId)
                .findFirst()
                .orElse(null);
    }

    public List<Review> getFilteredReviews(String service, String stylist) {
        List<Review> reviews = reviewFileManager.readAllReviews();

        String serviceSearch = service == null ? "" : service.trim().toLowerCase();
        String stylistSearch = stylist == null ? "" : stylist.trim().toLowerCase();

        if (serviceSearch.isEmpty() && stylistSearch.isEmpty()) {
            return reviews;
        }

        List<Review> filteredReviews = new ArrayList<>();

        for (Review review : reviews) {
            boolean matches = true;

            if (!serviceSearch.isEmpty()) {
                matches = review.getServiceName() != null
                        && review.getServiceName().toLowerCase().contains(serviceSearch);
            }

            if (matches && !stylistSearch.isEmpty()) {
                matches = review.getStylistName() != null
                        && review.getStylistName().toLowerCase().contains(stylistSearch);
            }

            if (matches) {
                filteredReviews.add(review);
            }
        }

        return filteredReviews;
    }

    public void toggleReviewVerification(int reviewId) {
        List<Review> reviews = reviewFileManager.readAllReviews();

        for (Review review : reviews) {
            if (review.getReviewId() == reviewId) {
                review.setVerified(!review.isVerified());
                break;
            }
        }

        reviewFileManager.writeAllReviews(reviews);
    }

    public int generateNextReviewId() {
        try {
            return reviewFileManager.generateNextReviewId();
        } catch (Exception e) {
            return 5001;
        }
    }
}