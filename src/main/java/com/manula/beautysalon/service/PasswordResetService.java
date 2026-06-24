package com.manula.beautysalon.service;

import com.manula.beautysalon.model.Customer;
import com.manula.beautysalon.model.Employee;
import com.manula.beautysalon.model.Stylist;
import com.manula.beautysalon.model.User;
import com.manula.beautysalon.repository.CustomerRepository;
import com.manula.beautysalon.repository.EmployeeRepository;
import com.manula.beautysalon.repository.StylistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.regex.Pattern;

@Service
public class PasswordResetService {

    public static final String GENERIC_RESET_REQUEST_MESSAGE =
            "If an account exists with this email, a password reset link has been sent.";

    private static final int MIN_PASSWORD_LENGTH = 8;
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final String INVALID_TOKEN_MESSAGE =
            "This password reset link is invalid or has already been used.";
    private static final String EXPIRED_TOKEN_MESSAGE =
            "This password reset link has expired. Please request a new password reset link.";

    private final CustomerRepository customerRepository;
    private final StylistRepository stylistRepository;
    private final EmployeeRepository employeeRepository;
    private final AccountEmailService accountEmailService;
    private final PasswordService passwordService;
    private final PasswordResetTokenService passwordResetTokenService;
    private final EmailService emailService;

    public PasswordResetService(
            CustomerRepository customerRepository,
            StylistRepository stylistRepository,
            EmployeeRepository employeeRepository,
            AccountEmailService accountEmailService,
            PasswordService passwordService,
            PasswordResetTokenService passwordResetTokenService,
            EmailService emailService
    ) {
        this.customerRepository = customerRepository;
        this.stylistRepository = stylistRepository;
        this.employeeRepository = employeeRepository;
        this.accountEmailService = accountEmailService;
        this.passwordService = passwordService;
        this.passwordResetTokenService = passwordResetTokenService;
        this.emailService = emailService;
    }

    @Transactional
    public void requestPasswordReset(String email) {
        validateEmail(email);
        String normalizedEmail = accountEmailService.normalize(email);
        ResettableAccount account = findAccountByEmail(normalizedEmail);
        if (account == null) {
            return;
        }

        PasswordResetToken resetToken = passwordResetTokenService.generateToken();
        account.user().setPasswordResetTokenHash(passwordResetTokenService.hashToken(resetToken.rawToken()));
        account.user().setPasswordResetTokenExpiresAt(resetToken.expiresAt());
        save(account);

        emailService.sendPasswordResetEmail(
                account.user().getEmail(),
                account.user().getName(),
                account.type().emailLabel(),
                resetToken
        );
    }

    public void previewReset(String rawToken) {
        requireValidResetAccount(rawToken);
    }

    @Transactional
    public PasswordResetCompletion resetPassword(String rawToken, String newPassword, String confirmPassword) {
        ResettableAccount account = requireValidResetAccount(rawToken);
        validateNewPassword(newPassword, confirmPassword);

        User user = account.user();
        user.setPassword(passwordService.hash(newPassword));
        user.clearPasswordResetToken();
        clearFirstTimeSetupTokenIfPresent(user);
        save(account);

        return new PasswordResetCompletion(account.type().loginPath());
    }

    private ResettableAccount findAccountByEmail(String normalizedEmail) {
        Customer customer = customerRepository.findByEmailIgnoreCase(normalizedEmail).orElse(null);
        if (customer != null) {
            return new ResettableAccount(AccountType.CUSTOMER, customer);
        }

        Stylist stylist = stylistRepository.findByEmailIgnoreCase(normalizedEmail).orElse(null);
        if (stylist != null) {
            return new ResettableAccount(AccountType.STYLIST, stylist);
        }

        Employee employee = employeeRepository.findByEmailIgnoreCase(normalizedEmail).orElse(null);
        if (employee != null) {
            return new ResettableAccount(AccountType.EMPLOYEE, employee);
        }

        return null;
    }

    private ResettableAccount requireValidResetAccount(String rawToken) {
        if (!hasText(rawToken)) {
            throw new IllegalArgumentException(INVALID_TOKEN_MESSAGE);
        }

        String tokenHash = passwordResetTokenService.hashToken(rawToken);
        ResettableAccount account = findAccountByResetTokenHash(tokenHash);
        if (account == null) {
            throw new IllegalArgumentException(INVALID_TOKEN_MESSAGE);
        }
        if (passwordResetTokenService.isExpired(account.user().getPasswordResetTokenExpiresAt())) {
            throw new IllegalArgumentException(EXPIRED_TOKEN_MESSAGE);
        }

        return account;
    }

    private ResettableAccount findAccountByResetTokenHash(String tokenHash) {
        Customer customer = customerRepository.findByPasswordResetTokenHash(tokenHash).orElse(null);
        if (customer != null) {
            return new ResettableAccount(AccountType.CUSTOMER, customer);
        }

        Stylist stylist = stylistRepository.findByPasswordResetTokenHash(tokenHash).orElse(null);
        if (stylist != null) {
            return new ResettableAccount(AccountType.STYLIST, stylist);
        }

        Employee employee = employeeRepository.findByPasswordResetTokenHash(tokenHash).orElse(null);
        if (employee != null) {
            return new ResettableAccount(AccountType.EMPLOYEE, employee);
        }

        return null;
    }

    private void save(ResettableAccount account) {
        switch (account.type()) {
            case CUSTOMER:
                customerRepository.save((Customer) account.user());
                break;
            case STYLIST:
                stylistRepository.save((Stylist) account.user());
                break;
            case EMPLOYEE:
                employeeRepository.save((Employee) account.user());
                break;
            default:
                throw new IllegalStateException("Unsupported account type.");
        }
    }

    private void clearFirstTimeSetupTokenIfPresent(User user) {
        if (user instanceof Customer customer) {
            customer.clearPasswordSetupToken();
        } else if (user instanceof Stylist stylist) {
            stylist.clearPasswordSetupToken();
        }
    }

    private void validateEmail(String email) {
        if (!hasText(email) || !EMAIL_PATTERN.matcher(email.trim()).matches()) {
            throw new IllegalArgumentException("Please enter a valid email address.");
        }
    }

    private void validateNewPassword(String newPassword, String confirmPassword) {
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

    public record PasswordResetCompletion(String loginPath) {
    }

    private record ResettableAccount(AccountType type, User user) {
    }

    private enum AccountType {
        CUSTOMER("customer", "/customers?action=login"),
        STYLIST("stylist", "/staff-login"),
        EMPLOYEE("staff", "/staff-login");

        private final String emailLabel;
        private final String loginPath;

        AccountType(String emailLabel, String loginPath) {
            this.emailLabel = emailLabel;
            this.loginPath = loginPath;
        }

        private String emailLabel() {
            return emailLabel;
        }

        private String loginPath() {
            return loginPath;
        }
    }
}
