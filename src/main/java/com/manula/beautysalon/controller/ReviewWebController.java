package com.manula.beautysalon.controller;

import com.manula.beautysalon.model.Review;
import com.manula.beautysalon.security.SalonUserPrincipal;
import com.manula.beautysalon.security.SecuritySessionService;
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
    private final SecuritySessionService securitySessionService;

    public ReviewWebController(ReviewService reviewService, SalonServiceService salonServiceService, SecuritySessionService securitySessionService) {
        this.reviewService = reviewService;
        this.salonServiceService = salonServiceService;
        this.securitySessionService = securitySessionService;
    }

    @GetMapping("/public-review")
    public String showPublicReviewPage(HttpSession session, Model model) {
        SalonUserPrincipal customerPrincipal = securitySessionService.currentPrincipal();
        if (customerPrincipal == null || !customerPrincipal.isCustomer()) {
            return "redirect:/customers?action=login";
        }

        model.addAttribute("customerName", customerPrincipal.getDisplayName());
        model.addAttribute("services", salonServiceService.readActiveServices());
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
        SalonUserPrincipal customerPrincipal = securitySessionService.currentPrincipal();
        if (customerPrincipal == null || !customerPrincipal.isCustomer()) {
            return "redirect:/customers?action=login";
        }

        Review review = new Review(
                0,
                customerPrincipal.getDisplayName(),
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
        model.addAttribute("reviews", reviewService.getFilteredReviews(service, stylist));
        model.addAttribute("service", service == null ? "" : service);
        model.addAttribute("stylist", stylist == null ? "" : stylist);
        return "admin-review-control";
    }

    @PostMapping("/admin/reviews/toggle")
    public String toggleReviewStatus(@RequestParam Integer id, HttpSession session) {
        if (!SecurityUtils.isManager(session)) {
            return "redirect:/access-denied";
        }

        reviewService.toggleReviewVerification(id);
        return "redirect:/admin/reviews";
    }

    @PostMapping("/admin/deleteReview")
    public String adminDeleteReview(
            @RequestParam(required = false) Integer id,
            HttpSession session
    ) {
        if (!SecurityUtils.isManager(session)) {
            return "redirect:/access-denied";
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
        SalonUserPrincipal customerPrincipal = securitySessionService.currentPrincipal();
        if (customerPrincipal == null || !customerPrincipal.isCustomer()) {
            return "redirect:/customers?action=login";
        }
        String loggedInCustomer = customerPrincipal.getDisplayName();

        Review review = reviewService.getReviewById(id);

        // Customers may edit only reviews tied to their own display name; admin moderation
        // uses separate review controls and the verified flag.
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
        SalonUserPrincipal customerPrincipal = securitySessionService.currentPrincipal();
        if (customerPrincipal == null || !customerPrincipal.isCustomer()) {
            return "redirect:/customers?action=login";
        }
        String loggedInCustomer = customerPrincipal.getDisplayName();

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
        SalonUserPrincipal customerPrincipal = securitySessionService.currentPrincipal();
        if (customerPrincipal == null || !customerPrincipal.isCustomer()) {
            return "redirect:/customers?action=login";
        }
        String loggedInCustomer = customerPrincipal.getDisplayName();

        Review review = reviewService.getReviewById(reviewId);

        if (review != null && review.getCustomerName().equalsIgnoreCase(loggedInCustomer)) {
            reviewService.deleteReview(reviewId);
            return "redirect:/my-portal?status=deleted";
        }

        return "redirect:/my-portal?error=unauthorized";
    }
}
