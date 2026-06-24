package com.manula.beautysalon.repository;

import com.manula.beautysalon.model.Employee;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface EmployeeRepository extends JpaRepository<Employee, Integer> {
    List<Employee> findAllByOrderByUserIdAsc();

    Optional<Employee> findByEmailIgnoreCase(String email);

    Optional<Employee> findByPasswordResetTokenHash(String passwordResetTokenHash);

    boolean existsByEmailIgnoreCase(String email);

    boolean existsByEmailIgnoreCaseAndUserIdNot(String email, int userId);

    Optional<Employee> findByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCase(String username);

    boolean existsByUsernameIgnoreCaseAndUserIdNot(String username, int userId);
}
