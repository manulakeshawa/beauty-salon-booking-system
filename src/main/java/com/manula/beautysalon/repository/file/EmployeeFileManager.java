package com.manula.beautysalon.repository.file;

import com.manula.beautysalon.model.Employee;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class EmployeeFileManager {
    private final List<Employee> employees = new ArrayList<>();
    private static final String STYLISTS_FILE_PATH = "stylists.txt";

    public EmployeeFileManager() {
        employees.add(new Employee(1001, "admin", "lumiere2026", "Salon Owner", "admin@lumieresalon.lk", "MANAGER", "Owner", "Management", "Welcome to Lumière.", "Available"));
        loadStylistsFromFile();
    }

    public List<Employee> getAllEmployees() {
        return employees;
    }

    public Employee authenticate(String username, String password) {
        if (username == null || password == null) return null;
        for (Employee e : employees) {
            if (e.getUsername().equalsIgnoreCase(username.trim()) && e.getPassword().equals(password)) return e;
        }
        return null;
    }

    private void loadStylistsFromFile() {
        try {
            ensureDataFileExists();
            try (BufferedReader reader = new BufferedReader(new FileReader(STYLISTS_FILE_PATH))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    Employee stylist = parseStylistLine(line);
                    if (stylist != null) employees.add(stylist);
                }
            }
        } catch (IOException ignored) {
        }
    }

    private Employee parseStylistLine(String line) {
        if (line == null || line.trim().isEmpty()) return null;
        String[] parts = line.split(",");

        // As long as we have the core 7 elements, we can build the object safely
        if (parts.length < 7) return null;

        try {
            int id = Integer.parseInt(parts[0].trim());
            String name = parts[1].trim();
            String email = parts[2].trim();
            String pass = parts[3].trim();
            String specialty = parts[4].trim();
            String level = parts[5].trim();
            String avail = parts[6].trim();

            String status = "true".equalsIgnoreCase(avail) ? "Available" : "Unavailable";
            String user = name.toLowerCase().replace(" ", "");

            // Safety check: Prevents weird sentences if the specialty gets left blank in the text file
            String safeSpecialty = specialty.isEmpty() ? "salon" : specialty.toLowerCase();
            String welcome = level + " stylist directing top-tier " + safeSpecialty + " experiences.";

            return new Employee(id, user, pass, name, email, "STYLIST", level, specialty, welcome, status);
        } catch (Exception e) {
            return null;
        }
    }

    private void ensureDataFileExists() throws IOException {
        File file = new File(STYLISTS_FILE_PATH);
        if (!file.exists()) file.createNewFile();
    }
}
