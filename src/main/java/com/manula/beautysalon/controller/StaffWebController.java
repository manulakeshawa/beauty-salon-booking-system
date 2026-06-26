package com.manula.beautysalon.controller;

import com.manula.beautysalon.model.Appointment;
import com.manula.beautysalon.model.Employee;
import com.manula.beautysalon.model.Stylist;
import com.manula.beautysalon.security.SalonUserPrincipal;
import com.manula.beautysalon.security.SecuritySessionService;
import com.manula.beautysalon.service.DuplicateEmailException;
import com.manula.beautysalon.service.AppointmentService;
import com.manula.beautysalon.service.StaffService;
import com.manula.beautysalon.service.StylistService;
import com.manula.beautysalon.util.SecurityUtils;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
public class StaffWebController {

    private final StaffService staffService;
    private final AppointmentService appointmentService;
    private final StylistService stylistService;
    private final SecuritySessionService securitySessionService;

    public StaffWebController(StaffService staffService, AppointmentService appointmentService, StylistService stylistService, SecuritySessionService securitySessionService) {
        this.staffService = staffService;
        this.appointmentService = appointmentService;
        this.stylistService = stylistService;
        this.securitySessionService = securitySessionService;
    }

    @GetMapping("/staff-login")
    public String showStaffLogin() {
        SalonUserPrincipal principal = securitySessionService.currentPrincipal();
        if (principal != null) {
            if (principal.isAdmin()) {
                return "redirect:/admin";
            }
            if (principal.isStylist()) {
                return "redirect:/stylist-portal";
            }
            if (principal.isCustomer()) {
                return "redirect:/my-portal";
            }
        }
        return "staff-login";
    }

    @PostMapping("/staff-login")
    public String processStaffLogin(
            @RequestParam String username,
            @RequestParam String password,
            HttpServletRequest request,
            HttpServletResponse response,
            Model model
    ) {
        try {
            SalonUserPrincipal principal = securitySessionService.loginStaff(username, password, request, response);
            return principal.isAdmin() ? "redirect:/admin" : "redirect:/stylist-portal";
        } catch (AuthenticationException ex) {
            if (stylistService.isPasswordSetupPending(username)) {
                model.addAttribute("error", StylistService.PASSWORD_SETUP_PENDING_LOGIN_MESSAGE);
                return "staff-login";
            }
        }

        model.addAttribute("error", "Invalid credentials. Access denied.");
        return "staff-login";
    }

    @GetMapping("/stylist-portal")
    public String showStylistPortal(Model model) {
        SalonUserPrincipal principal = securitySessionService.currentPrincipal();
        if (principal == null || !principal.isStylist()) {
            return "redirect:/staff-login";
        }

        String stylistName = principal.getDisplayName();
        model.addAttribute("stylistName", stylistName);

        List<Appointment> mySchedule = appointmentService.findByStylistName(stylistName);
        model.addAttribute("mySchedule", mySchedule);

        return "stylist-portal";
    }

    @GetMapping("/stylist-profile")
    public String showStylistProfile(HttpSession session, Model model) {
        SalonUserPrincipal principal = securitySessionService.currentPrincipal();
        if (principal == null || !principal.isStylist()) {
            return "redirect:/staff-login";
        }

        Stylist stylist = stylistService.findByEmail(principal.getEmail());
        if (stylist == null) {
            session.invalidate();
            return "redirect:/staff-login";
        }

        model.addAttribute("stylist", stylist);
        return "stylist-profile";
    }

    @PostMapping("/stylist-profile")
    public String updateStylistProfile(
            @RequestParam String action,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String currentPassword,
            @RequestParam(required = false) String newPassword,
            @RequestParam(required = false) String confirmPassword,
            HttpSession session,
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes
    ) {
        SalonUserPrincipal principal = securitySessionService.currentPrincipal();
        if (principal == null || !principal.isStylist()) {
            return "redirect:/staff-login";
        }

        try {
            if ("update-profile".equalsIgnoreCase(action)) {
                Stylist updated = stylistService.updateStylistProfile(principal.getEmail(), name, email);
                securitySessionService.refreshStylist(updated, request, response);
                redirectAttributes.addFlashAttribute("successMessage", "Your profile has been successfully updated.");
            } else if ("change-password".equalsIgnoreCase(action)) {
                stylistService.changeStylistPassword(principal.getEmail(), currentPassword, newPassword, confirmPassword);
                redirectAttributes.addFlashAttribute("successMessage", "Your password has been successfully updated.");
            }
        } catch (DuplicateEmailException | IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/stylist-profile";
    }

    @GetMapping("/admin-password")
    public String showAdminPasswordForm(HttpSession session, Model model) {
        SalonUserPrincipal principal = securitySessionService.currentPrincipal();
        if (principal == null || !principal.isAdmin()) {
            return "redirect:/staff-login";
        }

        Employee admin = staffService.findAdminAccount(principal.getUsername());
        if (admin == null) {
            session.invalidate();
            return "redirect:/staff-login";
        }

        model.addAttribute("admin", admin);
        return "admin-password";
    }

    @PostMapping("/admin-password")
    public String updateAdminAccount(
            @RequestParam(required = false, defaultValue = "change-password") String action,
            @RequestParam(name = "username", required = false) String newUsername,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String currentPassword,
            @RequestParam(required = false) String newPassword,
            @RequestParam(required = false) String confirmPassword,
            HttpSession session,
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes
    ) {
        SalonUserPrincipal principal = securitySessionService.currentPrincipal();
        if (principal == null || !principal.isAdmin()) {
            return "redirect:/staff-login";
        }

        try {
            if ("update-account".equalsIgnoreCase(action)) {
                Employee updated = staffService.updateAdminAccount(principal.getUsername(), newUsername, email);
                securitySessionService.refreshAdmin(updated, request, response);
                redirectAttributes.addFlashAttribute("successMessage", "Your admin account details have been successfully updated.");
            } else {
                staffService.changeAdminPassword(principal.getUsername(), currentPassword, newPassword, confirmPassword);
                redirectAttributes.addFlashAttribute("successMessage", "Your admin password has been successfully updated.");
            }
        } catch (DuplicateEmailException | IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin-password";
    }

    @PostMapping("/staff-logout")
    public String staffLogout(HttpServletRequest request, HttpServletResponse response) {
        securitySessionService.logout(request, response);
        return "redirect:/staff-login";
    }
}
