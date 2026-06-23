package com.manula.beautysalon.service;

import com.manula.beautysalon.repository.CustomerRepository;
import com.manula.beautysalon.repository.EmployeeRepository;
import com.manula.beautysalon.repository.StylistRepository;
import com.manula.beautysalon.util.EmailUtils;
import org.springframework.stereotype.Service;

@Service
public class AccountEmailService {

    public static final String DUPLICATE_EMAIL_MESSAGE =
            "This email address is already used by another customer, stylist, or staff account. Please use a different email.";

    private final CustomerRepository customerRepository;
    private final StylistRepository stylistRepository;
    private final EmployeeRepository employeeRepository;

    public AccountEmailService(CustomerRepository customerRepository, StylistRepository stylistRepository, EmployeeRepository employeeRepository) {
        this.customerRepository = customerRepository;
        this.stylistRepository = stylistRepository;
        this.employeeRepository = employeeRepository;
    }

    public String normalize(String email) {
        return EmailUtils.normalize(email);
    }

    public void assertCustomerEmailAvailable(String email, int currentCustomerId) {
        assertEmailAvailable(email, AccountType.CUSTOMER, currentCustomerId);
    }

    public void assertStylistEmailAvailable(String email, int currentStylistId) {
        assertEmailAvailable(email, AccountType.STYLIST, currentStylistId);
    }

    public void assertEmployeeEmailAvailable(String email, int currentEmployeeId) {
        assertEmailAvailable(email, AccountType.EMPLOYEE, currentEmployeeId);
    }

    private void assertEmailAvailable(String email, AccountType accountType, int currentUserId) {
        String normalizedEmail = normalize(email);
        if (normalizedEmail == null || normalizedEmail.isBlank()) {
            return;
        }

        boolean usedByCustomer = accountType == AccountType.CUSTOMER && currentUserId > 0
                ? customerRepository.existsByEmailIgnoreCaseAndUserIdNot(normalizedEmail, currentUserId)
                : customerRepository.existsByEmailIgnoreCase(normalizedEmail);

        boolean usedByStylist = accountType == AccountType.STYLIST && currentUserId > 0
                ? stylistRepository.existsByEmailIgnoreCaseAndUserIdNot(normalizedEmail, currentUserId)
                : stylistRepository.existsByEmailIgnoreCase(normalizedEmail);

        boolean usedByEmployee = accountType == AccountType.EMPLOYEE && currentUserId > 0
                ? employeeRepository.existsByEmailIgnoreCaseAndUserIdNot(normalizedEmail, currentUserId)
                : employeeRepository.existsByEmailIgnoreCase(normalizedEmail);

        if (usedByCustomer || usedByStylist || usedByEmployee) {
            throw new DuplicateEmailException(DUPLICATE_EMAIL_MESSAGE);
        }
    }

    private enum AccountType {
        CUSTOMER,
        STYLIST,
        EMPLOYEE
    }
}
