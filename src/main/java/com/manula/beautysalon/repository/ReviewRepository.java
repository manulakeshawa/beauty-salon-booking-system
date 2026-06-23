package com.manula.beautysalon.repository;

import com.manula.beautysalon.model.Review;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface ReviewRepository extends JpaRepository<Review, Integer> {
    List<Review> findAllByOrderByReviewIdAsc();

    List<Review> findByVerifiedTrueOrderByReviewIdAsc();

    List<Review> findByCustomerNameIgnoreCaseOrderByReviewIdAsc(String customerName);

    long deleteByCustomerNameIgnoreCase(String customerName);

    boolean existsByServiceNameIgnoreCase(String serviceName);

    boolean existsByStylistNameIgnoreCase(String stylistName);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Review r set r.customerName = :newName where lower(r.customerName) = lower(:oldName)")
    int updateCustomerNameIgnoreCase(@Param("oldName") String oldName, @Param("newName") String newName);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Review r set r.serviceName = :newName where lower(r.serviceName) = lower(:oldName)")
    int updateServiceNameIgnoreCase(@Param("oldName") String oldName, @Param("newName") String newName);

    @Modifying(clearAutomatically = true, flushAutomatically = true)
    @Query("update Review r set r.stylistName = :newName where lower(r.stylistName) = lower(:oldName)")
    int updateStylistNameIgnoreCase(@Param("oldName") String oldName, @Param("newName") String newName);

    Optional<Review> findTopByOrderByReviewIdDesc();
}
