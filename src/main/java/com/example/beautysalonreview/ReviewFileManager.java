package com.example.beautysalonreview;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class ReviewFileManager {

    // THE FIX: Pointing exactly to the root folder where your screenshot shows the file is!
    private static final String FILE_PATH = "reviews.txt";

    public List<Review> readAllReviews() {
        List<Review> reviews = new ArrayList<>();
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            System.out.println("🚨 ERROR: Cannot find the file at " + file.getAbsolutePath());
            return reviews;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Review review = parseLine(line);
                if (review != null) {
                    reviews.add(review); // Adds every single review to the list!
                }
            }
        } catch (IOException e) {
            System.out.println("🚨 ERROR reading file: " + e.getMessage());
        }

        return reviews;
    }

    private Review parseLine(String line) {
        // Removes any invisible characters that corrupt the text
        line = line.replace("\uFEFF", "").trim();

        if (line.isEmpty()) {
            return null;
        }

        // Splits your database using the pipe symbol
        String[] parts = line.split("\\|", -1);

        try {
            // Checks if the line starts with 'v2' to align the columns perfectly
            int startIndex = line.startsWith("v2") ? 1 : 0;

            if (parts.length >= startIndex + 6) {
                int id = Integer.parseInt(parts[startIndex].trim());
                String customer = parts[startIndex + 1].trim();
                String service = parts[startIndex + 2].trim();
                String stylist = parts[startIndex + 3].trim(); 
                
                int rating = 5; // Default fallback rating
                if (!parts[startIndex + 4].trim().isEmpty()) {
                    rating = Integer.parseInt(parts[startIndex + 4].trim());
                }
                
                String comment = parts[startIndex + 5].trim();
                
                return new Review(id, customer, service, stylist, rating, comment);
            }
        } catch (Exception e) {
            // Skips broken lines without crashing your server
            System.out.println("⚠️ Skipped a corrupted line in reviews.txt");
            return null;
        }
        return null;
    }

    // NEW: Auto-Increment Engine for Reviews (5000 Block)
    public int generateNextReviewId() throws IOException {
        List<Review> reviews = readAllReviews();
        int maxId = 5000; // Reviews will use the 5000 sequence block

        for (Review review : reviews) {
            if (review.getReviewId() > maxId) {
                maxId = review.getReviewId();
            }
        }
        return maxId + 1;
    }
}