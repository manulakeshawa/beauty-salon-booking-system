package com.manula.beautysalon.repository;

import com.manula.beautysalon.model.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CustomerRepository extends JpaRepository<Customer, Integer> {
    List<Customer> findAllByOrderByUserIdAsc();

    Optional<Customer> findByEmailIgnoreCase(String email);

    Optional<Customer> findByEmailIgnoreCaseAndPassword(String email, String password);

    Optional<Customer> findTopByOrderByUserIdDesc();
}
