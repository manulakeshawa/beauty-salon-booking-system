package com.example.beautysalonreview;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

public class Appointment {

    private int appointmentId;
    private String customerName;
    private String serviceName;
    private String appointmentDate;
    private String appointmentTime;
    private String status;

    public Appointment(int appointmentId, String customerName, String serviceName,
                       String appointmentDate, String appointmentTime) {
        this.appointmentId = appointmentId;
        this.customerName = customerName;
        this.serviceName = serviceName;
        this.appointmentDate = appointmentDate;
        this.appointmentTime = appointmentTime;
        this.status = "Pending";
    }

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

    // ==========================================================
    // NEW: The UI Display Translator for AM/PM
    // ==========================================================
    public String getFormattedAppointmentTime() {
        if (appointmentTime == null || appointmentTime.trim().isEmpty()) {
            return "";
        }
        try {
            // Parses the 24-hour time (e.g., "14:00")
            LocalTime time = LocalTime.parse(this.appointmentTime);
            // Formats it to 12-hour AM/PM (e.g., "02:00 PM")
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("hh:mm a");
            return time.format(formatter);
        } catch (Exception e) {
            // If something goes wrong, just fall back to the original database string safely
            return this.appointmentTime;
        }
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public void reschedule(String newDate, String newTime) {
        this.appointmentDate = newDate;
        this.appointmentTime = newTime;
        this.status = "Rescheduled";
    }

    public String getDisplayStatus() {
        if ("Pending".equalsIgnoreCase(status)) {
            return "<span class='badge bg-warning text-dark'>Pending</span>";
        } else if ("Confirmed".equalsIgnoreCase(status)) {
            return "<span class='badge bg-success'>Confirmed</span>";
        } else if ("Cancelled".equalsIgnoreCase(status)) {
            return "<span class='badge bg-danger'>Cancelled</span>";
        } else if ("Rescheduled".equalsIgnoreCase(status)) {
            return "<span class='badge bg-info text-dark'>Rescheduled</span>";
        }
        return "<span class='badge bg-secondary'>" + status + "</span>";
    }
}