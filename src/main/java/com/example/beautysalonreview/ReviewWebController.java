package com.example.beautysalonreview;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession; // NEW: Added for session tracking
import java.io.IOException;
import java.util.UUID;

/**
 * Spring MVC controller that maps HTTP requests to review-related Thymeleaf views.
 */
@Controller
public class ReviewWebController {

    private final ReviewController reviewController = new ReviewController();
    private final ReviewFileManager reviewFileManager = new ReviewFileManager();

    // ==========================================================
    // NEW: PUBLIC REVIEW ROUTING
    // ==========================================================
    
    @GetMapping("/public-review")
    public String showPublicReviewPage(HttpSession session, Model model) {
        String loggedInName = (String) session.getAttribute("loggedInCustomerName");
        
        // Security Bouncer: Boot them to login if they aren't signed in
        if (loggedInName == null) {
            return "redirect:/customers?action=login";
        }
        
        // Pre-fill and lock their exact name!
        model.addAttribute("customerName", loggedInName);
        
        return "public-review-form"; 
    }

    @PostMapping("/submitPublicReview")
    public String submitPublicReview(
            HttpSession session,
            @RequestParam String customerName,
            @RequestParam String serviceName,
            @RequestParam(required = false) String stylistName,
            @RequestParam int rating,
            @RequestParam String comment
    ) {
        if (session.getAttribute("staffRole") != null && !SecurityUtils.isManager(session)) {
            return "redirect:/admin?error=unauthorized";
        }
        int newId = 5001;
        try {
            newId = reviewFileManager.generateNextReviewId();
        } catch (IOException ignored) {}

        String ownerToken = UUID.randomUUID().toString();
        
        // Automatically make it a Verified Review because they are a logged-in user!
        Review review = new VerifiedReview(newId, customerName, serviceName, stylistName, rating, comment, ownerToken);
        reviewController.addReview(review);

        // Send them straight back to their portal!
        return "redirect:/my-portal";
    }

    // ==========================================================
    // EXISTING ADMIN / GENERAL ROUTING
    // ==========================================================

    @GetMapping("/review-home")
    public String showHomePage(Model model) {
        try {
            model.addAttribute("generatedReviewId", reviewFileManager.generateNextReviewId());
        } catch (IOException ignored) {
            model.addAttribute("generatedReviewId", 5001); 
        }
        
        model.addAttribute("customerName", "");
        model.addAttribute("serviceName", "");
        model.addAttribute("stylistName", "");
        model.addAttribute("rating", 5);
        model.addAttribute("comment", "");
        model.addAttribute("reviewType", "Public");
        return "review-form";
    }

    @PostMapping("/submitReview")
    public String submitReview(
            HttpSession session,
            @RequestParam(required = false) Integer reviewId, 
            @RequestParam String customerName,
            @RequestParam String serviceName,
            @RequestParam(required = false) String stylistName,
            @RequestParam int rating,
            @RequestParam String comment,
            @RequestParam(defaultValue = "Public") String reviewType,
            Model model
    ) {
        if (session.getAttribute("staffRole") != null && !SecurityUtils.isManager(session)) {
            return "redirect:/admin?error=unauthorized";
        }
        int newId = 5001;
        try {
            newId = reviewFileManager.generateNextReviewId();
        } catch (IOException ignored) {}

        String ownerToken = UUID.randomUUID().toString();
        Review review;
        if ("Verified".equalsIgnoreCase(reviewType)) {
            review = new VerifiedReview(newId, customerName, serviceName, stylistName, rating, comment, ownerToken);
        } else {
            review = new PublicReview(newId, customerName, serviceName, stylistName, rating, comment, ownerToken);
        }
        reviewController.addReview(review);

        model.addAttribute("createdToken", ownerToken);
        model.addAttribute("createdReviewId", newId);
        model.addAttribute("createdManageUrl", "/editReview?id=" + newId + "&token=" + ownerToken);

        try {
            model.addAttribute("generatedReviewId", reviewFileManager.generateNextReviewId());
        } catch (IOException ignored) {
            model.addAttribute("generatedReviewId", newId + 1);
        }
        
        model.addAttribute("customerName", "");
        model.addAttribute("serviceName", "");
        model.addAttribute("stylistName", "");
        model.addAttribute("rating", 5);
        model.addAttribute("comment", "");
        model.addAttribute("reviewType", "Public");

        return "review-form";
    }

    @GetMapping("/reviews")
    public String showReviewsPage(
            @RequestParam(required = false) String service,
            @RequestParam(required = false) String stylist,
            @RequestParam(required = false) String error,
            Model model
    ) {
        model.addAttribute("reviews", reviewController.getFilteredReviews(service, stylist));
        model.addAttribute("service", service == null ? "" : service);
        model.addAttribute("stylist", stylist == null ? "" : stylist);
        model.addAttribute("error", error == null ? "" : error);
        return "review-list";
    }

    @GetMapping("/manageReview")
    public String manageReview(
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) String token,
            HttpSession session) {
        if (session.getAttribute("staffRole") != null && !SecurityUtils.isManager(session)) {
            return "redirect:/admin?error=unauthorized";
        }
        if (id == null || token == null || token.isBlank()) {
            return "redirect:/reviews?error=invalid";
        }
        Review review = reviewController.getReviewById(id);
        if (review == null) {
            return "redirect:/reviews?error=notfound";
        }
        if (review.getOwnerToken() == null || review.getOwnerToken().isBlank() || !review.getOwnerToken().equals(token)) {
            return "redirect:/reviews?error=invalid";
        }
        return "redirect:/editReview?id=" + id + "&token=" + token;
    }

    @GetMapping("/editReview")
    public String showEditPage(
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) String token,
            HttpSession session,
            Model model) {
        if (session.getAttribute("staffRole") != null && !SecurityUtils.isManager(session)) {
            return "redirect:/admin?error=unauthorized";
        }
        if (id == null || token == null || token.isBlank()) {
            return "redirect:/reviews?error=invalid";
        }
        Review review = reviewController.getReviewById(id);
        if (review == null) {
            return "redirect:/reviews?error=notfound";
        }
        if (review.getOwnerToken() == null || review.getOwnerToken().isBlank() || !review.getOwnerToken().equals(token)) {
            return "redirect:/reviews?error=invalid";
        }
        model.addAttribute("review", review);
        model.addAttribute("token", token);
        model.addAttribute("formAction", "/updateReview");
        return "review-edit";
    }

    @PostMapping("/updateReview")
    public String updateReview(
            @RequestParam(required = false) Integer reviewId,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) String comment,
            @RequestParam(required = false) String token,
            HttpSession session
    ) {
        if (session.getAttribute("staffRole") != null && !SecurityUtils.isManager(session)) {
            return "redirect:/admin?error=unauthorized";
        }
        if (reviewId == null || rating == null || comment == null || token == null || token.isBlank()) {
            return "redirect:/reviews?error=invalid";
        }
        Review review = reviewController.getReviewById(reviewId);
        if (review == null) {
            return "redirect:/reviews?error=notfound";
        }
        if (review.getOwnerToken() == null || review.getOwnerToken().isBlank() || !review.getOwnerToken().equals(token)) {
            return "redirect:/reviews?error=invalid";
        }
        reviewController.updateReview(reviewId, rating, comment);
        return "redirect:/reviews";
    }

    @GetMapping("/deleteReview")
    public String deleteReview(
            @RequestParam(required = false) Integer id,
            @RequestParam(required = false) String token,
            HttpSession session) {
        if (session.getAttribute("staffRole") != null && !SecurityUtils.isManager(session)) {
            return "redirect:/admin?error=unauthorized";
        }
        if (id == null || token == null || token.isBlank()) {
            return "redirect:/reviews?error=invalid";
        }
        Review review = reviewController.getReviewById(id);
        if (review == null) {
            return "redirect:/reviews?error=notfound";
        }
        if (review.getOwnerToken() == null || review.getOwnerToken().isBlank() || !review.getOwnerToken().equals(token)) {
            return "redirect:/reviews?error=invalid";
        }
        reviewController.deleteReview(id);
        return "redirect:/reviews";
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
        model.addAttribute("reviews", reviewController.getFilteredReviews(service, stylist));
        model.addAttribute("service", service == null ? "" : service);
        model.addAttribute("stylist", stylist == null ? "" : stylist);
        return "admin-review-control";
    }

    @GetMapping("/admin/deleteReview")
    public String adminDeleteReview(@RequestParam(required = false) Integer id, HttpSession session) {
        if (session.getAttribute("staffRole") == null) {
            return "redirect:/staff-login";
        }
        if (!SecurityUtils.isManager(session)) {
            return "redirect:/admin?error=unauthorized";
        }
        if (id == null) {
            return "redirect:/admin/reviews";
        }
        reviewController.deleteReview(id);
        return "redirect:/admin/reviews";
    }

    @GetMapping("/admin/editReview")
    public String adminEditReview(@RequestParam(required = false) Integer id, HttpSession session, Model model) {
        if (session.getAttribute("staffRole") == null) {
            return "redirect:/staff-login";
        }
        if (!SecurityUtils.isManager(session)) {
            return "redirect:/admin?error=unauthorized";
        }
        if (id == null) {
            return "redirect:/admin/reviews";
        }
        Review review = reviewController.getReviewById(id);
        if (review == null) {
            return "redirect:/admin/reviews";
        }
        model.addAttribute("review", review);
        model.addAttribute("token", "");
        model.addAttribute("formAction", "/admin/updateReview");
        return "review-edit";
    }

    @PostMapping("/admin/updateReview")
    public String adminUpdateReview(
            @RequestParam(required = false) Integer reviewId,
            @RequestParam(required = false) Integer rating,
            @RequestParam(required = false) String comment,
            HttpSession session
    ) {
        if (session.getAttribute("staffRole") == null) {
            return "redirect:/staff-login";
        }
        if (!SecurityUtils.isManager(session)) {
            return "redirect:/admin?error=unauthorized";
        }
        if (reviewId == null || rating == null || comment == null) {
            return "redirect:/admin/reviews";
        }
        reviewController.updateReview(reviewId, rating, comment);
        return "redirect:/admin/reviews";
    }
}