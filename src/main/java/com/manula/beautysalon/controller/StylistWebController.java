package com.manula.beautysalon.controller;

import com.manula.beautysalon.model.Stylist;
import com.manula.beautysalon.service.DuplicateEmailException;
import com.manula.beautysalon.service.PasswordSetupToken;
import com.manula.beautysalon.service.StylistService;
import com.manula.beautysalon.util.SecurityUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;
import org.springframework.web.servlet.support.ServletUriComponentsBuilder;

import java.util.List;

@Controller
public class StylistWebController {

    private final StylistService stylistService;

    public StylistWebController(StylistService stylistService) {
        this.stylistService = stylistService;
    }

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

        switch (action.toLowerCase()) {
            case "new":
            case "register":
                if (!SecurityUtils.isManager(session)) {
                    return "redirect:/admin?error=unauthorized";
                }
                model.addAttribute("generatedUserId", stylistService.generateNextStylistId());
                return "Stylist-register";
            case "edit":
            case "update":
                if (!SecurityUtils.isManager(session)) {
                    return "redirect:/admin?error=unauthorized";
                }
                if (userId == null) {
                    return "redirect:/stylists?action=list";
                }
                Stylist stylistToEdit = stylistService.findById(userId);
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
                List<Stylist> stylists = stylistService.readAllStylists();
                model.addAttribute("stylists", stylists);
                return "stylist-availability";
            case "delete":
                if (!SecurityUtils.isManager(session)) {
                    return "redirect:/admin?error=unauthorized";
                }
                if (userId != null) {
                    stylistService.deleteStylist(userId);
                }
                return "redirect:/stylists?action=list";
            case "list":
            default:
                model.addAttribute("stylists", stylistService.readAllStylists());
                return "stylist-list";
        }
    }

    @PostMapping("/stylists")
    public String handleStylistsPost(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String currentPassword,
            @RequestParam(required = false) String newPassword,
            @RequestParam(required = false) String confirmPassword,
            @RequestParam(required = false) String specialty,
            @RequestParam(required = false) String level,
            @RequestParam(required = false) Boolean available,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        if (session.getAttribute("staffRole") == null) {
            return "redirect:/staff-login";
        }
        if (action == null || action.isBlank()) {
            return "redirect:/stylists?action=list";
        }

        switch (action.toLowerCase()) {
            case "change-password":
                String loggedInEmail = (String) session.getAttribute("staffEmail");
                if (loggedInEmail == null || !"STYLIST".equalsIgnoreCase((String) session.getAttribute("staffRole"))) {
                    return "redirect:/staff-login";
                }
                try {
                    stylistService.changeStylistPassword(loggedInEmail, currentPassword, newPassword, confirmPassword);
                } catch (IllegalArgumentException ex) {
                    redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
                    return "redirect:/stylist-profile";
                }
                redirectAttributes.addFlashAttribute("successMessage", "Your password has been successfully updated.");
                return "redirect:/stylist-profile";

            case "new":
            case "register":
                if (!SecurityUtils.isManager(session)) {
                    return "redirect:/admin?error=unauthorized";
                }
                if (name != null && email != null && specialty != null && level != null) {
                    Stylist stylist = new Stylist(
                            0,
                            name,
                            email,
                            "",
                            specialty,
                            level,
                            available != null && available,
                            "default.jpg"
                    );
                    try {
                        PasswordSetupToken setupToken = stylistService.saveAdminCreatedStylist(stylist);
                        addSetupLinkFlash(redirectAttributes, "stylist", name, setupToken);
                        redirectAttributes.addFlashAttribute("successMessage",
                                "Stylist created. Share the first-time password setup link below.");
                    } catch (DuplicateEmailException | IllegalArgumentException ex) {
                        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
                        return "redirect:/stylists?action=register";
                    }
                }
                break;
            case "edit":
            case "update":
                if (!SecurityUtils.isManager(session)) {
                    return "redirect:/admin?error=unauthorized";
                }
                if (userId != null) {
                    Stylist existing = stylistService.findById(userId);
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
                        try {
                            stylistService.updateStylist(existing);
                        } catch (DuplicateEmailException ex) {
                            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
                            return "redirect:/stylists?action=update&userId=" + userId;
                        }
                    }
                }
                break;
            case "availability":
                if (!SecurityUtils.isManager(session)) {
                    return "redirect:/admin?error=unauthorized";
                }
                if (userId != null) {
                    Stylist stylist = stylistService.findById(userId);
                    if (stylist != null) {
                        stylist.setAvailable(available != null && available);
                        try {
                            stylistService.updateStylist(stylist);
                        } catch (DuplicateEmailException ex) {
                            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
                            return "redirect:/stylists?action=availability";
                        }
                    }
                }
                return "redirect:/stylists?action=availability";
            case "regenerate-password-setup":
                if (!SecurityUtils.isManager(session)) {
                    return "redirect:/admin?error=unauthorized";
                }
                if (userId != null) {
                    Stylist stylist = stylistService.findById(userId);
                    try {
                        PasswordSetupToken setupToken = stylistService.regeneratePasswordSetupToken(userId);
                        addSetupLinkFlash(redirectAttributes, "stylist", stylist != null ? stylist.getName() : "Stylist", setupToken);
                        redirectAttributes.addFlashAttribute("successMessage",
                                "Password setup link regenerated. Share the new link below.");
                    } catch (IllegalArgumentException ex) {
                        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
                    }
                }
                return "redirect:/stylists?action=list";
            default:
                break;
        }

        return "redirect:/stylists?action=list";
    }

    private void addSetupLinkFlash(RedirectAttributes redirectAttributes, String accountType, String accountName, PasswordSetupToken setupToken) {
        String setupLink = ServletUriComponentsBuilder.fromCurrentContextPath()
                .path("/password-setup")
                .queryParam("type", accountType)
                .queryParam("token", setupToken.rawToken())
                .toUriString();
        redirectAttributes.addFlashAttribute("setupLink", setupLink);
        redirectAttributes.addFlashAttribute("setupAccountName", accountName);
        redirectAttributes.addFlashAttribute("setupAccountType", accountType);
        redirectAttributes.addFlashAttribute("setupLinkExpiresAt", setupToken.expiresAt());
    }
}
