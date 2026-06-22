package com.manula.beautysalon.service;

import com.manula.beautysalon.model.Appointment;
import com.manula.beautysalon.repository.AppointmentRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AppointmentService {

    private final AppointmentRepository appointmentRepository;

    public AppointmentService(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    public Appointment saveAppointment(Appointment appointment) {
        appointment.setAppointmentId(0);
        return appointmentRepository.save(appointment);
    }

    public List<Appointment> readAllAppointments() {
        return appointmentRepository.findAllByOrderByAppointmentIdAsc();
    }

    public void updateAppointment(Appointment updatedAppointment) {
        appointmentRepository.findById(updatedAppointment.getAppointmentId()).ifPresent(existing -> {
            existing.setCustomerName(updatedAppointment.getCustomerName());
            existing.setServiceName(updatedAppointment.getServiceName());
            existing.setStylistName(updatedAppointment.getStylistName());
            existing.setAppointmentDate(updatedAppointment.getAppointmentDate());
            existing.setAppointmentTime(updatedAppointment.getAppointmentTime());
            existing.setStatus(updatedAppointment.getStatus());
            appointmentRepository.save(existing);
        });
    }

    public void deleteAppointment(int appointmentId) {
        if (appointmentRepository.existsById(appointmentId)) {
            appointmentRepository.deleteById(appointmentId);
        }
    }

    public boolean isStylistAvailable(String stylistName, String date, String time) {
        for (Appointment appointment : readAllAppointments()) {
            if (!"Cancelled".equalsIgnoreCase(appointment.getStatus())
                    && appointment.getStylistName().equalsIgnoreCase(stylistName)
                    && appointment.getAppointmentDate().equals(date)
                    && appointment.getAppointmentTime().equals(time)) {
                return false;
            }
        }
        return true;
    }

    public boolean isStylistAvailableForUpdate(String stylistName, String date, String time, int excludeAppointmentId) {
        for (Appointment appointment : readAllAppointments()) {
            if (!"Cancelled".equalsIgnoreCase(appointment.getStatus())
                    && appointment.getAppointmentId() != excludeAppointmentId
                    && appointment.getStylistName().equalsIgnoreCase(stylistName)
                    && appointment.getAppointmentDate().equals(date)
                    && appointment.getAppointmentTime().equals(time)) {
                return false;
            }
        }
        return true;
    }

    public Appointment findById(int appointmentId) {
        return appointmentRepository.findById(appointmentId).orElse(null);
    }

    public List<Appointment> findByCustomerName(String customerName) {
        if (customerName == null || customerName.isBlank()) {
            return List.of();
        }
        return appointmentRepository.findByCustomerNameIgnoreCaseOrderByAppointmentIdAsc(customerName);
    }

    public List<Appointment> findByStylistName(String stylistName) {
        if (stylistName == null || stylistName.isBlank()) {
            return List.of();
        }
        return appointmentRepository.findByStylistNameIgnoreCaseOrderByAppointmentDateAscAppointmentTimeAsc(stylistName);
    }

    public int generateNextAppointmentId() {
        return appointmentRepository.findTopByOrderByAppointmentIdDesc()
                .map(appointment -> appointment.getAppointmentId() + 1)
                .orElse(1);
    }
}
