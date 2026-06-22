package com.manula.beautysalon.controller;

import com.manula.beautysalon.model.Appointment;
import com.manula.beautysalon.model.SalonService;
import com.manula.beautysalon.service.AppointmentService;
import com.manula.beautysalon.service.SalonServiceService;
import com.manula.beautysalon.service.StylistService;
import com.manula.beautysalon.util.SecurityUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Controller
public class AppointmentWebController {

    private final AppointmentService appointmentService;
    private final SalonServiceService salonServiceService;
    private final StylistService stylistService;

    public AppointmentWebController(AppointmentService appointmentService, SalonServiceService salonServiceService, StylistService stylistService) {
        this.appointmentService = appointmentService;
        this.salonServiceService = salonServiceService;
        this.stylistService = stylistService;
    }

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

        switch (action.toLowerCase()) {
            case "new":
                if (!SecurityUtils.isManager(session)) {
                    return "redirect:/admin?error=unauthorized";
                }
                model.addAttribute("generatedAppointmentId", appointmentService.generateNextAppointmentId());
                model.addAttribute("services", salonServiceService.readAllServices());
                model.addAttribute("stylists", stylistService.readAllStylists());
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
                model.addAttribute("generatedAppointmentId", appointmentService.generateNextAppointmentId());
                model.addAttribute("services", salonServiceService.readAllServices());
                model.addAttribute("stylists", stylistService.readAllStylists());
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
                Appointment appointmentToEdit = appointmentService.findById(appointmentId);
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
                    appointmentService.deleteAppointment(appointmentId);
                }
                return "redirect:/appointments?action=list";

            case "list":
            default:
                List<Appointment> appointments = appointmentService.readAllAppointments();
                model.addAttribute("appointments", appointments);
                return "appointment-list";
        }
    }

    @GetMapping("/receipt")
    public String showReceipt(@RequestParam int appointmentId, HttpSession session, Model model) {
        String loggedInCustomer = (String) session.getAttribute("loggedInCustomerName");
        if (loggedInCustomer == null) {
            return "redirect:/customers?action=login";
        }

        Appointment appt = appointmentService.findById(appointmentId);
        if (appt == null || !appt.getCustomerName().equalsIgnoreCase(loggedInCustomer) || !"Completed".equalsIgnoreCase(appt.getStatus())) {
            return "redirect:/my-portal";
        }

        List<SalonService> allServices = salonServiceService.readAllServices();
        double basePrice = 0.0;
        for (SalonService service : allServices) {
            if (service.getName().equalsIgnoreCase(appt.getServiceName())) {
                basePrice = service.getBasePrice();
                break;
            }
        }

        List<Appointment> allAppts = appointmentService.readAllAppointments();
        Appointment firstVisit = null;

        for (Appointment appointment : allAppts) {
            if (appointment.getCustomerName().equalsIgnoreCase(loggedInCustomer) && "Completed".equalsIgnoreCase(appointment.getStatus())) {
                if (firstVisit == null) {
                    firstVisit = appointment;
                } else {
                    int dateOrder = appointment.getAppointmentDate().compareTo(firstVisit.getAppointmentDate());

                    if (dateOrder < 0) {
                        firstVisit = appointment;
                    } else if (dateOrder == 0 && appointment.getAppointmentId() < firstVisit.getAppointmentId()) {
                        firstVisit = appointment;
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

        switch (action.toLowerCase()) {
            case "new":
                if (!SecurityUtils.isManager(session)) {
                    return "redirect:/admin?error=unauthorized";
                }
                if (customerName != null && serviceName != null && appointmentDate != null && appointmentTime != null) {
                    String finalStylist = stylistName != null ? stylistName : "Unassigned";

                    if (!"Unassigned".equals(finalStylist) && !appointmentService.isStylistAvailable(finalStylist, appointmentDate, appointmentTime)) {
                        return "redirect:/appointments?action=new&error=double_booked";
                    }

                    Appointment appointment = new Appointment(
                            0, customerName, serviceName, finalStylist, appointmentDate, appointmentTime
                    );
                    appointmentService.saveAppointment(appointment);
                }
                break;

            case "public-book":
                if (customerName != null && serviceName != null && appointmentDate != null && appointmentTime != null) {
                    String finalStylist = stylistName != null ? stylistName : "Unassigned";

                    if (!"Unassigned".equals(finalStylist) && !appointmentService.isStylistAvailable(finalStylist, appointmentDate, appointmentTime)) {
                        return "redirect:/appointments?action=public-book&error=double_booked";
                    }

                    Appointment appointment = new Appointment(
                            0, customerName, serviceName, finalStylist, appointmentDate, appointmentTime
                    );
                    appointmentService.saveAppointment(appointment);
                    return "redirect:/my-portal";
                }
                break;

            case "public-cancel":
                if (session.getAttribute("loggedInCustomerName") == null) {
                    return "redirect:/customers?action=login";
                }
                if (appointmentId != null) {
                    Appointment apptToCancel = appointmentService.findById(appointmentId);
                    if (apptToCancel != null) {
                        try {
                            LocalDate apptDate = LocalDate.parse(apptToCancel.getAppointmentDate());
                            LocalTime apptTime = LocalTime.parse(apptToCancel.getAppointmentTime());
                            LocalDateTime apptDateTime = LocalDateTime.of(apptDate, apptTime);

                            if (LocalDateTime.now().plus(24, ChronoUnit.HOURS).isAfter(apptDateTime)) {
                                return "redirect:/my-portal?error=late_cancel";
                            }
                        } catch (Exception ignored) {
                        }

                        String currentStatus = apptToCancel.getStatus();
                        if ("Pending".equalsIgnoreCase(currentStatus) || "Confirmed".equalsIgnoreCase(currentStatus)) {
                            apptToCancel.setStatus("Cancelled");
                            appointmentService.updateAppointment(apptToCancel);
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
                    Appointment appointment = appointmentService.findById(appointmentId);
                    if (appointment != null) {
                        if ("stylist-checkin".equalsIgnoreCase(action)) {
                            appointment.setStatus("Checked In");
                        } else if ("stylist-complete".equalsIgnoreCase(action)) {
                            appointment.setStatus("Completed");
                        }
                        appointmentService.updateAppointment(appointment);
                    }
                }
                return "redirect:/stylist-portal";

            case "update":
                if (!SecurityUtils.isManager(session)) {
                    return "redirect:/admin?error=unauthorized";
                }
                if (appointmentId != null) {
                    Appointment existing = appointmentService.findById(appointmentId);
                    if (existing != null) {

                        String newDate = appointmentDate != null ? appointmentDate : existing.getAppointmentDate();
                        String newTime = appointmentTime != null ? appointmentTime : existing.getAppointmentTime();
                        String newStylist = stylistName != null ? stylistName : existing.getStylistName();

                        if (!"Unassigned".equals(newStylist)
                                && !appointmentService.isStylistAvailableForUpdate(newStylist, newDate, newTime, appointmentId)) {
                            return "redirect:/appointments?action=edit&appointmentId=" + appointmentId + "&error=double_booked";
                        }

                        if (appointmentDate != null) {
                            existing.setAppointmentDate(appointmentDate);
                        }
                        if (appointmentTime != null) {
                            existing.setAppointmentTime(appointmentTime);
                        }
                        if (status != null) {
                            existing.setStatus(status);
                        }
                        if (stylistName != null) {
                            existing.setStylistName(stylistName);
                        }

                        appointmentService.updateAppointment(existing);
                    }
                }
                break;
            default:
                break;
        }

        return "redirect:/appointments?action=list";
    }
}
