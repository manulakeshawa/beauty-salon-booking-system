package com.manula.beautysalon.controller;

import com.manula.beautysalon.model.Stylist;
import com.manula.beautysalon.repository.file.StylistFileManager;
import com.manula.beautysalon.util.SecurityUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;

@Controller
public class StylistWebController {

    private final StylistFileManager stylistFileManager = new StylistFileManager();

    @GetMapping("/stylists")
    public String handleStylistsGet(
            @RequestParam(defaultValue = "list") String action,
            @RequestParam(required = false) Integer userId,
            HttpSession session,
            Model model
    ) {
        if (session.getAttribute("staffRole") == null) {
            return "redirect:/staff-login";
        }
        try {
            switch (action.toLowerCase()) {
                case "new":
                    if (!SecurityUtils.isManager(session)) {
                        return "redirect:/admin?error=unauthorized";
                    }
                    model.addAttribute("generatedUserId", stylistFileManager.generateNextStylistId());
                    return "Stylist-register";
                case "register":
                    if (!SecurityUtils.isManager(session)) {
                        return "redirect:/admin?error=unauthorized";
                    }
                    model.addAttribute("generatedUserId", stylistFileManager.generateNextStylistId());
                    return "Stylist-register";
                case "edit":
                case "update":
                    if (!SecurityUtils.isManager(session)) {
                        return "redirect:/admin?error=unauthorized";
                    }
                    if (userId == null) {
                        return "redirect:/stylists?action=list";
                    }
                    Stylist stylistToEdit = findById(userId);
                    if (stylistToEdit == null) {
                        return "redirect:/stylists?action=list";
                    }
                    model.addAttribute("stylist", stylistToEdit);
                    return "stylist-update";
                case "manageavailability":
                case "availability":
                    if (!SecurityUtils.isManager(session)) {
                        return "redirect:/admin?error=unauthorized";
                    }
                    List<Stylist> stylists = stylistFileManager.readAllStylists();
                    model.addAttribute("stylists", stylists);
                    return "stylist-availability";
                case "delete":
                    if (!SecurityUtils.isManager(session)) {
                        return "redirect:/admin?error=unauthorized";
                    }
                    if (userId != null) {
                        stylistFileManager.deleteStylist(userId);
                    }
                    return "redirect:/stylists?action=list";
                case "list":
                default:
                    model.addAttribute("stylists", stylistFileManager.readAllStylists());
                    return "stylist-list";
            }
        } catch (IOException ignored) {
            return "redirect:/stylists?action=list";
        }
    }

    @PostMapping("/stylists")
    public String handleStylistsPost(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String newPassword,
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) Boolean available,
            HttpSession session
    ) {
        if (session.getAttribute("staffRole") == null) {
            return "redirect:/staff-login";
        }
        if (action == null || action.isBlank()) {
            return "redirect:/stylists?action=list";
        }
        try {
            switch (action.toLowerCase()) {
                case "change-password":
                    String loggedInEmail = (String) session.getAttribute("staffEmail");
                    if (loggedInEmail == null) {
                        return "redirect:/staff-login";
                    }
                    if (newPassword != null && !newPassword.isBlank()) {
                        List<Stylist> allStylists = stylistFileManager.readAllStylists();
                        for (Stylist s : allStylists) {
                            if (s.getEmail().equalsIgnoreCase(loggedInEmail)) {
                                s.setPassword(newPassword);
                                stylistFileManager.updateStylist(s);
                                break;
                            }
                        }
                    }
                    // FIXED: Redirects specifically to the stylist portal now!
                    return "redirect:/stylist-portal?passwordStatus=updated";

                case "new":
                case "register":
                    if (!SecurityUtils.isManager(session)) {
                        return "redirect:/admin?error=unauthorized";
                    }
                    if (name != null && email != null && specialty != null && level != null) {

                        int newId = stylistFileManager.generateNextStylistId();
                        String defaultPassword = "LumiereStaff2026";

                        Stylist stylist = new Stylist(
                                newId,
                                name,
                                email,
                                defaultPassword,
                                specialty,
                                level,
                                available != null && available,
                                "default.jpg"
                        );
                        stylistFileManager.saveStylist(stylist);
                    }
                    break;
                case "edit":
                case "update":
                    if (!SecurityUtils.isManager(session)) {
                        return "redirect:/admin?error=unauthorized";
                    }
                    if (userId != null) {
                        Stylist existing = findById(userId);
                        if (existing != null) {
                            if (name != null) {
                                existing.setName(name);
                            }
                            if (email != null) {
                                existing.setEmail(email);
                            }
                            if (password != null && !password.trim().isEmpty()) {
                                existing.setPassword(password);
                            }
                            if (specialty != null) {
                                existing.setSpecialty(specialty);
                            }
                            if (level != null) {
                                existing.setLevel(level);
                            }
                            existing.setAvailable(available != null && available);
                            stylistFileManager.updateStylist(existing);
                        }
                    }
                    break;
                case "availability":
                    if (!SecurityUtils.isManager(session)) {
                        return "redirect:/admin?error=unauthorized";
                    }
                    if (userId != null) {
                        Stylist stylist = findById(userId);
                        if (stylist != null) {
                            stylist.setAvailable(available != null && available);
                            stylistFileManager.updateStylist(stylist);
                        }
                    }
                    return "redirect:/stylists?action=availability";
                default:
                    break;
            }
        } catch (IOException ignored) {
            return "redirect:/stylists?action=list";
        }

        return "redirect:/stylists?action=list";
    }

    private Stylist findById(int userId) throws IOException {
        return stylistFileManager.readAllStylists().stream()
                .filter(stylist -> stylist.getUserId() == userId)
                .findFirst().orElse(null);
    }
}
