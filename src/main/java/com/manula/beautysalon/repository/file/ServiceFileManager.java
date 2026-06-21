package com.manula.beautysalon.repository.file;

import com.manula.beautysalon.model.PackageService;
import com.manula.beautysalon.model.SalonService;
import com.manula.beautysalon.model.StandardService;

import java.io.*;
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

        // CHANGED: Split by 7 to account for the new stylist column
        String[] parts = line.split(",", 7);
        if (parts.length < 5) {
            return null;
        }

        try {
            int id = Integer.parseInt(parts[0].trim());
            String type = parts[1].trim();
            String name = parts[2].trim();
            String description = parts[3].trim();
            double basePrice = Double.parseDouble(parts[4].trim());

            String imageFileName = (parts.length >= 6) ? parts[5].trim() : "default-service.jpg";
            // NEW: Grabs the assigned stylist. Defaults to "Unassigned" for older data to prevent crashes.
            String stylistName = (parts.length == 7) ? parts[6].trim() : "Unassigned";

            if ("Package".equalsIgnoreCase(type)) {
                return new PackageService(id, name, description, basePrice, imageFileName, stylistName);
            }
            if ("Standard".equalsIgnoreCase(type)) {
                return new StandardService(id, name, description, basePrice, imageFileName, stylistName);
            }
        } catch (NumberFormatException ignored) {
            return null;
        }

        return null;
    }

    private String toLine(SalonService service) {
        String type = service instanceof PackageService ? "Package" : "Standard";
        // NEW: Appends the assigned stylist to the end of the data line
        return service.getServiceId()
                + "," + type
                + "," + sanitizeField(service.getName())
                + "," + sanitizeField(service.getDescription())
                + "," + service.getBasePrice()
                + "," + sanitizeField(service.getImageFileName())
                + "," + sanitizeField(service.getStylistName());
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

    public int generateNextServiceId() throws IOException {
        List<SalonService> services = readAllServices();
        int maxId = 2000;

        for (SalonService service : services) {
            if (service.getServiceId() > maxId) {
                maxId = service.getServiceId();
            }
        }
        return maxId + 1;
    }
}
