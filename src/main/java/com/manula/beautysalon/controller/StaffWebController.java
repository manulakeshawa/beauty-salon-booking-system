package com.manula.beautysalon.controller;

import com.manula.beautysalon.model.Appointment;
import com.manula.beautysalon.model.Employee;
import com.manula.beautysalon.model.Stylist;
import com.manula.beautysalon.repository.file.AppointmentFileManager;
import com.manula.beautysalon.repository.file.EmployeeFileManager;
import com.manula.beautysalon.repository.file.StylistFileManager;
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
public class StaffWebController {

    private final EmployeeFileManager employeeFileManager = new EmployeeFileManager();
    private final AppointmentFileManager appointmentFileManager = new AppointmentFileManager();
    // NEW: Bring in the Stylist Manager so we can check their text file!
    private final StylistFileManager stylistFileManager = new StylistFileManager();

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

        // Step 1: Check if it's a Manager (Employees.txt)
        Employee emp = employeeFileManager.authenticate(username, password);
        if (emp != null && "MANAGER".equalsIgnoreCase(emp.getRole())) {
            session.setAttribute("staffRole", emp.getRole());
            session.setAttribute("staffName", emp.getFullName());
            session.setAttribute("staffEmail", username);
            return "redirect:/admin";
        }

        // Step 2: If not a Manager, check if it's a Stylist (Stylists.txt)
        try {
            Stylist stylist = stylistFileManager.authenticate(username, password);
            if (stylist != null) {
                // Save Stylist identity to session
                session.setAttribute("staffRole", "STYLIST");
                session.setAttribute("staffName", stylist.getName());
                session.setAttribute("staffEmail", stylist.getEmail());
                return "redirect:/stylist-portal";
            }
        } catch (IOException e) {
            System.err.println("File Error in Login: " + e.getMessage());
        }

        // Step 3: If found in neither, reject them
        model.addAttribute("error", "Invalid credentials. Access denied.");
        return "staff-login";
    }

    // ==========================================================
    // THE STYLIST PORTAL LOGIC
    // ==========================================================
    @GetMapping("/stylist-portal")
    public String showStylistPortal(HttpSession session, Model model) {
        String role = (String) session.getAttribute("staffRole");
        String stylistName = (String) session.getAttribute("staffName");

        if (role == null) {
            return "redirect:/staff-login";
        }

        model.addAttribute("stylistName", stylistName);

        try {
            List<Appointment> mySchedule = appointmentFileManager.readAllAppointments().stream()
                    .filter(appt -> stylistName != null && stylistName.equalsIgnoreCase(appt.getStylistName()))
                    .collect(java.util.stream.Collectors.toList());

            model.addAttribute("mySchedule", mySchedule);

        } catch (IOException e) {
            model.addAttribute("mySchedule", new ArrayList<>());
            System.err.println("🚨 Portal Error: " + e.getMessage());
        }

        return "stylist-portal";
    }

    @GetMapping("/staff-logout")
    public String staffLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/staff-login";
    }
}
