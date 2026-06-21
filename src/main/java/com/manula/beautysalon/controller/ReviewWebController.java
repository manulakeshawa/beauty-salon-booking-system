package com.manula.beautysalon.controller;

import com.manula.beautysalon.model.Review;
import com.manula.beautysalon.repository.file.ServiceFileManager;
import com.manula.beautysalon.service.ReviewService;
import com.manula.beautysalon.util.SecurityUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.UUID;

/**
 * Spring MVC controller that maps HTTP requests to review-related Thymeleaf views.
 */
@Controller
public class ReviewWebController {

    private final ReviewService reviewService;
    private final ServiceFileManager serviceFileManager;

    public ReviewWebController(ReviewService reviewService) {
        this.reviewService = reviewService;
        this.serviceFileManager = new ServiceFileManager();
    }

    // ==========================================================
    // PUBLIC REVIEW ROUTING
    // ==========================================================

    @GetMapping("/public-review")
    public String showPublicReviewPage(HttpSession session, Model model) {
        String loggedInName = (String) session.getAttribute("loggedInCustomerName");

        if (loggedInName == null) {
            return "redirect:/customers?action=login";
        }

        model.addAttribute("customerName", loggedInName);

        try {
            model.addAttribute("services", serviceFileManager.readAllServices());
        } catch (IOException e) {
            System.err.println("Error loading services for the review form: " + e.getMessage());
        }

        return "public-review-form";
    }

    @PostMapping("/submitPublicReview")
    public String submitPublicReview(
            HttpSession session,
            @RequestParam String serviceName,
            @RequestParam(required = false) String stylistName,
            @RequestParam int rating,
            @RequestParam String comment
    ) {
        String loggedInCustomerName = (String) session.getAttribute("loggedInCustomerName");

        if (loggedInCustomerName == null) {
            return "redirect:/customers?action=login";
        }

        // Staff cannot submit public reviews unless they are a manager
        if (session.getAttribute("staffRole") != null && !SecurityUtils.isManager(session)) {
            return "redirect:/admin?error=unauthorized";
        }

        int newId = reviewService.generateNextReviewId();
        String ownerToken = UUID.randomUUID().toString();

        // Newly submitted reviews default to unverified
        Review review = new Review(
                newId,
                loggedInCustomerName,
                serviceName,
                stylistName,
                rating,
                comment,
                ownerToken,
                false
        );

        reviewService.addReview(review);

        return "redirect:/my-portal";
    }

    // ==========================================================
    // ADMIN / GENERAL ROUTING
    // ==========================================================

    @GetMapping("/reviews")
    public String showReviewsPage(
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String stylist,
            @RequestParam(required = false) String error,
            Model model
    ) {
        model.addAttribute("reviews", reviewService.getFilteredReviews(service, stylist));
        model.addAttribute("service", service == null ? "" : service);
        model.addAttribute("stylist", stylist == null ? "" : stylist);
        model.addAttribute("error", error == null ? "" : error);

        return "review-list";
    }

    @GetMapping("/admin/reviews")
    public String showAdminReviews(
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String stylist,
            HttpSession session,
            Model model
    ) {
        if (session.getAttribute("staffRole") == null) {
            return "redirect:/staff-login";
        }

        model.addAttribute("reviews", reviewService.getFilteredReviews(service, stylist));
        model.addAttribute("service", service == null ? "" : service);
        model.addAttribute("stylist", stylist == null ? "" : stylist);

        return "admin-review-control";
    }

    @GetMapping("/admin/reviews/toggle")
    public String toggleReviewStatus(@RequestParam Integer id, HttpSession session) {
        if (session.getAttribute("staffRole") == null) {
            return "redirect:/staff-login";
        }

        if (!SecurityUtils.isManager(session)) {
            return "redirect:/admin/reviews?error=unauthorized";
        }

        reviewService.toggleReviewVerification(id);

        return "redirect:/admin/reviews";
    }

    @GetMapping("/admin/deleteReview")
    public String adminDeleteReview(
            @RequestParam(required = false) Integer id,
            HttpSession session
    ) {
        if (session.getAttribute("staffRole") == null) {
            return "redirect:/staff-login";
        }

        if (!SecurityUtils.isManager(session)) {
            return "redirect:/admin?error=unauthorized";
        }

        if (id != null) {
            reviewService.deleteReview(id);
        }

        return "redirect:/admin/reviews";
    }

    // ==========================================================
    // CUSTOMER PORTAL - SELF-EDIT LOGIC
    // ==========================================================

    @GetMapping("/editReview")
    public String showEditPage(
            @RequestParam Integer id,
            HttpSession session,
            Model model
    ) {
        String loggedInCustomer = (String) session.getAttribute("loggedInCustomerName");

        if (loggedInCustomer == null) {
            return "redirect:/customers?action=login";
        }

        Review review = reviewService.getReviewById(id);

        if (review != null && review.getCustomerName().equalsIgnoreCase(loggedInCustomer)) {
            model.addAttribute("review", review);
            return "review-edit";
        }

        return "redirect:/my-portal?error=unauthorized";
    }

    @PostMapping("/updateReview")
    public String updateReview(
            @RequestParam Integer reviewId,
            @RequestParam Integer rating,
            @RequestParam String comment,
            HttpSession session
    ) {
        String loggedInCustomer = (String) session.getAttribute("loggedInCustomerName");

        if (loggedInCustomer == null) {
            return "redirect:/customers?action=login";
        }

        Review review = reviewService.getReviewById(reviewId);

        if (review != null && review.getCustomerName().equalsIgnoreCase(loggedInCustomer)) {
            reviewService.updateReview(reviewId, rating, comment);
            return "redirect:/my-portal?status=updated";
        }

        return "redirect:/my-portal?error=failed";
    }

    @PostMapping("/deleteReview")
    public String deleteCustomerReview(
            @RequestParam Integer reviewId,
            HttpSession session
    ) {
        String loggedInCustomer = (String) session.getAttribute("loggedInCustomerName");

        if (loggedInCustomer == null) {
            return "redirect:/customers?action=login";
        }

        Review review = reviewService.getReviewById(reviewId);

        if (review != null && review.getCustomerName().equalsIgnoreCase(loggedInCustomer)) {
            reviewService.deleteReview(reviewId);
            return "redirect:/my-portal?status=deleted";
        }

        return "redirect:/my-portal?error=unauthorized";
    }
}