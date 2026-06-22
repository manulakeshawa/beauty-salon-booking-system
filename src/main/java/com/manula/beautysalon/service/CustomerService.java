package com.manula.beautysalon.service;

import com.manula.beautysalon.model.Customer;
import com.manula.beautysalon.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository customerRepository;

    public CustomerService(CustomerRepository customerRepository) {
        this.customerRepository = customerRepository;
    }

    public Customer saveCustomer(Customer customer) {
        customer.setUserId(0);
        return customerRepository.save(customer);
    }

    public List<Customer> readAllCustomers() {
        return customerRepository.findAllByOrderByUserIdAsc();
    }

    public void updateCustomer(Customer updatedCustomer) {
        customerRepository.findById(updatedCustomer.getUserId()).ifPresent(existing -> {
            existing.setName(updatedCustomer.getName());
            existing.setEmail(updatedCustomer.getEmail());
            existing.setPassword(updatedCustomer.getPassword());
            existing.setCustomerType(updatedCustomer.getCustomerType());
            customerRepository.save(existing);
        });
    }

    public void deleteCustomer(int userId) {
        if (customerRepository.existsById(userId)) {
            customerRepository.deleteById(userId);
        }
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
}
