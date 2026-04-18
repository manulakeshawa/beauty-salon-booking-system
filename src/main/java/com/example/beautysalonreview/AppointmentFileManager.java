package com.example.beautysalonreview;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
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

    private void writeAllAppointments(List<Appointment> appointments) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, false))) {
            for (Appointment appointment : appointments) {
                writer.write(toLine(appointment));
                writer.newLine();
            }
        }
    }

    private Appointment parseLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        String[] parts = line.split(",", 6);
        if (parts.length != 6) {
            return null;
        }

        try {
            int appointmentId = Integer.parseInt(parts[0].trim());
            String customerName = parts[1].trim();
            String serviceName = parts[2].trim();
            String appointmentDate = parts[3].trim();
            String appointmentTime = parts[4].trim();
            String status = parts[5].trim();

            Appointment appointment = new Appointment(
                    appointmentId,
                    customerName,
                    serviceName,
                    appointmentDate,
                    appointmentTime
            );
            appointment.setStatus(status);
            return appointment;
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String toLine(Appointment appointment) {
        return appointment.getAppointmentId()
                + "," + sanitizeField(appointment.getCustomerName())
                + "," + sanitizeField(appointment.getServiceName())
                + "," + sanitizeField(appointment.getAppointmentDate())
                + "," + sanitizeField(appointment.getAppointmentTime())
                + "," + sanitizeField(appointment.getStatus());
    }

    private String sanitizeField(String value) {
        if (value == null) {
            return "";
        }
        return value.replace(",", " ").trim();
    }

    private void ensureDataFileExists() throws IOException {
        File file = new File(FILE_PATH);
        File parent = file.getParentFile();

        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        if (!file.exists()) {
            file.createNewFile();
        }
    }

    // NEW: Auto-Increment Engine
    public int generateNextAppointmentId() throws IOException {
        List<Appointment> appointments = readAllAppointments();
        int maxId = 3000; // This means your very first appointment will be 3001

        for (Appointment apt : appointments) {
            if (apt.getAppointmentId() > maxId) {
                maxId = apt.getAppointmentId();
            }
        }
        return maxId + 1;
    }
}