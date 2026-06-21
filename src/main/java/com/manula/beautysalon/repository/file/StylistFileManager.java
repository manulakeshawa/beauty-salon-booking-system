package com.manula.beautysalon.repository.file;

import com.manula.beautysalon.model.Stylist;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class StylistFileManager {

    private static final String FILE_PATH = "stylists.txt";

    public void saveStylist(Stylist stylist) throws IOException {
        ensureDataFileExists();

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, true))) {
            writer.write(toLine(stylist));
            writer.newLine();
        }
    }

    public List<Stylist> readAllStylists() throws IOException {
        ensureDataFileExists();
        List<Stylist> stylists = new ArrayList<>();

        try (BufferedReader reader = new BufferedReader(new FileReader(FILE_PATH))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Stylist stylist = parseLine(line);
                if (stylist != null) {
                    stylists.add(stylist);
                }
            }
        }

        return stylists;
    }

    // FIXED: Universal Login - Accepts Name, Email, OR Personnel ID
    public Stylist authenticate(String identifier, String password) throws IOException {
        List<Stylist> stylists = readAllStylists();
        for (Stylist stylist : stylists) {

            String idString = String.valueOf(stylist.getUserId());

            // Checks if what they typed matches their exact Name, Email, OR ID
            if ((stylist.getName().equalsIgnoreCase(identifier) ||
                    stylist.getEmail().equalsIgnoreCase(identifier) ||
                    idString.equals(identifier))
                    && stylist.getPassword().equals(password)) {
                return stylist;
            }
        }
        return null;
    }

    public void updateStylist(Stylist updatedStylist) throws IOException {
        ensureDataFileExists();
        List<Stylist> stylists = readAllStylists();

        for (int i = 0; i < stylists.size(); i++) {
            if (stylists.get(i).getUserId() == updatedStylist.getUserId()) {
                stylists.set(i, updatedStylist);
                break;
            }
        }

        writeAllStylists(stylists);
    }

    public void deleteStylist(int userId) throws IOException {
        ensureDataFileExists();
        List<Stylist> stylists = readAllStylists();
        stylists.removeIf(stylist -> stylist.getUserId() == userId);
        writeAllStylists(stylists);
    }

    private void writeAllStylists(List<Stylist> stylists) throws IOException {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(FILE_PATH, false))) {
            for (Stylist stylist : stylists) {
                writer.write(toLine(stylist));
                writer.newLine();
            }
        }
    }

    private Stylist parseLine(String line) {
        if (line == null || line.trim().isEmpty()) {
            return null;
        }

        // Changed from 7 to 8 to account for the new image column
        String[] parts = line.split(",", 8);
        if (parts.length < 7) {
            return null;
        }

        try {
            int userId = Integer.parseInt(parts[0].trim());
            String name = parts[1].trim();
            String email = parts[2].trim();
            String password = parts[3].trim();
            String specialty = parts[4].trim();
            String level = parts[5].trim();
            boolean available = Boolean.parseBoolean(parts[6].trim());

            // NEW: Grabs the image file name. If an admin registers a new stylist without an image, it defaults safely.
            String imageFileName = (parts.length == 8) ? parts[7].trim() : "default.jpg";

            return new Stylist(userId, name, email, password, specialty, level, available, imageFileName);
        } catch (NumberFormatException ignored) {
            return null;
        }
    }

    private String toLine(Stylist stylist) {
        // NEW: Appends the image file name so it doesn't get erased during an update!
        return stylist.getUserId()
                + "," + sanitizeField(stylist.getName())
                + "," + sanitizeField(stylist.getEmail())
                + "," + sanitizeField(stylist.getPassword())
                + "," + sanitizeField(stylist.getSpecialty())
                + "," + sanitizeField(stylist.getLevel())
                + "," + stylist.isAvailable()
                + "," + sanitizeField(stylist.getImageFileName());
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

    // NEW: Auto-Increment Engine for Stylists (4000 Block)
    public int generateNextStylistId() throws IOException {
        List<Stylist> stylists = readAllStylists();
        int maxId = 4000; // Stylists will use the 4000 sequence block

        for (Stylist stylist : stylists) {
            // Note: Uses getUserId() because Stylist extends your User class!
            if (stylist.getUserId() > maxId) {
                maxId = stylist.getUserId();
            }
        }
        return maxId + 1;
    }
}
