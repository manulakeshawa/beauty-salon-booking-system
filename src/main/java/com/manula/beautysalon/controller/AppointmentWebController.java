package com.manula.beautysalon.controller;

import com.manula.beautysalon.model.Appointment;
import com.manula.beautysalon.model.SalonService;
import com.manula.beautysalon.repository.file.AppointmentFileManager;
import com.manula.beautysalon.repository.file.ServiceFileManager;
import com.manula.beautysalon.repository.file.StylistFileManager;
import com.manula.beautysalon.util.SecurityUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Controller
public class AppointmentWebController {

    private final AppointmentFileManager appointmentFileManager = new AppointmentFileManager();
    private final ServiceFileManager serviceFileManager = new ServiceFileManager();
    private final StylistFileManager stylistFileManager = new StylistFileManager();

    @GetMapping("/appointments")
    public String handleAppointmentsGet(
            @RequestParam(defaultValue = "list") String action,
            @RequestParam(required = false) Integer appointmentId,
            @RequestParam(required = false) String error,
            HttpSession session,
            Model model
    ) {
        if (!"public-book".equalsIgnoreCase(action) && session.getAttribute("staffRole") == null) {
            return "redirect:/staff-login";
        }

        try {
            switch (action.toLowerCase()) {
                case "new":
                    if (!SecurityUtils.isManager(session)) {
                        return "redirect:/admin?error=unauthorized";
                    }
                    model.addAttribute("generatedAppointmentId", appointmentFileManager.generateNextAppointmentId());
                    model.addAttribute("services", serviceFileManager.readAllServices());
                    model.addAttribute("stylists", stylistFileManager.readAllStylists());
                    if ("double_booked".equals(error)) {
                        model.addAttribute("errorMessage", "Double Booking Prevented: That stylist is already booked at that exact time!");
                    }
                    return "book-appointment";

                case "public-book":
                    String loggedInName = (String) session.getAttribute("loggedInCustomerName");
                    if (loggedInName == null) {
                        return "redirect:/customers?action=login";
                    }
                    model.addAttribute("customerName", loggedInName);
                    model.addAttribute("generatedAppointmentId", appointmentFileManager.generateNextAppointmentId());
                    model.addAttribute("services", serviceFileManager.readAllServices());
                    model.addAttribute("stylists", stylistFileManager.readAllStylists());
                    if ("double_booked".equals(error)) {
                        model.addAttribute("errorMessage", "This time slot was just taken! Please select a different time or stylist.");
                    }
                    return "public-book-appointment";

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

                    if ("double_booked".equals(error)) {
                        model.addAttribute("errorMessage", "Cannot reschedule: That stylist is already booked at that new time!");
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

    @GetMapping("/receipt")
    public String showReceipt(@RequestParam int appointmentId, HttpSession session, Model model) {
        String loggedInCustomer = (String) session.getAttribute("loggedInCustomerName");
        if (loggedInCustomer == null) {
            return "redirect:/customers?action=login";
        }

        try {
            Appointment appt = findById(appointmentId);
            if (appt == null || !appt.getCustomerName().equalsIgnoreCase(loggedInCustomer) || !"Completed".equalsIgnoreCase(appt.getStatus())) {
                return "redirect:/my-portal";
            }

            List<SalonService> allServices = serviceFileManager.readAllServices();
            double basePrice = 0.0;
            for (SalonService s : allServices) {
                if (s.getName().equalsIgnoreCase(appt.getServiceName())) {
                    basePrice = s.getBasePrice();
                    break;
                }
            }

            List<Appointment> allAppts = appointmentFileManager.readAllAppointments();
            Appointment firstVisit = null;

            for (Appointment a : allAppts) {
                if (a.getCustomerName().equalsIgnoreCase(loggedInCustomer) && "Completed".equalsIgnoreCase(a.getStatus())) {
                    if (firstVisit == null) {
                        firstVisit = a;
                    } else {
                        int dateOrder = a.getAppointmentDate().compareTo(firstVisit.getAppointmentDate());

                        if (dateOrder < 0) {
                            firstVisit = a;
                        } else if (dateOrder == 0) {
                            if (a.getAppointmentId() < firstVisit.getAppointmentId()) {
                                firstVisit = a;
                            }
                        }
                    }
                }
            }

            boolean isFirstTime = (firstVisit != null && appt.getAppointmentId() == firstVisit.getAppointmentId());

            double discountAmount = isFirstTime ? (basePrice * 0.10) : 0.0;
            double tax = (basePrice - discountAmount) * 0.05;
            double finalTotal = (basePrice - discountAmount) + tax;

            model.addAttribute("appointment", appt);
            model.addAttribute("basePrice", basePrice);
            model.addAttribute("isFirstTime", isFirstTime);
            model.addAttribute("discountAmount", discountAmount);
            model.addAttribute("taxAmount", tax);
            model.addAttribute("finalTotal", finalTotal);

            return "receipt";
        } catch (IOException e) {
            return "redirect:/my-portal";
        }
    }

    @PostMapping("/appointments")
    public String handleAppointmentsPost(
            @RequestParam(required = false) String action,
            @RequestParam(required = false) Integer appointmentId,
            @RequestParam(required = false) String customerName,
            @RequestParam(required = false) String serviceName,
            @RequestParam(required = false) String stylistName,
            @RequestParam(required = false) String appointmentDate,
            @RequestParam(required = false) String appointmentTime,
            @RequestParam(required = false) String status,
            HttpSession session
    ) {
        if (!"public-book".equalsIgnoreCase(action) && !"public-cancel".equalsIgnoreCase(action) && session.getAttribute("staffRole") == null) {
            return "redirect:/staff-login";
        }

        if (action == null || action.isBlank()) {
            return "redirect:/appointments?action=list";
        }

        try {
            switch (action.toLowerCase()) {
                case "new":
                    if (!SecurityUtils.isManager(session)) {
                        return "redirect:/admin?error=unauthorized";
                    }
                    if (customerName != null && serviceName != null && appointmentDate != null && appointmentTime != null) {
                        String finalStylist = stylistName != null ? stylistName : "Unassigned";

                        if (!"Unassigned".equals(finalStylist)) {
                            boolean isAvailable = appointmentFileManager.isStylistAvailable(finalStylist, appointmentDate, appointmentTime);
                            if (!isAvailable) {
                                return "redirect:/appointments?action=new&error=double_booked";
                            }
                        }

                        int newId = appointmentFileManager.generateNextAppointmentId();
                        Appointment appointment = new Appointment(
                                newId, customerName, serviceName, finalStylist, appointmentDate, appointmentTime
                        );
                        appointmentFileManager.saveAppointment(appointment);
                    }
                    break;

                case "public-book":
                    if (customerName != null && serviceName != null && appointmentDate != null && appointmentTime != null) {
                        String finalStylist = stylistName != null ? stylistName : "Unassigned";

                        if (!"Unassigned".equals(finalStylist)) {
                            boolean isAvailable = appointmentFileManager.isStylistAvailable(finalStylist, appointmentDate, appointmentTime);
                            if (!isAvailable) {
                                return "redirect:/appointments?action=public-book&error=double_booked";
                            }
                        }

                        int newId = appointmentFileManager.generateNextAppointmentId();
                        Appointment appointment = new Appointment(
                                newId, customerName, serviceName, finalStylist, appointmentDate, appointmentTime
                        );
                        appointmentFileManager.saveAppointment(appointment);
                        return "redirect:/my-portal";
                    }
                    break;

                case "public-cancel":
                    if (session.getAttribute("loggedInCustomerName") == null) {
                        return "redirect:/customers?action=login";
                    }
                    if (appointmentId != null) {
                        Appointment apptToCancel = findById(appointmentId);
                        if (apptToCancel != null) {

                            // NEW: 24-Hour Cancellation Policy Check
                            try {
                                LocalDate apptDate = LocalDate.parse(apptToCancel.getAppointmentDate());
                                LocalTime apptTime = LocalTime.parse(apptToCancel.getAppointmentTime());
                                LocalDateTime apptDateTime = LocalDateTime.of(apptDate, apptTime);

                                // If "Right Now + 24 Hours" is AFTER the appointment time, it's too late to cancel!
                                if (LocalDateTime.now().plus(24, ChronoUnit.HOURS).isAfter(apptDateTime)) {
                                    return "redirect:/my-portal?error=late_cancel";
                                }
                            } catch (Exception e) {
                                // If there is an issue parsing the date, safely ignore and allow standard process
                            }

                            String currentStatus = apptToCancel.getStatus();
                            if ("Pending".equalsIgnoreCase(currentStatus) || "Confirmed".equalsIgnoreCase(currentStatus)) {
                                apptToCancel.setStatus("Cancelled");
                                appointmentFileManager.updateAppointment(apptToCancel);
                            }
                        }
                    }
                    return "redirect:/my-portal";

                case "stylist-checkin":
                case "stylist-complete":
                    if (session.getAttribute("staffRole") == null) {
                        return "redirect:/staff-login";
                    }
                    if (appointmentId != null) {
                        Appointment appt = findById(appointmentId);
                        if (appt != null) {
                            if ("stylist-checkin".equalsIgnoreCase(action)) {
                                appt.setStatus("Checked In");
                            } else if ("stylist-complete".equalsIgnoreCase(action)) {
                                appt.setStatus("Completed");
                            }
                            appointmentFileManager.updateAppointment(appt);
                        }
                    }
                    return "redirect:/stylist-portal";

                case "update":
                    if (!SecurityUtils.isManager(session)) {
                        return "redirect:/admin?error=unauthorized";
                    }
                    if (appointmentId != null) {
                        Appointment existing = findById(appointmentId);
                        if (existing != null) {

                            String newDate = appointmentDate != null ? appointmentDate : existing.getAppointmentDate();
                            String newTime = appointmentTime != null ? appointmentTime : existing.getAppointmentTime();
                            String newStylist = stylistName != null ? stylistName : existing.getStylistName();

                            if (!"Unassigned".equals(newStylist)) {
                                boolean isAvailable = appointmentFileManager.isStylistAvailableForUpdate(newStylist, newDate, newTime, appointmentId);
                                if (!isAvailable) {
                                    return "redirect:/appointments?action=edit&appointmentId=" + appointmentId + "&error=double_booked";
                                }
                            }

                            if (appointmentDate != null) existing.setAppointmentDate(appointmentDate);
                            if (appointmentTime != null) existing.setAppointmentTime(appointmentTime);
                            if (status != null) existing.setStatus(status);
                            if (stylistName != null) existing.setStylistName(stylistName);

                            appointmentFileManager.updateAppointment(existing);
                        }
                    }
                    break;
            }
        } catch (IOException ignored) {
            return "redirect:/appointments?action=list";
        }

        return "redirect:/appointments?action=list";
    }

    private Appointment findById(int appointmentId) throws IOException {
        return appointmentFileManager.readAllAppointments().stream()
                .filter(appointment -> appointment.getAppointmentId() == appointmentId)
                .findFirst().orElse(null);
    }
}
