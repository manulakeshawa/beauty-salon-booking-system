package com.manula.beautysalon.controller;

import com.manula.beautysalon.model.Appointment;
import com.manula.beautysalon.model.Employee;
import com.manula.beautysalon.model.Stylist;
import com.manula.beautysalon.service.AppointmentService;
import com.manula.beautysalon.service.StaffService;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class StaffWebController {

    private final StaffService staffService;
    private final AppointmentService appointmentService;

    public StaffWebController(StaffService staffService, AppointmentService appointmentService) {
        this.staffService = staffService;
        this.appointmentService = appointmentService;
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
            session.setAttribute("staffEmail", username);
            return "redirect:/admin";
        }

        Stylist stylist = staffService.authenticateStylist(username, password);
        if (stylist != null) {
            session.setAttribute("staffRole", "STYLIST");
            session.setAttribute("staffName", stylist.getName());
            session.setAttribute("staffEmail", stylist.getEmail());
            return "redirect:/stylist-portal";
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

    @GetMapping("/staff-logout")
    public String staffLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/staff-login";
    }
}
