package com.manula.beautysalon.service;

import com.manula.beautysalon.model.Customer;
import com.manula.beautysalon.repository.AppointmentRepository;
import com.manula.beautysalon.repository.CustomerRepository;
import com.manula.beautysalon.repository.ReviewRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.regex.Pattern;

@Service
public class CustomerService {

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");

    private final CustomerRepository customerRepository;
    private final AppointmentRepository appointmentRepository;
    private final ReviewRepository reviewRepository;
    private final AccountEmailService accountEmailService;
    private final PasswordService passwordService;

    public CustomerService(CustomerRepository customerRepository, AppointmentRepository appointmentRepository, ReviewRepository reviewRepository, AccountEmailService accountEmailService, PasswordService passwordService) {
        this.customerRepository = customerRepository;
        this.appointmentRepository = appointmentRepository;
        this.reviewRepository = reviewRepository;
        this.accountEmailService = accountEmailService;
        this.passwordService = passwordService;
    }

    @Transactional
    public Customer saveCustomer(Customer customer) {
        customer.setUserId(0);
        customer.setEmail(accountEmailService.normalize(customer.getEmail()));
        customer.setPassword(passwordService.hashIfPlainText(customer.getPassword()));
        accountEmailService.assertCustomerEmailAvailable(customer.getEmail(), customer.getUserId());
        try {
            return customerRepository.save(customer);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateEmailException(AccountEmailService.DUPLICATE_EMAIL_MESSAGE, ex);
        }
    }

    public List<Customer> readAllCustomers() {
        return customerRepository.findAllByOrderByUserIdAsc();
    }

    @Transactional
    public void updateCustomer(Customer updatedCustomer) {
        customerRepository.findById(updatedCustomer.getUserId()).ifPresent(existing -> {
            String previousName = existing.getName();
            String normalizedEmail = accountEmailService.normalize(updatedCustomer.getEmail());
            accountEmailService.assertCustomerEmailAvailable(normalizedEmail, updatedCustomer.getUserId());
            existing.setName(updatedCustomer.getName());
            existing.setEmail(normalizedEmail);
            existing.setPassword(passwordService.hashIfPlainText(updatedCustomer.getPassword()));
            existing.setCustomerType(updatedCustomer.getCustomerType());
            try {
                customerRepository.save(existing);
            } catch (DataIntegrityViolationException ex) {
                throw new DuplicateEmailException(AccountEmailService.DUPLICATE_EMAIL_MESSAGE, ex);
            }

            if (hasText(previousName) && hasText(updatedCustomer.getName())
                    && !previousName.equalsIgnoreCase(updatedCustomer.getName())) {
                appointmentRepository.updateCustomerNameIgnoreCase(previousName, updatedCustomer.getName());
                reviewRepository.updateCustomerNameIgnoreCase(previousName, updatedCustomer.getName());
            }
        });
    }

    @Transactional
    public Customer updateCustomerProfile(String currentEmail, String name, String email) {
        Customer existing = findByEmail(currentEmail);
        if (existing == null) {
            throw new IllegalArgumentException("Your customer account could not be found. Please sign in again.");
        }
        validateProfile(name, email);

        String previousName = existing.getName();
        String normalizedEmail = accountEmailService.normalize(email);
        accountEmailService.assertCustomerEmailAvailable(normalizedEmail, existing.getUserId());

        existing.setName(name.trim());
        existing.setEmail(normalizedEmail);
        try {
            customerRepository.save(existing);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateEmailException(AccountEmailService.DUPLICATE_EMAIL_MESSAGE, ex);
        }

        if (hasText(previousName) && !previousName.equalsIgnoreCase(existing.getName())) {
            appointmentRepository.updateCustomerNameIgnoreCase(previousName, existing.getName());
            reviewRepository.updateCustomerNameIgnoreCase(previousName, existing.getName());
        }

        return existing;
    }

    @Transactional
    public void changeCustomerPassword(String email, String currentPassword, String newPassword, String confirmPassword) {
        Customer customer = findByEmail(email);
        if (customer == null) {
            throw new IllegalArgumentException("Your customer account could not be found. Please sign in again.");
        }
        validatePasswordChange(currentPassword, newPassword, confirmPassword, customer.getPassword());
        customer.setPassword(passwordService.hash(newPassword));
        customerRepository.save(customer);
    }

    @Transactional
    public void deleteCustomer(int userId) {
        customerRepository.findById(userId).ifPresent(customer -> {
            if (hasText(customer.getName())) {
                reviewRepository.deleteByCustomerNameIgnoreCase(customer.getName());
                appointmentRepository.deleteByCustomerNameIgnoreCase(customer.getName());
            }
            customerRepository.delete(customer);
        });
    }

    public Customer findById(int userId) {
        return customerRepository.findById(userId).orElse(null);
    }

    public Customer findByEmail(String email) {
        if (email == null) {
            return null;
        }
        return customerRepository.findByEmailIgnoreCase(accountEmailService.normalize(email)).orElse(null);
    }

    public Customer authenticateCustomer(String email, String password) {
        if (email == null || password == null) {
            return null;
        }
        Customer customer = findByEmail(email);
        if (customer == null || !passwordService.matches(password, customer.getPassword())) {
            return null;
        }
        return customer;
    }

    public int generateNextCustomerId() {
        return customerRepository.findTopByOrderByUserIdDesc()
                .map(customer -> customer.getUserId() + 1)
                .orElse(1);
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }

    private void validateProfile(String name, String email) {
        if (!hasText(name)) {
            throw new IllegalArgumentException("Please enter your name.");
        }
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
}
