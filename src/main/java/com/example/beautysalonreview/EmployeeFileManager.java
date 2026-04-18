package com.example.beautysalonreview;

import java.util.ArrayList;
import java.util.List;

public class EmployeeFileManager {

    private final List<Employee> employees = new ArrayList<>();

    public EmployeeFileManager() {
        employees.add(new Employee("admin", "lumiere2026", "Salon Owner", "MANAGER"));
        employees.add(new Employee("kasun", "stylist123", "Kasun", "STYLIST"));
    }

    public Employee authenticate(String username, String password) {
        for (Employee employee : employees) {
            if (employee.getUsername().equals(username) && employee.getPassword().equals(password)) {
                return employee;
            }
        }
        return null;
    }
}
