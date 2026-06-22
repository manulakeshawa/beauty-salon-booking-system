package com.manula.beautysalon.repository;

import com.manula.beautysalon.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
    List<Review> findAllByOrderByReviewIdAsc();

    List<Review> findByVerifiedTrueOrderByReviewIdAsc();

    List<Review> findByCustomerNameIgnoreCaseOrderByReviewIdAsc(String customerName);

    Optional<Review> findTopByOrderByReviewIdDesc();
}
