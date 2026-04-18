package com.example.beautysalonreview;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import jakarta.servlet.http.HttpSession; // NEW: Added for session tracking
import java.io.IOException;
import java.util.List;

@Controller
public class AppointmentWebController {

    private final AppointmentFileManager appointmentFileManager = new AppointmentFileManager();

    @GetMapping("/appointments")
    public String handleAppointmentsGet(
            @RequestParam(defaultValue = "list") String action,
            @RequestParam(required = false) Integer appointmentId,
            HttpSession session, // NEW: Added so we can check who is logged in!
            Model model
    ) {
        if (!"public-book".equalsIgnoreCase(action) && session.getAttribute("staffRole") == null) { return "redirect:/staff-login"; }
        try {
            switch (action.toLowerCase()) {
                case "new":
                    if (!SecurityUtils.isManager(session)) {
                        return "redirect:/admin?error=unauthorized";
                    }
                    model.addAttribute("generatedAppointmentId", appointmentFileManager.generateNextAppointmentId());
                    return "book-appointment"; // The Admin View
                
                // ==========================================================
                // NEW: THE PUBLIC BOOKING ROUTE
                // ==========================================================
                case "public-book":
                    String loggedInName = (String) session.getAttribute("loggedInCustomerName");
                    
                    // Security Check: If they aren't logged in, boot them to the login page
                    if (loggedInName == null) {
                        return "redirect:/customers?action=login"; 
                    }
                    
                    // Pre-fill their details!
                    model.addAttribute("customerName", loggedInName);
                    model.addAttribute("generatedAppointmentId", appointmentFileManager.generateNextAppointmentId());
                    
                    return "public-book-appointment"; // The new HTML file we will build next

                case "edit":
                    if (!SecurityUtils.isManager(session)) {
                        return "redirect:/admin?error=unauthorized";
                    }
                    if (appointmentId == null) {
                        return "redirect:/appointments?action=list";
                    }
                    Appointment appointmentToEdit = findById(appointmentId);
                    if (appointmentToEdit == null) {
                        return "redirect:/appointments?action=list";
                    }
                    model.addAttribute("appointment", appointmentToEdit);
                    return "edit-appointment";
                case "delete":
                    if (!SecurityUtils.isManager(session)) {
                        return "redirect:/admin?error=unauthorized";
                    }
                    if (appointmentId != null) {
                        appointmentFileManager.deleteAppointment(appointmentId);
                    }
                    return "redirect:/appointments?action=list";
                case "list":
                default:
                    List<Appointment> appointments = appointmentFileManager.readAllAppointments();
                    model.addAttribute("appointments", appointments);
                    return "appointment-list";
            }
        } catch (IOException ignored) {
            return "redirect:/appointments?action=list";
        }
    }

    @PostMapping("/appointments")
    public String handleAppointmentsPost(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Integer appointmentId,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String serviceName,
            @RequestParam(required = false) String appointmentDate,
            @RequestParam(required = false) String appointmentTime,
            @RequestParam(required = false) String status,
            HttpSession session
    ) {
        if (!"public-book".equalsIgnoreCase(action) && session.getAttribute("staffRole") == null) { return "redirect:/staff-login"; }
        if (action == null || action.isBlank()) {
            return "redirect:/appointments?action=list";
        }
        try {
            switch (action.toLowerCase()) {
                case "new":
                    if (!SecurityUtils.isManager(session)) {
                        return "redirect:/admin?error=unauthorized";
                    }
                    if (customerName != null && serviceName != null
                            && appointmentDate != null && appointmentTime != null) {
                        int newId = appointmentFileManager.generateNextAppointmentId();
                        Appointment appointment = new Appointment(
                                newId,
                                customerName,
                                serviceName,
                                appointmentDate,
                                appointmentTime
                        );
                        appointmentFileManager.saveAppointment(appointment);
                    }
                    break;
                case "public-book":
                    if (customerName != null && serviceName != null
                            && appointmentDate != null && appointmentTime != null) {
                        int newId = appointmentFileManager.generateNextAppointmentId();
                        Appointment appointment = new Appointment(
                                newId,
                                customerName,
                                serviceName,
                                appointmentDate,
                                appointmentTime
                        );
                        appointmentFileManager.saveAppointment(appointment);
                        return "redirect:/my-portal";
                    }
                    break;
                case "update":
                    if (!SecurityUtils.isManager(session)) {
                        return "redirect:/admin?error=unauthorized";
                    }
                    if (appointmentId != null) {
                        Appointment existing = findById(appointmentId);
                        if (existing != null) {
                            if (appointmentDate != null) {
                                existing.setAppointmentDate(appointmentDate);
                            }
                            if (appointmentTime != null) {
                                existing.setAppointmentTime(appointmentTime);
                            }
                            if (status != null) {
                                existing.setStatus(status);
                            }
                            appointmentFileManager.updateAppointment(existing);
                        }
                    }
                    break;
                default:
                    break;
            }
        } catch (IOException ignored) {
            return "redirect:/appointments?action=list";
        }

        return "redirect:/appointments?action=list";
    }

    private Appointment findById(int appointmentId) throws IOException {
        List<Appointment> appointments = appointmentFileManager.readAllAppointments();
        for (Appointment appointment : appointments) {
            if (appointment.getAppointmentId() == appointmentId) {
                return appointment;
            }
        }
        return null;
    }
}