package com.example.beautysalonreview.repository;

import com.example.beautysalonreview.model.*;
import com.example.beautysalonreview.controller.*;
import com.example.beautysalonreview.repository.*;
import com.example.beautysalonreview.util.*;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository Class responsible for File I/O operations (CRUD).
 * This class acts as the Data Access Object (DAO), managing the persistence
 * of Customer objects into a flat text file acting as our database.
 */
public class CustomerFileManager {

    // Defines the physical location of our text file database.
    private static final String FILE_PATH = "customers.txt";

    /**
     * CREATE operation.
     * Uses a 'try-with-resources' block to ensure the BufferedWriter closes automatically.
     * The 'true' parameter in FileWriter enables Append Mode so it doesn't overwrite existing data.
     */
    public void saveCustomer(Customer customer) throws IOException {
        ensureDataFileExists();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(toLine(customer)); // Serializes the Java Object into a CSV string
            writer.newLine();
        }
    }

    /**
     * READ operation.
     * Loads the entire text file into server memory as an ArrayList of Customer objects.
     */
    public List<Customer> readAllCustomers() throws IOException {
        ensureDataFileExists();
        List<Customer> customers = new ArrayList<>();

        // Reads the file line by line
        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Customer customer = parseLine(line); // Deserializes the CSV string back into a Java Object
                if (customer != null) {
                    customers.add(customer);
                }
            }
        }

        return customers;
    }

    /**
     * UPDATE operation.
     * Since we cannot edit a specific line in a flat file, we read the whole file into memory,
     * update the specific object in the ArrayList, and rewrite the entire file.
     */
    public void updateCustomer(Customer updatedCustomer) throws IOException {
        ensureDataFileExists();
        List<Customer> customers = readAllCustomers();

        // Standard for-loop to find the matching ID and replace the old object with the new one
        for (int i = 0; i < customers.size(); i++) {
            if (customers.get(i).getUserId() == updatedCustomer.getUserId()) {
                customers.set(i, updatedCustomer);
                break;
            }
        }

        writeAllCustomers(customers); // Overwrite the database with the updated list
    }

    /**
     * DELETE operation.
     */
    public void deleteCustomer(int userId) throws IOException {
        ensureDataFileExists();
        List<Customer> customers = readAllCustomers();

        // OOP FLEX: Using a Java 8 Lambda Expression (->) to efficiently filter and remove the target object
        customers.removeIf(customer -> customer.getUserId() == userId);

        writeAllCustomers(customers); // Overwrite the database without the deleted user
    }

    /**
     * Helper Method: Overwrites the entire file.
     * Notice FileWriter(FILE_PATH, false) -> 'false' means it wipes the old file and starts fresh.
     */
    private void writeAllCustomers(List<Customer> customers) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, false))) {
            for (Customer customer : customers) {
                writer.write(toLine(customer));
                writer.newLine();
            }
        }
    }

    /**
     * Helper Method (Deserialization): Converts a single CSV string line into a Customer object.
     */
    private Customer parseLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        // Splits the comma-separated string into an array of exactly 5 parts
        String[] parts = line.split(",", 5);
        if (parts.length != 5) {
            return null;
        }

        try {
            // Parses string data back into appropriate Java data types
            int userId = Integer.parseInt(parts[0].trim());
            String name = parts[1].trim();
            String email = parts[2].trim();
            String password = parts[3].trim();
            String customerType = parts[4].trim();
            return new Customer(userId, name, email, password, customerType);
        } catch (NumberFormatException ignored) {
            return null; // Failsafe: if the ID isn't a number, skip this corrupted line
        }
    }

    /**
     * Helper Method (Serialization): Converts a Customer object into a CSV string for storage.
     */
    private String toLine(Customer customer) {
        return customer.getUserId()
                + "," + sanitizeField(customer.getName())
                + "," + sanitizeField(customer.getEmail())
                + "," + sanitizeField(customer.getPassword())
                + "," + sanitizeField(customer.getCustomerType());
    }

    /**
     * Data validation: Removes accidental commas from user input so it doesn't break our CSV format.
     */
    private String sanitizeField(String value) {
        if (value == null) {
            return "";
        }
        return value.replace(",", " ").trim();
    }

    /**
     * Initialization step: Checks if the database text file exists. If not, it safely creates an empty one.
     */
    private void ensureDataFileExists() throws IOException {
        File file = new File(FILE_PATH);
        File parent = file.getParentFile();

        // Creates the folder directory if it doesn't exist
        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        // Creates the physical .txt file
        if (!file.exists()) {
            file.createNewFile();
        }
    }

    /**
     * Auto-Increment Engine for primary keys (IDs).
     * Prevents duplicate IDs by finding the current highest ID and adding 1.
     */
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