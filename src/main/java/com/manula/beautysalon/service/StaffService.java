package com.manula.beautysalon.service;

import com.manula.beautysalon.model.Employee;
import com.manula.beautysalon.model.Stylist;
import com.manula.beautysalon.repository.EmployeeRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;
import java.util.regex.Pattern;

@Service
public class StaffService {

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

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

    public Employee authenticateEmployee(String identifier, String password) {
        if (identifier == null || password == null) {
            return null;
        }

        String trimmedIdentifier = identifier.trim();
        Employee employee = findEmployeeByIdentifier(trimmedIdentifier).orElse(null);

        if (employee != null) {
            return passwordService.matches(password, employee.getPassword()) ? employee : null;
        }

        if (matchesDefaultManagerIdentifier(trimmedIdentifier) && passwordService.matches(password, defaultManagerPasswordHash())) {
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
        if (employee == null && matchesDefaultManagerIdentifier(username.trim())) {
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

    public Employee findAdminAccount(String username) {
        if (!hasText(username)) {
            return null;
        }

        Employee employee = employeeRepository.findByUsernameIgnoreCase(username.trim()).orElse(null);
        if (employee != null && "MANAGER".equalsIgnoreCase(employee.getRole())) {
            return employee;
        }

        if (matchesDefaultManagerIdentifier(username.trim())) {
            return defaultManager();
        }

        return null;
    }

    @Transactional
    public Employee updateAdminEmail(String username, String email) {
        if (!hasText(username)) {
            throw new IllegalArgumentException("Your admin session has expired. Please sign in again.");
        }
        validateEmail(email);

        Employee employee = employeeRepository.findByUsernameIgnoreCase(username.trim()).orElse(null);
        if (employee == null && matchesDefaultManagerIdentifier(username.trim())) {
            Employee manager = defaultManager();
            manager.setEmail(accountEmailService.normalize(email));
            return saveEmployee(manager);
        }

        if (employee == null || !"MANAGER".equalsIgnoreCase(employee.getRole())) {
            throw new IllegalArgumentException("Your admin account could not be found. Please sign in again.");
        }

        String normalizedEmail = accountEmailService.normalize(email);
        accountEmailService.assertEmployeeEmailAvailable(normalizedEmail, employee.getUserId());
        employee.setEmail(normalizedEmail);
        try {
            return employeeRepository.save(employee);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateEmailException(AccountEmailService.DUPLICATE_EMAIL_MESSAGE, ex);
        }
    }

    private Optional<Employee> findEmployeeByIdentifier(String identifier) {
        if (!hasText(identifier)) {
            return Optional.empty();
        }

        Optional<Employee> employee = employeeRepository.findByUsernameIgnoreCase(identifier.trim());
        if (employee.isPresent()) {
            return employee;
        }

        String normalizedIdentifier = accountEmailService.normalize(identifier);
        if (!hasText(normalizedIdentifier)) {
            return Optional.empty();
        }

        return employeeRepository.findByEmailIgnoreCase(normalizedIdentifier);
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

    private boolean matchesDefaultManagerIdentifier(String identifier) {
        if (!hasText(identifier)) {
            return false;
        }

        String trimmedIdentifier = identifier.trim();
        return "admin".equalsIgnoreCase(trimmedIdentifier)
                || "admin@lumieresalon.lk".equalsIgnoreCase(accountEmailService.normalize(trimmedIdentifier));
    }

    private String defaultManagerPasswordHash() {
        return "pbkdf2_sha256$210000$nZjYarRaYRQNlerCO4cmkA==$hXTAKZy0H4jjZC4l9Y8uZUwsj3OuyxGV3f8mM7WHZSM=";
    }

    private void validateEmail(String email) {
        if (!hasText(email) || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("Please enter a valid email address.");
        }
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
