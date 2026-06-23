package com.manula.beautysalon.service;

import com.manula.beautysalon.model.Review;
import com.manula.beautysalon.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;

    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    public Review addReview(Review review) {
        review.setReviewId(null);
        return reviewRepository.save(review);
    }

    @Transactional
    public void updateReview(int reviewId, int newRating, String newComment) {
        reviewRepository.findById(reviewId).ifPresent(review -> {
            review.setRating(newRating);
            review.setComment(newComment);
            reviewRepository.save(review);
        });
    }

    @Transactional
    public void deleteReview(int reviewId) {
        if (reviewRepository.existsById(reviewId)) {
            reviewRepository.deleteById(reviewId);
        }
    }

    public List<Review> getAllReviews() {
        return reviewRepository.findAllByOrderByReviewIdAsc();
    }

    public List<Review> getVerifiedReviews() {
        return reviewRepository.findByVerifiedTrueOrderByReviewIdAsc();
    }

    public List<Review> getReviewsByCustomerName(String customerName) {
        if (customerName == null || customerName.isBlank()) {
            return List.of();
        }
        return reviewRepository.findByCustomerNameIgnoreCaseOrderByReviewIdAsc(customerName);
    }

    public Review getReviewById(int reviewId) {
        return reviewRepository.findById(reviewId).orElse(null);
    }

    public List<Review> getFilteredReviews(String service, String stylist) {
        List<Review> reviews = getAllReviews();

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

    @Transactional
    public void toggleReviewVerification(int reviewId) {
        reviewRepository.findById(reviewId).ifPresent(review -> {
            review.setVerified(!review.isVerified());
            reviewRepository.save(review);
        });
    }

    public int generateNextReviewId() {
        return reviewRepository.findTopByOrderByReviewIdDesc()
                .map(review -> review.getReviewId() + 1)
                .orElse(1);
    }
}
