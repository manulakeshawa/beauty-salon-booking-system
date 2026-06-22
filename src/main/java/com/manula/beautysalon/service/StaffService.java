package com.manula.beautysalon.service;

import com.manula.beautysalon.model.Employee;
import com.manula.beautysalon.model.Stylist;
import com.manula.beautysalon.repository.EmployeeRepository;
import org.springframework.stereotype.Service;

@Service
public class StaffService {

    private final EmployeeRepository employeeRepository;
    private final StylistService stylistService;

    public StaffService(EmployeeRepository employeeRepository, StylistService stylistService) {
        this.employeeRepository = employeeRepository;
        this.stylistService = stylistService;
    }

    public Employee authenticateEmployee(String username, String password) {
        if (username == null || password == null) {
            return null;
        }

        String trimmedUsername = username.trim();
        Employee employee = employeeRepository
                .findByUsernameIgnoreCaseAndPassword(trimmedUsername, password)
                .orElse(null);

        if (employee != null) {
            return employee;
        }

        if ("admin".equalsIgnoreCase(trimmedUsername) && "lumiere2026".equals(password)) {
            return defaultManager();
        }

        return null;
    }

    public Stylist authenticateStylist(String username, String password) {
        return stylistService.authenticate(username, password);
    }

    private Employee defaultManager() {
        return new Employee(
                0,
                "admin",
                "lumiere2026",
                "Salon Owner",
                "admin@lumieresalon.lk",
                "MANAGER",
                "Owner",
                "Management",
                "Welcome to Lumiere.",
                "Available"
        );
    }
}
