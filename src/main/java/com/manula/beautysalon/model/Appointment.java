package com.manula.beautysalon.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;

import java.time.LocalTime;
import java.time.format.DateTimeFormatter;

@Entity
@Table(name = "appointments")
public class Appointment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "appointment_id")
    private int appointmentId;

    @Column(nullable = false)
    private String customerName;

    @Column(nullable = false)
    private String serviceName;

    private String stylistName;
    private String appointmentDate;
    private String appointmentTime;
    private String status;

    public Appointment() {
        this.status = "Pending";
    }

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

    @PrePersist
    private void applyDefaults() {
        if (status == null || status.isBlank()) {
            status = "Pending";
        }
        if (stylistName == null || stylistName.isBlank()) {
            stylistName = "Unassigned";
        }
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

    public void reschedule(String newDate, String newTime) {
        this.appointmentDate = newDate;
        this.appointmentTime = newTime;
        this.status = "Rescheduled";
    }

    public String getFormattedAppointmentTime() {
        if (appointmentTime == null || appointmentTime.trim().isEmpty()) {
            return "TBD";
        }
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
