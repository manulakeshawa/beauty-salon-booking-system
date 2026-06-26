package com.manula.beautysalon.controller;

import com.manula.beautysalon.model.Appointment;
import com.manula.beautysalon.model.Customer;
import com.manula.beautysalon.model.Review;
import com.manula.beautysalon.service.AppointmentService;
import com.manula.beautysalon.service.CustomerService;
import com.manula.beautysalon.service.DuplicateEmailException;
import com.manula.beautysalon.service.EmailService;
import com.manula.beautysalon.service.PasswordSetupEmailResult;
import com.manula.beautysalon.service.PasswordSetupToken;
import com.manula.beautysalon.service.ReviewService;
import com.manula.beautysalon.security.SalonUserPrincipal;
import com.manula.beautysalon.security.SecuritySessionService;
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
public class CustomerWebController {

    private final CustomerService customerService;
    private final AppointmentService appointmentService;
    private final ReviewService reviewService;
    private final EmailService emailService;
    private final SecuritySessionService securitySessionService;

    public CustomerWebController(CustomerService customerService, AppointmentService appointmentService, ReviewService reviewService, EmailService emailService, SecuritySessionService securitySessionService) {
        this.customerService = customerService;
        this.appointmentService = appointmentService;
        this.reviewService = reviewService;
        this.emailService = emailService;
        this.securitySessionService = securitySessionService;
    }

    @GetMapping("/customers")
    public String handleCustomersGet(
            @RequestParam(defaultValue = "list") String action,
            @RequestParam(required = false) Integer userId,
            HttpSession session,
            Model model
    ) {
        switch (action.toLowerCase()) {
            case "register":
                if (!SecurityUtils.isAdmin()) {
                    return adminAccessRedirect();
                }
                model.addAttribute("generatedUserId", customerService.generateNextCustomerId());
                return "customer-register";

            case "public-register":
                model.addAttribute("generatedUserId", customerService.generateNextCustomerId());
                return "public-register";

            case "login":
                SalonUserPrincipal principal = securitySessionService.currentPrincipal();
                if (principal != null) {
                    // Visiting the public login page while already signed in should preserve
                    // the session and send each account type back to its own workspace.
                    return dashboardRedirectFor(principal);
                }
                return "customer-login";

            case "edit":
            case "update":
                if (!SecurityUtils.isAdmin()) {
                    return adminAccessRedirect();
                }
                if (userId != null) {
                    Customer existing = customerService.findById(userId);
                    if (existing != null) {
                        model.addAttribute("customer", existing);
                        return "customer-update";
                    }
                }
                return "redirect:/customers?action=list";

            case "delete":
                if (!SecurityUtils.isAdmin()) {
                    return adminAccessRedirect();
                }
                return "redirect:/customers?action=list";

            case "list":
            default:
                if (!SecurityUtils.isAdmin()) {
                    return adminAccessRedirect();
                }
                List<Customer> customers = customerService.readAllCustomers();
                model.addAttribute("customers", customers);
                return "customer-list";
        }
    }

    @PostMapping("/customers")
    public String handleCustomersPost(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String currentPassword,
            @RequestParam(required = false) String newPassword,
            @RequestParam(required = false) String confirmPassword,
            @RequestParam(required = false) String customerType,
            HttpSession session,
            HttpServletRequest request,
            HttpServletResponse response,
            RedirectAttributes redirectAttributes
    ) {
        if (action == null || action.isBlank()) {
            return "redirect:/customers?action=list";
        }

        switch (action.toLowerCase()) {
            case "change-password":
                SalonUserPrincipal customerPrincipal = securitySessionService.currentPrincipal();
                if (customerPrincipal == null || !customerPrincipal.isCustomer()) {
                    return "redirect:/customers?action=login";
                }
                try {
                    customerService.changeCustomerPassword(customerPrincipal.getEmail(), currentPassword, newPassword, confirmPassword);
                } catch (IllegalArgumentException ex) {
                    redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
                    return "redirect:/customer-profile";
                }
                redirectAttributes.addFlashAttribute("successMessage", "Your password has been successfully updated.");
                return "redirect:/customer-profile";

            case "register":
                if (!SecurityUtils.isAdmin()) {
                    return adminAccessRedirect();
                }
                if (name != null && email != null && customerType != null) {
                    // Admin-created customers receive setup links so staff never need to know
                    // or type a customer's initial password.
                    Customer customer = new Customer(0, name, email, "", customerType);
                    try {
                        PasswordSetupToken setupToken = customerService.saveAdminCreatedCustomer(customer);
                        PasswordSetupEmailResult emailResult = emailService.sendPasswordSetupEmail(
                                customer.getEmail(),
                                customer.getName(),
                                "customer",
                                setupToken
                        );
                        addEmailDeliveryFlash(
                                redirectAttributes,
                                emailResult,
                                "Account created and password setup email sent.",
                                "Account created, but setup email could not be sent. Please check mail settings and resend the setup email."
                        );
                    } catch (DuplicateEmailException | IllegalArgumentException ex) {
                        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
                        return "redirect:/customers?action=register";
                    }
                }
                break;

            case "public-register":
                if (name != null && email != null && password != null && customerType != null) {
                    Customer customer = new Customer(0, name, email, password, customerType);
                    try {
                        customerService.saveCustomer(customer);
                    } catch (DuplicateEmailException | IllegalArgumentException ex) {
                        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
                        return "redirect:/customers?action=public-register";
                    }
                    return "redirect:/customers?action=login";
                }
                break;

            case "login":
                if (email != null && password != null) {
                    try {
                        securitySessionService.loginCustomer(email, password, request, response);
                        // Customers land on their portal because bookings, receipts, and reviews
                        // are customer-specific after authentication.
                        return "redirect:/my-portal";
                    } catch (AuthenticationException ex) {
                        if (customerService.isPasswordSetupPending(email)) {
                            redirectAttributes.addFlashAttribute("errorMessage", CustomerService.PASSWORD_SETUP_PENDING_LOGIN_MESSAGE);
                            return "redirect:/customers?action=login";
                        }
                    }
                }
                return "redirect:/customers?action=login&error=1";

            case "edit":
            case "update":
                if (!SecurityUtils.isAdmin()) {
                    return adminAccessRedirect();
                }
                if (userId != null) {
                    Customer existing = customerService.findById(userId);
                    if (existing != null) {
                        if (name != null) {
                            existing.setName(name);
                        }
                        if (email != null) {
                            existing.setEmail(email);
                        }
                        if (password != null) {
                            existing.setPassword(password);
                        }
                        if (customerType != null) {
                            existing.setCustomerType(customerType);
                        }
                        try {
                            customerService.updateCustomer(existing);
                        } catch (DuplicateEmailException ex) {
                            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
                            return "redirect:/customers?action=update&userId=" + userId;
                        }
                    }
                }
                break;
            case "delete":
                if (!SecurityUtils.isAdmin()) {
                    return adminAccessRedirect();
                }
                if (userId != null) {
                    customerService.deleteCustomer(userId);
                }
                break;
            case "regenerate-password-setup":
                if (!SecurityUtils.isAdmin()) {
                    return adminAccessRedirect();
                }
                if (userId != null) {
                    Customer customer = customerService.findById(userId);
                    try {
                        PasswordSetupToken setupToken = customerService.regeneratePasswordSetupToken(userId);
                        PasswordSetupEmailResult emailResult = emailService.sendPasswordSetupEmail(
                                customer.getEmail(),
                                customer.getName(),
                                "customer",
                                setupToken
                        );
                        addEmailDeliveryFlash(
                                redirectAttributes,
                                emailResult,
                                "Password setup email resent.",
                                "New setup email could not be sent. Please check mail settings and resend the setup email."
                        );
                    } catch (IllegalArgumentException ex) {
                        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
                    }
                }
                return "redirect:/customers?action=list";
            default:
                break;
        }

        return "redirect:/customers?action=list";
    }

    @GetMapping("/my-portal")
    public String showCustomerPortal(HttpSession session, Model model) {
        SalonUserPrincipal principal = securitySessionService.currentPrincipal();
        if (principal == null || !principal.isCustomer()) {
            return "redirect:/customers?action=login";
        }

        String userName = principal.getDisplayName();
        model.addAttribute("userName", userName);

        List<Appointment> myAppointments = appointmentService.findByCustomerName(userName);
        model.addAttribute("myAppointments", myAppointments);

        List<Review> myReviews = reviewService.getReviewsByCustomerName(userName);
        model.addAttribute("myReviews", myReviews);

        return "customer-portal";
    }

    @GetMapping("/customer-profile")
    public String showCustomerProfile(HttpSession session, Model model) {
        SalonUserPrincipal principal = securitySessionService.currentPrincipal();
        if (principal == null || !principal.isCustomer()) {
            return "redirect:/customers?action=login";
        }

        Customer customer = customerService.findByEmail(principal.getEmail());
        if (customer == null) {
            session.invalidate();
            return "redirect:/customers?action=login";
        }

        model.addAttribute("customer", customer);
        return "customer-profile";
    }

    @PostMapping("/customer-profile")
    public String updateCustomerProfile(
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
        if (principal == null || !principal.isCustomer()) {
            return "redirect:/customers?action=login";
        }

        try {
            if ("update-profile".equalsIgnoreCase(action)) {
                Customer updated = customerService.updateCustomerProfile(principal.getEmail(), name, email);
                // Refresh the principal so navigation and ownership checks use the updated
                // name/email immediately in the same browser session.
                securitySessionService.refreshCustomer(updated, request, response);
                redirectAttributes.addFlashAttribute("successMessage", "Your profile has been successfully updated.");
            } else if ("change-password".equalsIgnoreCase(action)) {
                customerService.changeCustomerPassword(principal.getEmail(), currentPassword, newPassword, confirmPassword);
                redirectAttributes.addFlashAttribute("successMessage", "Your password has been successfully updated.");
            }
        } catch (DuplicateEmailException | IllegalArgumentException ex) {
            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
        }

        return "redirect:/customer-profile";
    }

    private void addEmailDeliveryFlash(RedirectAttributes redirectAttributes, PasswordSetupEmailResult emailResult, String successMessage, String warningMessage) {
        if (emailResult.emailSent()) {
            redirectAttributes.addFlashAttribute("successMessage", successMessage);
        } else {
            redirectAttributes.addFlashAttribute("warningMessage", warningMessage);
        }
    }

    private String adminAccessRedirect() {
        return SecurityUtils.isAuthenticated() ? "redirect:/access-denied" : "redirect:/staff-login";
    }

    private String dashboardRedirectFor(SalonUserPrincipal principal) {
        // Each role has a different default workspace and should not be dropped onto a
        // generic page after already-authenticated navigation.
        if (principal.isCustomer()) {
            return "redirect:/my-portal";
        }
        if (principal.isStylist()) {
            return "redirect:/stylist-portal";
        }
        if (principal.isAdmin()) {
            return "redirect:/admin";
        }
        return "redirect:/";
    }
}
