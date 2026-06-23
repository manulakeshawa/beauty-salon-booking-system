package com.manula.beautysalon.service;

import com.manula.beautysalon.model.Customer;
import com.manula.beautysalon.repository.AppointmentRepository;
import com.manula.beautysalon.repository.CustomerRepository;
import com.manula.beautysalon.repository.ReviewRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;
    private final AppointmentRepository appointmentRepository;
    private final ReviewRepository reviewRepository;

    public CustomerService(CustomerRepository customerRepository, AppointmentRepository appointmentRepository, ReviewRepository reviewRepository) {
        this.customerRepository = customerRepository;
        this.appointmentRepository = appointmentRepository;
        this.reviewRepository = reviewRepository;
    }

    public Customer saveCustomer(Customer customer) {
        customer.setUserId(0);
        return customerRepository.save(customer);
    }

    public List<Customer> readAllCustomers() {
        return customerRepository.findAllByOrderByUserIdAsc();
    }

    @Transactional
    public void updateCustomer(Customer updatedCustomer) {
        customerRepository.findById(updatedCustomer.getUserId()).ifPresent(existing -> {
            String previousName = existing.getName();
            existing.setName(updatedCustomer.getName());
            existing.setEmail(updatedCustomer.getEmail());
            existing.setPassword(updatedCustomer.getPassword());
            existing.setCustomerType(updatedCustomer.getCustomerType());
            customerRepository.save(existing);

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
        return customerRepository.findByEmailIgnoreCase(email).orElse(null);
    }

    public Customer findByEmailAndPassword(String email, String password) {
        if (email == null || password == null) {
            return null;
        }
        return customerRepository.findByEmailIgnoreCaseAndPassword(email, password).orElse(null);
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
