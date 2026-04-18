package com.example.beautysalonreview;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ServiceFileManager {

    private static final String FILE_PATH = "data/services.txt";

    public void createService(SalonService service) throws IOException {
        ensureDataFileExists();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(toLine(service));
            writer.newLine();
        }
    }

    public List<SalonService> readAllServices() throws IOException {
        ensureDataFileExists();
        List<SalonService> services = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                SalonService service = parseLine(line);
                if (service != null) {
                    services.add(service);
                }
            }
        }

        return services;
    }

    public boolean updateService(SalonService updatedService) throws IOException {
        ensureDataFileExists();
        List<SalonService> services = readAllServices();
        boolean updated = false;

        for (int i = 0; i < services.size(); i++) {
            if (services.get(i).getServiceId() == updatedService.getServiceId()) {
                services.set(i, updatedService);
                updated = true;
                break;
            }
        }

        if (updated) {
            writeAllServices(services);
        }

        return updated;
    }

    public boolean deleteService(int serviceId) throws IOException {
        ensureDataFileExists();
        List<SalonService> services = readAllServices();
        boolean removed = services.removeIf(service -> service.getServiceId() == serviceId);

        if (removed) {
            writeAllServices(services);
        }

        return removed;
    }

    private void writeAllServices(List<SalonService> services) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, false))) {
            for (SalonService service : services) {
                writer.write(toLine(service));
                writer.newLine();
            }
        }
    }

    private SalonService parseLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        // Changed to split by 6 to account for the image column
        String[] parts = line.split(",", 6);
        if (parts.length < 5) {
            return null;
        }

        try {
            int id = Integer.parseInt(parts[0].trim());
            String type = parts[1].trim();
            String name = parts[2].trim();
            String description = parts[3].trim();
            double basePrice = Double.parseDouble(parts[4].trim());
            
            // NEW: Grabs the image filename. Defaults safely if none exists yet.
            String imageFileName = (parts.length == 6) ? parts[5].trim() : "default-service.jpg";

            if ("Package".equalsIgnoreCase(type)) {
                return new PackageService(id, name, description, basePrice, imageFileName);
            }
            if ("Standard".equalsIgnoreCase(type)) {
                return new StandardService(id, name, description, basePrice, imageFileName);
            }
        } catch (NumberFormatException ignored) {
            return null;
        }

        return null;
    }

    private String toLine(SalonService service) {
        String type = service instanceof PackageService ? "Package" : "Standard";
        // NEW: Appends the image filename to the end of the line
        return service.getServiceId()
                + "," + type
                + "," + sanitizeField(service.getName())
                + "," + sanitizeField(service.getDescription())
                + "," + service.getBasePrice()
                + "," + sanitizeField(service.getImageFileName()); 
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

    // NEW: Auto-Increment Engine for Services (2000 Block)
    public int generateNextServiceId() throws IOException {
        List<SalonService> services = readAllServices();
        int maxId = 2000; // Services will use the 2000 sequence block

        for (SalonService service : services) {
            if (service.getServiceId() > maxId) {
                maxId = service.getServiceId();
            }
        }
        return maxId + 1;
    }
}