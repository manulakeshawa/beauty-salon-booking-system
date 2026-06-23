package com.manula.beautysalon.service;

import com.manula.beautysalon.model.Employee;
import com.manula.beautysalon.model.Stylist;
import com.manula.beautysalon.repository.EmployeeRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StaffService {

    private static final int MIN_PASSWORD_LENGTH = 8;

    private final EmployeeRepository employeeRepository;
    private final StylistService stylistService;
    private final AccountEmailService accountEmailService;
    private final PasswordService passwordService;

    public StaffService(EmployeeRepository employeeRepository, StylistService stylistService, AccountEmailService accountEmailService, PasswordService passwordService) {
        this.employeeRepository = employeeRepository;
        this.stylistService = stylistService;
        this.accountEmailService = accountEmailService;
        this.passwordService = passwordService;
    }

    public Employee authenticateEmployee(String username, String password) {
        if (username == null || password == null) {
            return null;
        }

        String trimmedUsername = username.trim();
        Employee employee = employeeRepository
                .findByUsernameIgnoreCase(trimmedUsername)
                .orElse(null);

        if (employee != null) {
            return passwordService.matches(password, employee.getPassword()) ? employee : null;
        }

        if ("admin".equalsIgnoreCase(trimmedUsername) && passwordService.matches(password, defaultManagerPasswordHash())) {
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
        employee.setPassword(passwordService.hashIfPlainText(employee.getPassword()));
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
            existing.setPassword(passwordService.hashIfPlainText(updatedEmployee.getPassword()));
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

    @Transactional
    public void changeAdminPassword(String username, String currentPassword, String newPassword, String confirmPassword) {
        if (!hasText(username)) {
            throw new IllegalArgumentException("Your admin session has expired. Please sign in again.");
        }

        Employee employee = employeeRepository.findByUsernameIgnoreCase(username.trim()).orElse(null);
        if (employee == null && "admin".equalsIgnoreCase(username.trim())) {
            validatePasswordChange(currentPassword, newPassword, confirmPassword, defaultManagerPasswordHash());
            Employee manager = defaultManager();
            manager.setPassword(newPassword);
            saveEmployee(manager);
            return;
        }

        if (employee == null || !"MANAGER".equalsIgnoreCase(employee.getRole())) {
            throw new IllegalArgumentException("Your admin account could not be found. Please sign in again.");
        }

        validatePasswordChange(currentPassword, newPassword, confirmPassword, employee.getPassword());
        employee.setPassword(passwordService.hash(newPassword));
        employeeRepository.save(employee);
    }

    private Employee defaultManager() {
        return new Employee(
                0,
                "admin",
                defaultManagerPasswordHash(),
                "Salon Owner",
                "admin@lumieresalon.lk",
                "MANAGER",
                "Owner",
                "Management",
                "Welcome to Lumiere.",
                "Available"
        );
    }

    private String defaultManagerPasswordHash() {
        return "pbkdf2_sha256$210000$nZjYarRaYRQNlerCO4cmkA==$hXTAKZy0H4jjZC4l9Y8uZUwsj3OuyxGV3f8mM7WHZSM=";
    }

    private void validatePasswordChange(String currentPassword, String newPassword, String confirmPassword, String storedPasswordHash) {
        if (!hasText(currentPassword)) {
            throw new IllegalArgumentException("Please enter your current password.");
        }
        if (!passwordService.matches(currentPassword, storedPasswordHash)) {
            throw new IllegalArgumentException("The current password you entered is incorrect.");
        }
        if (!hasText(newPassword) || newPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("New password must be at least " + MIN_PASSWORD_LENGTH + " characters long.");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("New password and confirm password must match.");
        }
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
