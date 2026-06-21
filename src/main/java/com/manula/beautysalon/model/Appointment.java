package com.manula.beautysalon.model;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Appointment {

    private int appointmentId;
    private String customerName;
    private String serviceName;
    private String stylistName;
    private String appointmentDate;
    private String appointmentTime;
    private String status;

    public Appointment(int appointmentId, String customerName, String serviceName,
                       String stylistName, String appointmentDate, String appointmentTime) {
        this.appointmentId = appointmentId;
        this.customerName = customerName;
        this.serviceName = serviceName;
        this.stylistName = stylistName;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.status = "Pending";
    }

    // --- ACCESSORS ---
    public int getAppointmentId() {
        return appointmentId;
    }

    public void setAppointmentId(int appointmentId) {
        this.appointmentId = appointmentId;
    }

    public String getCustomerName() {
        return customerName;
    }

    public void setCustomerName(String customerName) {
        this.customerName = customerName;
    }

    public String getServiceName() {
        return serviceName;
    }

    public void setServiceName(String serviceName) {
        this.serviceName = serviceName;
    }

    public String getStylistName() {
        return (stylistName == null || stylistName.isBlank()) ? "Unassigned" : stylistName;
    }

    public void setStylistName(String stylistName) {
        this.stylistName = stylistName;
    }

    public String getAppointmentDate() {
        return appointmentDate;
    }

    public void setAppointmentDate(String appointmentDate) {
        this.appointmentDate = appointmentDate;
    }

    public String getAppointmentTime() {
        return appointmentTime;
    }

    public void setAppointmentTime(String appointmentTime) {
        this.appointmentTime = appointmentTime;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    // --- LOGIC HELPERS ---

    // ADDED BACK: For specific rescheduling logic if needed
    public void reschedule(String newDate, String newTime) {
        this.appointmentDate = newDate;
        this.appointmentTime = newTime;
        this.status = "Rescheduled";
    }

    public String getFormattedAppointmentTime() {
        if (appointmentTime == null || appointmentTime.trim().isEmpty()) return "TBD";
        try {
            LocalTime time = LocalTime.parse(this.appointmentTime);
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
            return time.format(formatter);
        } catch (Exception e) {
            return this.appointmentTime;
        }
    }

    public String getDisplayStatus() {
        String baseClass = "badge rounded-pill px-3 py-2 ";

        if ("Pending".equalsIgnoreCase(status)) {
            return "<span class='" + baseClass + "bg-warning text-dark'>Pending</span>";
        } else if ("Confirmed".equalsIgnoreCase(status)) {
            return "<span class='" + baseClass + "bg-success text-white'>Confirmed</span>";
        } else if ("Cancelled".equalsIgnoreCase(status)) {
            return "<span class='" + baseClass + "bg-secondary text-white' style='text-decoration: line-through;'>Cancelled</span>";
        } else if ("Checked In".equalsIgnoreCase(status)) {
            return "<span class='" + baseClass + "text-white' style='background-color: #6a1b9a;'>Checked In</span>";
        } else if ("Completed".equalsIgnoreCase(status)) {
            return "<span class='" + baseClass + "text-dark' style='background-color: #e2f0d9; border: 1px solid #2e7d32;'>Completed</span>";
        } else if ("Rescheduled".equalsIgnoreCase(status)) {
            return "<span class='" + baseClass + "bg-info text-dark'>Rescheduled</span>";
        }
        return "<span class='" + baseClass + "bg-secondary'>" + status + "</span>";
    }
}
