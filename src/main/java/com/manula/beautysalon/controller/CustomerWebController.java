package com.manula.beautysalon.controller;

import com.manula.beautysalon.model.Appointment;
import com.manula.beautysalon.model.Customer;
import com.manula.beautysalon.model.Review;
import com.manula.beautysalon.service.AppointmentService;
import com.manula.beautysalon.service.CustomerService;
import com.manula.beautysalon.service.DuplicateEmailException;
import com.manula.beautysalon.service.ReviewService;
import jakarta.servlet.http.HttpSession;
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

    public CustomerWebController(CustomerService customerService, AppointmentService appointmentService, ReviewService reviewService) {
        this.customerService = customerService;
        this.appointmentService = appointmentService;
        this.reviewService = reviewService;
    }

    @GetMapping("/customers")
    public String handleCustomersGet(
            @RequestParam(defaultValue = "list") String action,
            @RequestParam(required = false) Integer userId,
            HttpSession session,
            Model model
    ) {
        if (!"public-register".equalsIgnoreCase(action)
                && !"login".equalsIgnoreCase(action)
                && session.getAttribute("staffRole") == null) {
            return "redirect:/staff-login";
        }

        String role = (String) session.getAttribute("staffRole");

        switch (action.toLowerCase()) {
            case "register":
                if (!"MANAGER".equals(role)) {
                    return "redirect:/admin?error=unauthorized";
                }
                model.addAttribute("generatedUserId", customerService.generateNextCustomerId());
                return "customer-register";

            case "public-register":
                model.addAttribute("generatedUserId", customerService.generateNextCustomerId());
                return "public-register";

            case "login":
                return "customer-login";

            case "edit":
            case "update":
                if (!"MANAGER".equals(role)) {
                    return "redirect:/admin?error=unauthorized";
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
                if (!"MANAGER".equals(role)) {
                    return "redirect:/admin?error=unauthorized";
                }
                if (userId != null) {
                    customerService.deleteCustomer(userId);
                }
                return "redirect:/customers?action=list";

            case "list":
            default:
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
            @RequestParam(required = false) String newPassword,
            @RequestParam(required = false) String customerType,
            HttpSession session,
            RedirectAttributes redirectAttributes
    ) {
        if (!"public-register".equalsIgnoreCase(action)
                && !"login".equalsIgnoreCase(action)
                && !"change-password".equalsIgnoreCase(action)
                && session.getAttribute("staffRole") == null) {
            return "redirect:/staff-login";
        }

        String role = (String) session.getAttribute("staffRole");

        if (action == null || action.isBlank()) {
            return "redirect:/customers?action=list";
        }

        switch (action.toLowerCase()) {
            case "change-password":
                String loggedInEmail = (String) session.getAttribute("loggedInCustomerEmail");
                if (loggedInEmail == null) {
                    return "redirect:/customers?action=login";
                }
                if (newPassword != null && !newPassword.isBlank()) {
                    Customer customer = customerService.findByEmail(loggedInEmail);
                    if (customer != null) {
                        customer.setPassword(newPassword);
                        try {
                            customerService.updateCustomer(customer);
                        } catch (DuplicateEmailException ex) {
                            redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
                            return "redirect:/my-portal?passwordStatus=emailConflict";
                        }
                    }
                }
                return "redirect:/my-portal?passwordStatus=updated";

            case "register":
                if (!"MANAGER".equals(role)) {
                    return "redirect:/admin?error=unauthorized";
                }
                if (name != null && email != null && customerType != null) {
                    Customer customer = new Customer(0, name, email, "lumiere2026", customerType);
                    try {
                        customerService.saveCustomer(customer);
                    } catch (DuplicateEmailException ex) {
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
                    } catch (DuplicateEmailException ex) {
                        redirectAttributes.addFlashAttribute("errorMessage", ex.getMessage());
                        return "redirect:/customers?action=public-register";
                    }
                    return "redirect:/customers?action=login";
                }
                break;

            case "login":
                if (email != null && password != null) {
                    Customer authenticated = customerService.findByEmailAndPassword(email, password);
                    if (authenticated != null) {
                        session.setAttribute("loggedInCustomerEmail", authenticated.getEmail());
                        session.setAttribute("loggedInCustomerName", authenticated.getName());
                        return "redirect:/my-portal";
                    }
                }
                return "redirect:/customers?action=login&error=1";

            case "edit":
            case "update":
                if (!"MANAGER".equals(role)) {
                    return "redirect:/admin?error=unauthorized";
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
            default:
                break;
        }

        return "redirect:/customers?action=list";
    }

    @GetMapping("/my-portal")
    public String showCustomerPortal(HttpSession session, Model model) {
        String userEmail = (String) session.getAttribute("loggedInCustomerEmail");
        String userName = (String) session.getAttribute("loggedInCustomerName");

        if (userEmail == null) {
            return "redirect:/customers?action=login";
        }

        model.addAttribute("userName", userName);

        List<Appointment> myAppointments = appointmentService.findByCustomerName(userName);
        model.addAttribute("myAppointments", myAppointments);

        List<Review> myReviews = reviewService.getReviewsByCustomerName(userName);
        model.addAttribute("myReviews", myReviews);

        return "customer-portal";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/customers?action=login";
    }
}
