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
    public static final String PASSWORD_SETUP_PENDING_LOGIN_MESSAGE =
            "Your account exists, but a password has not been set yet. Please use your password setup link.";

    private final CustomerRepository customerRepository;
    private final AppointmentRepository appointmentRepository;
    private final ReviewRepository reviewRepository;
    private final AccountEmailService accountEmailService;
    private final PasswordService passwordService;
    private final PasswordSetupTokenService passwordSetupTokenService;

    public CustomerService(CustomerRepository customerRepository, AppointmentRepository appointmentRepository, ReviewRepository reviewRepository, AccountEmailService accountEmailService, PasswordService passwordService, PasswordSetupTokenService passwordSetupTokenService) {
        this.customerRepository = customerRepository;
        this.appointmentRepository = appointmentRepository;
        this.reviewRepository = reviewRepository;
        this.accountEmailService = accountEmailService;
        this.passwordService = passwordService;
        this.passwordSetupTokenService = passwordSetupTokenService;
    }

    @Transactional
    public Customer saveCustomer(Customer customer) {
        customer.setUserId(0);
        validateProfile(customer.getName(), customer.getEmail());
        validateNewPassword(customer.getPassword(), customer.getPassword());
        customer.setEmail(accountEmailService.normalize(customer.getEmail()));
        customer.setPassword(passwordService.hash(customer.getPassword()));
        accountEmailService.assertCustomerEmailAvailable(customer.getEmail(), customer.getUserId());
        try {
            return customerRepository.save(customer);
        } catch (DataIntegrityViolationException ex) {
            throw new DuplicateEmailException(AccountEmailService.DUPLICATE_EMAIL_MESSAGE, ex);
        }
    }

    @Transactional
    public PasswordSetupToken saveAdminCreatedCustomer(Customer customer) {
        customer.setUserId(0);
        validateProfile(customer.getName(), customer.getEmail());
        customer.setEmail(accountEmailService.normalize(customer.getEmail()));
        // Admin-created accounts start without a password; the customer must set one through
        // the emailed setup link before they can log in.
        customer.setPassword("");
        accountEmailService.assertCustomerEmailAvailable(customer.getEmail(), customer.getUserId());
        PasswordSetupToken token = assignPasswordSetupToken(customer);
        try {
            customerRepository.save(customer);
            return token;
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
                // Appointments and reviews store customer names for display/history, so a
                // profile rename must keep those records readable under the new name.
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
            // Customer-facing history is keyed by display name in these tables; propagate the
            // profile name change so the customer's portal still finds their old activity.
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
                // Customer deletion is a hard delete in this app, so remove the dependent
                // appointment/review rows that are tied to the customer's display name first.
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

    public boolean isPasswordSetupPending(String email) {
        Customer customer = findByEmail(email);
        return customer != null && customer.isPasswordSetupRequired();
    }

    public Customer previewPasswordSetup(String rawToken) {
        return requireValidPasswordSetupCustomer(rawToken);
    }

    @Transactional
    public Customer setupPassword(String rawToken, String newPassword, String confirmPassword) {
        Customer customer = requireValidPasswordSetupCustomer(rawToken);
        validateNewPassword(newPassword, confirmPassword);
        customer.setPassword(passwordService.hash(newPassword));
        customer.clearPasswordSetupToken();
        return customerRepository.save(customer);
    }

    @Transactional
    public PasswordSetupToken regeneratePasswordSetupToken(int userId) {
        Customer customer = customerRepository.findById(userId)
                .orElseThrow(() -> new IllegalArgumentException("Customer account could not be found."));
        if (customer.isPasswordSet()) {
            throw new IllegalArgumentException("This customer already has a password set.");
        }
        PasswordSetupToken token = assignPasswordSetupToken(customer);
        customerRepository.save(customer);
        return token;
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
        validateNewPassword(newPassword, confirmPassword);
    }

    private void validateNewPassword(String newPassword, String confirmPassword) {
        if (!hasText(newPassword) || newPassword.length() < MIN_PASSWORD_LENGTH) {
            throw new IllegalArgumentException("New password must be at least " + MIN_PASSWORD_LENGTH + " characters long.");
        }
        if (!newPassword.equals(confirmPassword)) {
            throw new IllegalArgumentException("New password and confirm password must match.");
        }
    }

    private PasswordSetupToken assignPasswordSetupToken(Customer customer) {
        PasswordSetupToken token = passwordSetupTokenService.generateToken();
        // Persist the hash and expiry, not the raw setup token. The raw token belongs only in
        // the email link and is invalidated when setupPassword clears it after use.
        customer.setPasswordSetupTokenHash(passwordSetupTokenService.hashToken(token.rawToken()));
        customer.setPasswordSetupTokenExpiresAt(token.expiresAt());
        return token;
    }

    private Customer requireValidPasswordSetupCustomer(String rawToken) {
        if (!hasText(rawToken)) {
            throw new IllegalArgumentException("This password setup link is invalid or has already been used.");
        }
        String tokenHash = passwordSetupTokenService.hashToken(rawToken);
        Customer customer = customerRepository.findByPasswordSetupTokenHash(tokenHash).orElse(null);
        if (customer == null || customer.isPasswordSet()) {
            throw new IllegalArgumentException("This password setup link is invalid or has already been used.");
        }
        if (passwordSetupTokenService.isExpired(customer.getPasswordSetupTokenExpiresAt())) {
            throw new IllegalArgumentException("This password setup link has expired. Please ask an administrator to regenerate it.");
        }
        return customer;
    }
}
