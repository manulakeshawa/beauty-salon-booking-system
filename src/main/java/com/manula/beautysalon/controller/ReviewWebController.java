package com.manula.beautysalon.controller;

import com.manula.beautysalon.model.Review;
import com.manula.beautysalon.service.ReviewService;
import com.manula.beautysalon.service.SalonServiceService;
import com.manula.beautysalon.util.SecurityUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.UUID;

@Controller
public class ReviewWebController {

    private final ReviewService reviewService;
    private final SalonServiceService salonServiceService;

    public ReviewWebController(ReviewService reviewService, SalonServiceService salonServiceService) {
        this.reviewService = reviewService;
        this.salonServiceService = salonServiceService;
    }

    @GetMapping("/public-review")
    public String showPublicReviewPage(HttpSession session, Model model) {
        String loggedInName = (String) session.getAttribute("loggedInCustomerName");

        if (loggedInName == null) {
            return "redirect:/customers?action=login";
        }

        model.addAttribute("customerName", loggedInName);
        model.addAttribute("services", salonServiceService.readAllServices());
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

        if (session.getAttribute("staffRole") != null && !SecurityUtils.isManager(session)) {
            return "redirect:/admin?error=unauthorized";
        }

        Review review = new Review(
                0,
                loggedInCustomerName,
                serviceName,
                stylistName,
                rating,
                comment,
                UUID.randomUUID().toString(),
                false
        );

        reviewService.addReview(review);
        return "redirect:/my-portal";
    }

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
