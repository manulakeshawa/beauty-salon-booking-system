package com.manula.beautysalon.controller;

import com.manula.beautysalon.model.Appointment;
import com.manula.beautysalon.model.Customer;
import com.manula.beautysalon.model.Review;
import com.manula.beautysalon.repository.file.AppointmentFileManager;
import com.manula.beautysalon.repository.file.CustomerFileManager;
import com.manula.beautysalon.repository.file.ReviewFileManager;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@Controller
public class CustomerWebController {

    private final CustomerFileManager customerFileManager = new CustomerFileManager();
    private final AppointmentFileManager appointmentFileManager = new AppointmentFileManager();
    private final ReviewFileManager reviewFileManager = new ReviewFileManager();

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

        try {
            switch (action.toLowerCase()) {
                case "register":
                    if (!"MANAGER".equals(role)) {
                        return "redirect:/admin?error=unauthorized";
                    }
                    model.addAttribute("generatedUserId", customerFileManager.generateNextCustomerId());
                    return "customer-register";

                case "public-register":
                    model.addAttribute("generatedUserId", customerFileManager.generateNextCustomerId());
                    return "public-register";

                case "login":
                    return "customer-login";

                case "edit":
                case "update":
                    if (!"MANAGER".equals(role)) {
                        return "redirect:/admin?error=unauthorized";
                    }
                    if (userId != null) {
                        Customer existing = findById(userId);
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
                        customerFileManager.deleteCustomer(userId);
                    }
                    return "redirect:/customers?action=list";

                case "list":
                default:
                    List<Customer> customers = customerFileManager.readAllCustomers();
                    model.addAttribute("customers", customers);
                    return "customer-list";
            }
        } catch (IOException e) {
            System.err.println("File Error in Customer Controller: " + e.getMessage());
            return "redirect:/customers?action=list";
        }
    }

    @PostMapping("/customers")
    public String handleCustomersPost(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Integer userId,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String email,
            @RequestParam(required = false) String password,
            @RequestParam(required = false) String newPassword, // NEW: Added parameter for the modal
            @RequestParam(required = false) String customerType,
            HttpSession session
    ) {
        // Allow logged-in customers to change their password via the portal
        if (!"public-register".equalsIgnoreCase(action)
                && !"login".equalsIgnoreCase(action)
                && !"change-password".equalsIgnoreCase(action) // Added bypass for password change
                && session.getAttribute("staffRole") == null) {
            return "redirect:/staff-login";
        }

        String role = (String) session.getAttribute("staffRole");

        if (action == null || action.isBlank()) {
            return "redirect:/customers?action=list";
        }

        try {
            switch (action.toLowerCase()) {
                case "change-password": // NEW: Logic to process the modal update
                    String loggedInEmail = (String) session.getAttribute("loggedInCustomerEmail");
                    if (loggedInEmail == null) {
                        return "redirect:/customers?action=login";
                    }
                    if (newPassword != null && !newPassword.isBlank()) {
                        List<Customer> allCusts = customerFileManager.readAllCustomers();
                        for (Customer c : allCusts) {
                            if (c.getEmail().equalsIgnoreCase(loggedInEmail)) {
                                c.setPassword(newPassword);
                                customerFileManager.updateCustomer(c);
                                break;
                            }
                        }
                    }
                    return "redirect:/my-portal?passwordStatus=updated";

                case "register":
                    if (!"MANAGER".equals(role)) {
                        return "redirect:/admin?error=unauthorized";
                    }
                    if (name != null && email != null && customerType != null) {
                        int newId = customerFileManager.generateNextCustomerId();
                        String defaultPassword = "lumiere2026";
                        Customer customer = new Customer(newId, name, email, defaultPassword, customerType);
                        customerFileManager.saveCustomer(customer);
                    }
                    break;

                case "public-register":
                    if (name != null && email != null && password != null && customerType != null) {
                        int newId = customerFileManager.generateNextCustomerId();
                        Customer customer = new Customer(newId, name, email, password, customerType);
                        customerFileManager.saveCustomer(customer);
                        return "redirect:/customers?action=login";
                    }
                    break;

                case "login":
                    if (email != null && password != null) {
                        Customer authenticated = findByEmailAndPassword(email, password);
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
                        Customer existing = findById(userId);
                        if (existing != null) {
                            if (name != null) existing.setName(name);
                            if (email != null) existing.setEmail(email);
                            if (password != null) existing.setPassword(password);
                            if (customerType != null) existing.setCustomerType(customerType);
                            customerFileManager.updateCustomer(existing);
                        }
                    }
                    break;
            }
        } catch (IOException ignored) {
            return "redirect:/customers?action=list";
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

        try {
            List<Appointment> myAppointments = appointmentFileManager.readAllAppointments().stream()
                    .filter(appt -> userName.equalsIgnoreCase(appt.getCustomerName()))
                    .collect(java.util.stream.Collectors.toList());
            model.addAttribute("myAppointments", myAppointments);

            List<Review> myReviews = reviewFileManager.readAllReviews().stream()
                    .filter(rev -> userName.equalsIgnoreCase(rev.getCustomerName()))
                    .collect(java.util.stream.Collectors.toList());
            model.addAttribute("myReviews", myReviews);

        } catch (Exception e) {
            model.addAttribute("myAppointments", new ArrayList<>());
            model.addAttribute("myReviews", new ArrayList<>());
        }

        return "customer-portal";
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate();
        return "redirect:/customers?action=login";
    }

    private Customer findById(int userId) throws IOException {
        return customerFileManager.readAllCustomers().stream()
                .filter(customer -> customer.getUserId() == userId)
                .findFirst().orElse(null);
    }

    private Customer findByEmailAndPassword(String email, String password) throws IOException {
        return customerFileManager.readAllCustomers().stream()
                .filter(c -> c.getEmail().equalsIgnoreCase(email) && c.getPassword().equals(password))
                .findFirst().orElse(null);
    }
}
