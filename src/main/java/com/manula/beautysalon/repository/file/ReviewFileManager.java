package com.manula.beautysalon.repository.file;

import com.manula.beautysalon.model.Review;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class ReviewFileManager {

    private static final String FILE_PATH = "reviews.txt";

    public List<Review> readAllReviews() {
        List<Review> reviews = new ArrayList<>();
        File file = new File(FILE_PATH);

        if (!file.exists()) {
            return reviews;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String line;
            while ((line = reader.readLine()) != null) {
                Review review = parseLine(line);
                if (review != null) {
                    reviews.add(review);
                }
            }
        } catch (IOException e) {
            System.out.println("🚨 ERROR reading file: " + e.getMessage());
        }
        return reviews;
    }

    private Review parseLine(String line) {
        line = line.replace("\uFEFF", "").trim();
        if (line.isEmpty()) return null;

        String[] parts = line.split("\\|", -1);

        try {
            int startIndex = line.startsWith("v2") ? 1 : 0;

            if (parts.length >= startIndex + 6) {
                int id = Integer.parseInt(parts[startIndex].trim());
                String customer = parts[startIndex + 1].trim();
                String service = parts[startIndex + 2].trim();
                String stylist = parts[startIndex + 3].trim();

                int rating = 5;
                if (!parts[startIndex + 4].trim().isEmpty()) {
                    rating = Integer.parseInt(parts[startIndex + 4].trim());
                }

                String comment = parts[startIndex + 5].trim();

                // Extract ownerToken if it exists (7th column)
                String token = (parts.length > startIndex + 6) ? parts[startIndex + 6].trim() : "";

                // NEW: Extract verified status if it exists (8th column)
                // Safety fallback: if the text file doesn't have this column yet, it defaults to false.
                boolean isVerified = false;
                if (parts.length > startIndex + 7) {
                    isVerified = Boolean.parseBoolean(parts[startIndex + 7].trim());
                }

                Review r = new Review(id, customer, service, stylist, rating, comment);
                r.setOwnerToken(token);
                r.setVerified(isVerified); // NEW: Applies the verified status
                return r;
            }
        } catch (Exception e) {
            return null;
        }
        return null;
    }

    // ==========================================================
    // WRITE & UPDATE LOGIC
    // ==========================================================

    /**
     * Overwrites the entire file with a new list.
     * Used after an edit or delete.
     */
    public void writeAllReviews(List<Review> reviews) {
        try (PrintWriter writer = new PrintWriter(new FileWriter(FILE_PATH))) {
            for (Review r : reviews) {
                writer.println(toLine(r));
            }
        } catch (IOException e) {
            System.err.println("🚨 ERROR writing reviews: " + e.getMessage());
        }
    }

    /**
     * THE KEY METHOD: Finds a review by ID and updates its content.
     */
    public void updateReview(Review updatedReview) {
        List<Review> reviews = readAllReviews();
        for (int i = 0; i < reviews.size(); i++) {
            if (reviews.get(i).getReviewId() == updatedReview.getReviewId()) {
                reviews.set(i, updatedReview); // Replace the old review with the new one
                break;
            }
        }
        writeAllReviews(reviews); // Save the whole list back to the file
    }

    /**
     * Helper to turn a Review object back into a string line for the TXT file.
     */
    private String toLine(Review r) {
        // Keeps the v2 format and appends the boolean verified flag at the very end
        return "v2 | " +
                r.getReviewId() + " | " +
                r.getCustomerName() + " | " +
                r.getServiceName() + " | " +
                (r.getStylistName() == null ? "" : r.getStylistName()) + " | " +
                r.getRating() + " | " +
                r.getComment() + " | " +
                (r.getOwnerToken() == null ? "" : r.getOwnerToken()) + " | " +
                r.isVerified(); // NEW: Writes true or false to the file
    }

    public int generateNextReviewId() throws IOException {
        List<Review> reviews = readAllReviews();
        int maxId = 5000;
        for (Review review : reviews) {
            if (review.getReviewId() > maxId) {
                maxId = review.getReviewId();
            }
        }
        return maxId + 1;
    }
}
