package com.manula.beautysalon.controller;

import com.manula.beautysalon.model.Review;
import com.manula.beautysalon.model.SalonService;
import com.manula.beautysalon.model.Stylist;
import com.manula.beautysalon.service.ReviewService;
import com.manula.beautysalon.service.SalonServiceService;
import com.manula.beautysalon.service.StylistService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class HomeController {

    private final SalonServiceService salonServiceService;
    private final ReviewService reviewService;
    private final StylistService stylistService;

    public HomeController(SalonServiceService salonServiceService, ReviewService reviewService, StylistService stylistService) {
        this.salonServiceService = salonServiceService;
        this.reviewService = reviewService;
        this.stylistService = stylistService;
    }

    @GetMapping("/")
    public String showStorefront(Model model) {
        model.addAttribute("services", loadServices());
        model.addAttribute("reviews", loadStorefrontReviews());
        model.addAttribute("stylists", loadStylists());
        return "index";
    }

    @GetMapping("/admin")
    public String showAdminDashboard(HttpSession session) {
        if (session.getAttribute("staffRole") == null) {
            return "redirect:/staff-login";
        }
        return "admin-dashboard";
    }

    private List<SalonService> loadServices() {
        try {
            return salonServiceService.readAllServices();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private List<Review> loadStorefrontReviews() {
        try {
            List<Review> verifiedReviews = reviewService.getVerifiedReviews();
            if (!verifiedReviews.isEmpty()) {
                return verifiedReviews;
            }
        } catch (Exception ignored) {
        }
        return fallbackTestimonials();
    }

    private List<Stylist> loadStylists() {
        try {
            return stylistService.readAllStylists();
        } catch (Exception e) {
            return new ArrayList<>();
        }
    }

    private List<Review> fallbackTestimonials() {
        List<Review> reviews = new ArrayList<>();
        reviews.add(new Review(0, "Amaya Fernando", "Lumiere Signature Facial", "Nalika", 5,
                "Absolutely incredible experience. My skin has never felt better!", "", true));
        reviews.add(new Review(0, "Dineth Perera", "Classic Gentlemen's Grooming", "Kamindu", 5,
                "A true professional service with a calm, polished salon experience.", "", true));
        reviews.add(new Review(0, "Kavindi Silva", "Bridal Hair & Makeup Package", "Kasun", 5,
                "The team made the whole appointment feel effortless and beautifully personal.", "", true));
        return reviews;
    }
}
