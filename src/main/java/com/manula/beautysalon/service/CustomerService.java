package com.manula.beautysalon.service;

import com.manula.beautysalon.model.Customer;
import com.manula.beautysalon.repository.AppointmentRepository;
import com.manula.beautysalon.repository.CustomerRepository;
import com.manula.beautysalon.repository.ReviewRepository;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {

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
}
