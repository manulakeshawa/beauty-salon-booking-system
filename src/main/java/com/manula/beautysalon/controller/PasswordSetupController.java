package com.manula.beautysalon.controller;

import com.manula.beautysalon.model.Customer;
import com.manula.beautysalon.model.Stylist;
import com.manula.beautysalon.service.CustomerService;
import com.manula.beautysalon.service.StylistService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PasswordSetupController {

    private final CustomerService customerService;
    private final StylistService stylistService;

    public PasswordSetupController(CustomerService customerService, StylistService stylistService) {
        this.customerService = customerService;
        this.stylistService = stylistService;
    }

    @GetMapping("/password-setup")
    public String showPasswordSetup(
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String token,
            Model model
    ) {
        // This page must be public because admin-created customers/stylists do not have
        // passwords yet; the emailed setup token is what proves access to the invitation.
        model.addAttribute("type", type);
        model.addAttribute("token", token);

        try {
            addAccountDetails(type, token, model);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("errorMessage", ex.getMessage());
        }

        return "password-setup";
    }

    @PostMapping("/password-setup")
    public String setupPassword(
            @RequestParam String type,
            @RequestParam String token,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            if ("customer".equalsIgnoreCase(type)) {
                customerService.setupPassword(token, newPassword, confirmPassword);
                redirectAttributes.addFlashAttribute("successMessage", "Your password has been set. You can now log in.");
                return "redirect:/customers?action=login";
            }
            if ("stylist".equalsIgnoreCase(type)) {
                // Stylists share the staff login page after setup, while customers return to
                // the customer login flow.
                stylistService.setupPassword(token, newPassword, confirmPassword);
                redirectAttributes.addFlashAttribute("successMessage", "Your password has been set. You can now log in.");
                return "redirect:/staff-login";
            }
            throw new IllegalArgumentException("This password setup link is invalid or has already been used.");
        } catch (IllegalArgumentException ex) {
            model.addAttribute("type", type);
            model.addAttribute("token", token);
            model.addAttribute("errorMessage", ex.getMessage());
            try {
                addAccountDetails(type, token, model);
            } catch (IllegalArgumentException ignored) {
            }
            return "password-setup";
        }
    }

    private void addAccountDetails(String type, String token, Model model) {
        if ("customer".equalsIgnoreCase(type)) {
            Customer customer = customerService.previewPasswordSetup(token);
            model.addAttribute("accountName", customer.getName());
            model.addAttribute("accountTypeLabel", "Customer");
            return;
        }
        if ("stylist".equalsIgnoreCase(type)) {
            Stylist stylist = stylistService.previewPasswordSetup(token);
            model.addAttribute("accountName", stylist.getName());
            model.addAttribute("accountTypeLabel", "Stylist");
            return;
        }
        throw new IllegalArgumentException("This password setup link is invalid or has already been used.");
    }
}
