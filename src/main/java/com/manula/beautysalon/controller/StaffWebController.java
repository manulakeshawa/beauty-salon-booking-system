package com.manula.beautysalon.controller;

import com.manula.beautysalon.model.Appointment;
import com.manula.beautysalon.model.Employee;
import com.manula.beautysalon.model.Stylist;
import com.manula.beautysalon.service.DuplicateEmailException;
import com.manula.beautysalon.service.AppointmentService;
import com.manula.beautysalon.service.StaffService;
import com.manula.beautysalon.service.StylistService;
import jakarta.servlet.http.HttpSession;
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

    public StaffWebController(StaffService staffService, AppointmentService appointmentService, StylistService stylistService) {
        this.staffService = staffService;
        this.appointmentService = appointmentService;
        this.stylistService = stylistService;
    }

    @GetMapping("/staff-login")
    public String showStaffLogin(HttpSession session) {
        String role = (String) session.getAttribute("staffRole");
        if (role != null) {
            return "MANAGER".equalsIgnoreCase(role) ? "redirect:/admin" : "redirect:/stylist-portal";
        }
        return "staff-login";
    }

    @PostMapping("/staff-login")
    public String processStaffLogin(@RequestParam String username, @RequestParam String password, HttpSession session, Model model) {

        Employee employee = staffService.authenticateEmployee(username, password);
        if (employee != null && "MANAGER".equalsIgnoreCase(employee.getRole())) {
            session.setAttribute("staffRole", employee.getRole());
            session.setAttribute("staffName", employee.getFullName());
            session.setAttribute("staffEmail", employee.getEmail());
            session.setAttribute("staffUsername", employee.getUsername());
            return "redirect:/admin";
        }

        Stylist stylist = staffService.authenticateStylist(username, password);
        if (stylist != null) {
            session.setAttribute("staffRole", "STYLIST");
            session.setAttribute("staffName", stylist.getName());
            session.setAttribute("staffEmail", stylist.getEmail());
            session.setAttribute("staffUserId", stylist.getUserId());
            return "redirect:/stylist-portal";
        }

        if (stylistService.isPasswordSetupPending(username)) {
            model.addAttribute("error", StylistService.PASSWORD_SETUP_PENDING_LOGIN_MESSAGE);
            return "staff-login";
        }

        model.addAttribute("error", "Invalid credentials. Access denied.");
        return "staff-login";
    }

    @GetMapping("/stylist-portal")
    public String showStylistPortal(HttpSession session, Model model) {
        String role = (String) session.getAttribute("staffRole");
        String stylistName = (String) session.getAttribute("staffName");

        if (role == null) {
            return "redirect:/staff-login";
        }

        model.addAttribute("stylistName", stylistName);

        List<Appointment> mySchedule = appointmentService.findByStylistName(stylistName);
        model.addAttribute("mySchedule", mySchedule);

        return "stylist-portal";
    }

    @GetMapping("/stylist-profile")
    public String showStylistProfile(HttpSession session, Model model) {
        if (!"STYLIST".equalsIgnoreCase((String) session.getAttribute("staffRole"))) {
            return "redirect:/staff-login";
        }

        String stylistEmail = (String) session.getAttribute("staffEmail");
        Stylist stylist = stylistService.findByEmail(stylistEmail);
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
            RedirectAttributes redirectAttributes
    ) {
        if (!"STYLIST".equalsIgnoreCase((String) session.getAttribute("staffRole"))) {
            return "redirect:/staff-login";
        }

        String stylistEmail = (String) session.getAttribute("staffEmail");
        try {
            if ("update-profile".equalsIgnoreCase(action)) {
                Stylist updated = stylistService.updateStylistProfile(stylistEmail, name, email);
                session.setAttribute("staffEmail", updated.getEmail());
                session.setAttribute("staffName", updated.getName());
                session.setAttribute("staffUserId", updated.getUserId());
                redirectAttributes.addFlashAttribute("successMessage", "Your profile has been successfully updated.");
            } else if ("change-password".equalsIgnoreCase(action)) {
                stylistService.changeStylistPassword(stylistEmail, currentPassword, newPassword, confirmPassword);
                redirectAttributes.addFlashAttribute("successMessage", "Your password has been successfully updated.");
            }
        } catch (DuplicateEmailException | IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/stylist-profile";
    }

    @GetMapping("/admin-password")
    public String showAdminPasswordForm(HttpSession session, Model model) {
        if (!"MANAGER".equalsIgnoreCase((String) session.getAttribute("staffRole"))) {
            return "redirect:/staff-login";
        }

        Employee admin = staffService.findAdminAccount((String) session.getAttribute("staffUsername"));
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
            RedirectAttributes redirectAttributes
    ) {
        if (!"MANAGER".equalsIgnoreCase((String) session.getAttribute("staffRole"))) {
            return "redirect:/staff-login";
        }

        String username = (String) session.getAttribute("staffUsername");
        try {
            if ("update-account".equalsIgnoreCase(action)) {
                Employee updated = staffService.updateAdminAccount(username, newUsername, email);
                session.setAttribute("staffEmail", updated.getEmail());
                session.setAttribute("staffName", updated.getFullName());
                session.setAttribute("staffUsername", updated.getUsername());
                redirectAttributes.addFlashAttribute("successMessage", "Your admin account details have been successfully updated.");
            } else {
                staffService.changeAdminPassword(username, currentPassword, newPassword, confirmPassword);
                redirectAttributes.addFlashAttribute("successMessage", "Your admin password has been successfully updated.");
            }
        } catch (DuplicateEmailException | IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/admin-password";
    }

    @GetMapping("/staff-logout")
    public String staffLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/staff-login";
    }
}
