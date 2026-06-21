package com.manula.beautysalon.controller;

import com.manula.beautysalon.model.Review;
import com.manula.beautysalon.model.SalonService;
import com.manula.beautysalon.model.Stylist;
import com.manula.beautysalon.repository.file.ReviewFileManager;
import com.manula.beautysalon.repository.file.ServiceFileManager;
import com.manula.beautysalon.repository.file.StylistFileManager;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.ArrayList;
import java.util.List;

@Controller
public class HomeController {

    // Connect to your existing Service file manager
    private final ServiceFileManager serviceFileManager = new ServiceFileManager();

    // Connect to your existing Review file manager
    private final ReviewFileManager reviewFileManager = new ReviewFileManager();

    // NEW: Connect to your existing Stylist file manager
    private final StylistFileManager stylistFileManager = new StylistFileManager();

    @GetMapping("/")
    public String showStorefront(Model model) {

        // 1. Fetch Live Services
        try {
            List<SalonService> liveServices = serviceFileManager.readAllServices();
            model.addAttribute("services", liveServices);
        } catch (Exception e) {
            // If the file is empty or fails, send an empty list so it doesn't crash
            model.addAttribute("services", new ArrayList<>());
        }

        // 2. Fetch Live Reviews
        try {
            List<Review> liveReviews = reviewFileManager.readAllReviews();
            model.addAttribute("reviews", liveReviews);
        } catch (Exception e) {
            // If the file is empty or fails, send an empty list
            model.addAttribute("reviews", new ArrayList<>());
        }

        // 3. NEW: Fetch Live Stylists
        try {
            List<Stylist> liveStylists = stylistFileManager.readAllStylists();
            model.addAttribute("stylists", liveStylists);
        } catch (Exception e) {
            // If the file is empty or fails, send an empty list
            model.addAttribute("stylists", new ArrayList<>());
        }

        return "index"; // This tells Spring Boot to look for index.html in the templates folder
    }

    // ==========================================================
    // ROUTE: The secure admin dashboard
    // ==========================================================
    @GetMapping("/admin")
    public String showAdminDashboard(HttpSession session) {
        if (session.getAttribute("staffRole") == null) {
            return "redirect:/staff-login";
        }
        return "admin-dashboard"; // This tells Spring Boot to load your new admin-dashboard.html
    }
}
