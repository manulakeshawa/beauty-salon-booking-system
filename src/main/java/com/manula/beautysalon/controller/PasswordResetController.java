package com.manula.beautysalon.controller;

import com.manula.beautysalon.service.PasswordResetService;
import com.manula.beautysalon.service.PasswordResetService.PasswordResetCompletion;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class PasswordResetController {

    private final PasswordResetService passwordResetService;

    public PasswordResetController(PasswordResetService passwordResetService) {
        this.passwordResetService = passwordResetService;
    }

    @GetMapping("/forgot-password")
    public String showForgotPassword() {
        return "forgot-password";
    }

    @PostMapping("/forgot-password")
    public String requestPasswordReset(
            @RequestParam(required = false) String email,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            passwordResetService.requestPasswordReset(email);
            redirectAttributes.addFlashAttribute("successMessage", PasswordResetService.GENERIC_RESET_REQUEST_MESSAGE);
            return "redirect:/forgot-password";
        } catch (IllegalArgumentException ex) {
            model.addAttribute("email", email);
            model.addAttribute("errorMessage", ex.getMessage());
            return "forgot-password";
        }
    }

    @GetMapping("/reset-password")
    public String showResetPassword(
            @RequestParam(required = false) String token,
            Model model
    ) {
        model.addAttribute("token", token);
        addResetTokenState(token, model);
        return "reset-password";
    }

    @PostMapping("/reset-password")
    public String resetPassword(
            @RequestParam(required = false) String token,
            @RequestParam(required = false) String newPassword,
            @RequestParam(required = false) String confirmPassword,
            Model model,
            RedirectAttributes redirectAttributes
    ) {
        try {
            PasswordResetCompletion completion = passwordResetService.resetPassword(token, newPassword, confirmPassword);
            redirectAttributes.addFlashAttribute("successMessage", "Your password has been reset. You can now log in.");
            return "redirect:" + completion.loginPath();
        } catch (IllegalArgumentException ex) {
            model.addAttribute("token", token);
            model.addAttribute("errorMessage", ex.getMessage());
            addResetTokenState(token, model);
            return "reset-password";
        }
    }

    private void addResetTokenState(String token, Model model) {
        try {
            passwordResetService.previewReset(token);
            model.addAttribute("validToken", true);
        } catch (IllegalArgumentException ex) {
            model.addAttribute("validToken", false);
            if (!model.containsAttribute("errorMessage")) {
                model.addAttribute("errorMessage", ex.getMessage());
            }
        }
    }
}
