package com.example.beautysalonreview;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;

@Controller
public class StaffWebController {

    private final EmployeeFileManager employeeFileManager = new EmployeeFileManager();

    @GetMapping("/staff-login")
    public String showStaffLogin(HttpSession session) {
        if (session.getAttribute("staffRole") != null) {
            return "redirect:/admin";
        }
        return "staff-login";
    }

    @PostMapping("/staff-login")
    public String processStaffLogin(@RequestParam String username, @RequestParam String password, HttpSession session, Model model) {
        Employee emp = employeeFileManager.authenticate(username, password);
        if (emp != null) {
            session.setAttribute("staffRole", emp.getRole());
            session.setAttribute("staffName", emp.getFullName());
            return "redirect:/admin";
        }
        model.addAttribute("error", "Invalid credentials. Access denied.");
        return "staff-login";
    }

    @GetMapping("/staff-logout")
    public String staffLogout(HttpSession session) {
        session.invalidate();
        return "redirect:/staff-login";
    }
}
