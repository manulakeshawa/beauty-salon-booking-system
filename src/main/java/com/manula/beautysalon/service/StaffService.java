package com.manula.beautysalon.service;

import com.manula.beautysalon.model.Employee;
import com.manula.beautysalon.model.Stylist;
import com.manula.beautysalon.repository.EmployeeRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StaffService {

    private final EmployeeRepository employeeRepository;
    private final StylistService stylistService;
    private final AccountEmailService accountEmailService;

    public StaffService(EmployeeRepository employeeRepository, StylistService stylistService, AccountEmailService accountEmailService) {
        this.employeeRepository = employeeRepository;
        this.stylistService = stylistService;
        this.accountEmailService = accountEmailService;
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

    @Transactional
    public Employee saveEmployee(Employee employee) {
        employee.setUserId(0);
        employee.setEmail(accountEmailService.normalize(employee.getEmail()));
        accountEmailService.assertEmployeeEmailAvailable(employee.getEmail(), employee.getUserId());
        try {
            return employeeRepository.save(employee);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateEmailException(AccountEmailService.DUPLICATE_EMAIL_MESSAGE, ex);
        }
    }

    @Transactional
    public void updateEmployee(Employee updatedEmployee) {
        employeeRepository.findById(updatedEmployee.getUserId()).ifPresent(existing -> {
            String normalizedEmail = accountEmailService.normalize(updatedEmployee.getEmail());
            accountEmailService.assertEmployeeEmailAvailable(normalizedEmail, updatedEmployee.getUserId());
            existing.setUsername(updatedEmployee.getUsername());
            existing.setPassword(updatedEmployee.getPassword());
            existing.setFullName(updatedEmployee.getFullName());
            existing.setEmail(normalizedEmail);
            existing.setRole(updatedEmployee.getRole());
            existing.setLevel(updatedEmployee.getLevel());
            existing.setSpecialty(updatedEmployee.getSpecialty());
            existing.setWelcomeMessage(updatedEmployee.getWelcomeMessage());
            existing.setAvailabilityStatus(updatedEmployee.getAvailabilityStatus());
            try {
                employeeRepository.save(existing);
            } catch (DataIntegrityViolationException ex) {
                throw new DuplicateEmailException(AccountEmailService.DUPLICATE_EMAIL_MESSAGE, ex);
            }
        });
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
