package com.example.beautysalonreview;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession; 
import java.io.IOException;
import java.util.List;
import java.util.ArrayList;

@Controller
public class CustomerWebController {

    private final CustomerFileManager customerFileManager = new CustomerFileManager();
    private final AppointmentFileManager appointmentFileManager = new AppointmentFileManager();

    @GetMapping("/customers")
    public String handleCustomersGet(
            @RequestParam(defaultValue = "list") String action,
            @RequestParam(required = false) Integer userId,
            HttpSession session,
            Model model
    ) {
        if (!"public-register".equalsIgnoreCase(action)
                && !"login".equalsIgnoreCase(action)
                && session.getAttribute("staffRole") == null) { return "redirect:/staff-login"; }
        try {
            switch (action.toLowerCase()) {
                case "register":
                    if (!SecurityUtils.isManager(session)) {
                        return "redirect:/admin?error=unauthorized";
                    }
                    model.addAttribute("generatedUserId", customerFileManager.generateNextCustomerId());
                    return "customer-register";
                
                // NEW: Added the public routing for the storefront!
                case "public-register":
                    model.addAttribute("generatedUserId", customerFileManager.generateNextCustomerId());
                    return "public-register";

                case "login":
                    return "customer-login";
                case "update":
                    if (!SecurityUtils.isManager(session)) {
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
                    if (!SecurityUtils.isManager(session)) {
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
        } catch (IOException ignored) {
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
            @RequestParam(required = false) String customerType,
            HttpSession session
    ) {
        if (!"public-register".equalsIgnoreCase(action)
                && !"login".equalsIgnoreCase(action)
                && session.getAttribute("staffRole") == null) { return "redirect:/staff-login"; }
        if (action == null || action.isBlank()) {
            return "redirect:/customers?action=list";
        }
        try {
            switch (action.toLowerCase()) {
                case "register":
                    if (!SecurityUtils.isManager(session)) {
                        return "redirect:/admin?error=unauthorized";
                    }
                    if (name != null && email != null && password != null && customerType != null) {
                        int newId = customerFileManager.generateNextCustomerId();
                        Customer customer = new Customer(newId, name, email, password, customerType);
                        customerFileManager.saveCustomer(customer);
                    }
                    break;
                // NEW: Added public-register so it knows to save the data exactly the same way!
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
                case "update":
                    if (!SecurityUtils.isManager(session)) {
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
                default:
                    break;
            }
        } catch (IOException ignored) {
            return "redirect:/customers?action=list";
        }

        return "redirect:/customers?action=list";
    }

    // ==========================================================
    // THE BOUNCER AND THE PRIVATE ROOM LOGIC
    // ==========================================================

    @GetMapping("/my-portal")
    public String showCustomerPortal(HttpSession session, Model model) {
        String userEmail = (String) session.getAttribute("loggedInCustomerEmail");
        String userName = (String) session.getAttribute("loggedInCustomerName");

        if (userEmail == null) {
            return "redirect:/customers?action=login";
        }

        model.addAttribute("userName", userName);

        try {
            List<Appointment> allAppointments = appointmentFileManager.readAllAppointments(); 
            List<Appointment> myAppointments = new ArrayList<>();
            
            for (Appointment appt : allAppointments) {
                if (userName.equalsIgnoreCase(appt.getCustomerName())) {
                    myAppointments.add(appt);
                }
            }
            model.addAttribute("myAppointments", myAppointments);
        } catch (Exception e) {
            model.addAttribute("myAppointments", new ArrayList<>()); 
        }

        return "customer-portal"; 
    }

    @GetMapping("/logout")
    public String logout(HttpSession session) {
        session.invalidate(); 
        return "redirect:/customers?action=login";
    }

    // ==========================================================
    // EXISTING HELPER METHODS
    // ==========================================================

    private Customer findById(int userId) throws IOException {
        List<Customer> customers = customerFileManager.readAllCustomers();
        for (Customer customer : customers) {
            if (customer.getUserId() == userId) {
                return customer;
            }
        }
        return null;
    }

    private Customer findByEmailAndPassword(String email, String password) throws IOException {
        List<Customer> customers = customerFileManager.readAllCustomers();
        for (Customer customer : customers) {
            if (customer.getEmail().equalsIgnoreCase(email) && customer.getPassword().equals(password)) {
                return customer;
            }
        }
        return null;
    }
}