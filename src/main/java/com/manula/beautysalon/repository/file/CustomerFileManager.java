package com.manula.beautysalon.repository.file;

import com.manula.beautysalon.model.Customer;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class CustomerFileManager {

    private static final String FILE_PATH = "customers.txt";

    public void saveCustomer(Customer customer) throws IOException {
        ensureDataFileExists();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(toLine(customer));
            writer.newLine();
        }
    }

    public List<Customer> readAllCustomers() throws IOException {
        ensureDataFileExists();
        List<Customer> customers = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Customer customer = parseLine(line);
                if (customer != null) {
                    customers.add(customer);
                }
            }
        }

        return customers;
    }

    public void updateCustomer(Customer updatedCustomer) throws IOException {
        ensureDataFileExists();
        List<Customer> customers = readAllCustomers();

        for (int i = 0; i < customers.size(); i++) {
            if (customers.get(i).getUserId() == updatedCustomer.getUserId()) {
                customers.set(i, updatedCustomer);
                break;
            }
        }

        writeAllCustomers(customers);
    }

    public void deleteCustomer(int userId) throws IOException {
        ensureDataFileExists();
        List<Customer> customers = readAllCustomers();
        customers.removeIf(customer -> customer.getUserId() == userId);
        writeAllCustomers(customers);
    }

    private void writeAllCustomers(List<Customer> customers) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, false))) {
            for (Customer customer : customers) {
                writer.write(toLine(customer));
                writer.newLine();
            }
        }
    }

    private Customer parseLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        String[] parts = line.split(",", 5);
        if (parts.length != 5) {
            return null;
        }

        try {
            int userId = Integer.parseInt(parts[0].trim());
            String name = parts[1].trim();
            String email = parts[2].trim();
            String password = parts[3].trim();
            String customerType = parts[4].trim();
            return new Customer(userId, name, email, password, customerType);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String toLine(Customer customer) {
        return customer.getUserId()
                + "," + sanitizeField(customer.getName())
                + "," + sanitizeField(customer.getEmail())
                + "," + sanitizeField(customer.getPassword())
                + "," + sanitizeField(customer.getCustomerType());
    }

    private String sanitizeField(String value) {
        if (value == null) {
            return "";
        }
        return value.replace(",", " ").trim();
    }

    private void ensureDataFileExists() throws IOException {
        File file = new File(FILE_PATH);
        File parent = file.getParentFile();

        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        if (!file.exists()) {
            file.createNewFile();
        }
    }

    // NEW: Auto-Increment Engine for Customers (1000 Block)
    public int generateNextCustomerId() throws IOException {
        List<Customer> customers = readAllCustomers();
        int maxId = 1000; // Customers will use the 1000 sequence block

        for (Customer customer : customers) {
            if (customer.getUserId() > maxId) {
                maxId = customer.getUserId();
            }
        }
        return maxId + 1;
    }
}
