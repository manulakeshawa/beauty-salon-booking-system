package com.manula.beautysalon.repository.file;

import com.manula.beautysalon.model.Appointment;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class AppointmentFileManager {

    private static final String FILE_PATH = "data/appointments.txt";

    public void saveAppointment(Appointment appointment) throws IOException {
        ensureDataFileExists();
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(toLine(appointment));
            writer.newLine();
        }
    }

    public List<Appointment> readAllAppointments() throws IOException {
        ensureDataFileExists();
        List<Appointment> appointments = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Appointment appointment = parseLine(line);
                if (appointment != null) {
                    appointments.add(appointment);
                }
            }
        }
        return appointments;
    }

    public void updateAppointment(Appointment updatedAppointment) throws IOException {
        ensureDataFileExists();
        List<Appointment> appointments = readAllAppointments();
        for (int i = 0; i < appointments.size(); i++) {
            if (appointments.get(i).getAppointmentId() == updatedAppointment.getAppointmentId()) {
                appointments.set(i, updatedAppointment);
                break;
            }
        }
        writeAllAppointments(appointments);
    }

    public void deleteAppointment(int appointmentId) throws IOException {
        ensureDataFileExists();
        List<Appointment> appointments = readAllAppointments();
        appointments.removeIf(appointment -> appointment.getAppointmentId() == appointmentId);
        writeAllAppointments(appointments);
    }

    // =====================================================================================
    // Double-Booking Radar Method
    // Checks if a stylist is already booked at the requested date and time.
    // =====================================================================================
    public boolean isStylistAvailable(String stylistName, String date, String time) throws IOException {
        List<Appointment> appointments = readAllAppointments();
        for (Appointment apt : appointments) {
            if (!"Cancelled".equalsIgnoreCase(apt.getStatus())) {
                if (apt.getStylistName().equalsIgnoreCase(stylistName) &&
                        apt.getAppointmentDate().equals(date) &&
                        apt.getAppointmentTime().equals(time)) {
                    return false;
                }
            }
        }
        return true;
    }

    // =====================================================================================
    // NEW: Smart Double-Booking Radar for Updates
    // Checks if a stylist is booked, but IGNORES the appointment we are currently editing!
    // =====================================================================================
    public boolean isStylistAvailableForUpdate(String stylistName, String date, String time, int excludeAppointmentId) throws IOException {
        List<Appointment> appointments = readAllAppointments();
        for (Appointment apt : appointments) {
            // Ignore cancelled appointments AND ignore the appointment we are currently editing
            if (!"Cancelled".equalsIgnoreCase(apt.getStatus()) && apt.getAppointmentId() != excludeAppointmentId) {
                if (apt.getStylistName().equalsIgnoreCase(stylistName) &&
                        apt.getAppointmentDate().equals(date) &&
                        apt.getAppointmentTime().equals(time)) {
                    return false; // The stylist is booked by SOMEONE ELSE!
                }
            }
        }
        return true; // The slot is free!
    }
    // =====================================================================================

    private void writeAllAppointments(List<Appointment> appointments) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, false))) {
            for (Appointment appointment : appointments) {
                writer.write(toLine(appointment));
                writer.newLine();
            }
        }
    }

    private Appointment parseLine(String line) {
        if (line == null || line.trim().isEmpty()) return null;

        String[] parts = line.split(",", 7);
        if (parts.length < 6) return null;

        try {
            int id = Integer.parseInt(parts[0].trim());
            String customer = parts[1].trim();
            String service = parts[2].trim();
            String stylist = parts[3].trim();
            String date = parts[4].trim();
            String time = parts[5].trim();

            String status = (parts.length == 7) ? parts[6].trim() : "Pending";

            Appointment appt = new Appointment(id, customer, service, stylist, date, time);
            appt.setStatus(status);
            return appt;
        } catch (Exception ignored) {
            return null;
        }
    }

    private String toLine(Appointment appointment) {
        return appointment.getAppointmentId()
                + "," + sanitizeField(appointment.getCustomerName())
                + "," + sanitizeField(appointment.getServiceName())
                + "," + sanitizeField(appointment.getStylistName())
                + "," + sanitizeField(appointment.getAppointmentDate())
                + "," + sanitizeField(appointment.getAppointmentTime())
                + "," + sanitizeField(appointment.getStatus());
    }

    private String sanitizeField(String value) {
        return (value == null) ? "" : value.replace(",", " ").trim();
    }

    private void ensureDataFileExists() throws IOException {
        File file = new File(FILE_PATH);
        File parent = file.getParentFile();
        if (parent != null && !parent.exists()) parent.mkdirs();
        if (!file.exists()) file.createNewFile();
    }

    public int generateNextAppointmentId() throws IOException {
        List<Appointment> appointments = readAllAppointments();
        int maxId = 3000;
        for (Appointment apt : appointments) {
            if (apt.getAppointmentId() > maxId) maxId = apt.getAppointmentId();
        }
        return maxId + 1;
    }
}
