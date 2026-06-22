package com.manula.beautysalon.service;

import com.manula.beautysalon.model.Appointment;
import com.manula.beautysalon.model.Customer;
import com.manula.beautysalon.model.Review;
import com.manula.beautysalon.model.SalonService;
import com.manula.beautysalon.model.Stylist;
import com.manula.beautysalon.repository.AppointmentRepository;
import com.manula.beautysalon.repository.CustomerRepository;
import com.manula.beautysalon.repository.ReviewRepository;
import com.manula.beautysalon.repository.SalonServiceRepository;
import com.manula.beautysalon.repository.StylistRepository;
import com.manula.beautysalon.repository.file.AppointmentFileManager;
import com.manula.beautysalon.repository.file.CustomerFileManager;
import com.manula.beautysalon.repository.file.ServiceFileManager;
import com.manula.beautysalon.repository.file.StylistFileManager;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.io.BufferedReader;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

@Component
public class LegacyDataInitializer implements ApplicationRunner {

    private static final Path LEGACY_REVIEWS_FILE = Path.of("reviews.txt");

    private final ReviewRepository reviewRepository;
    private final SalonServiceRepository serviceRepository;
    private final StylistRepository stylistRepository;
    private final CustomerRepository customerRepository;
    private final AppointmentRepository appointmentRepository;

    public LegacyDataInitializer(
            ReviewRepository reviewRepository,
            SalonServiceRepository serviceRepository,
            StylistRepository stylistRepository,
            CustomerRepository customerRepository,
            AppointmentRepository appointmentRepository
    ) {
        this.reviewRepository = reviewRepository;
        this.serviceRepository = serviceRepository;
        this.stylistRepository = stylistRepository;
        this.customerRepository = customerRepository;
        this.appointmentRepository = appointmentRepository;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        importMissingLegacyReviews();
        importServicesIfEmpty();
        importStylistsIfEmpty();
        importCustomersIfEmpty();
        importAppointmentsIfEmpty();
    }

    private void importMissingLegacyReviews() {
        try {
            List<Review> existingReviews = reviewRepository.findAll();
            int importedCount = 0;
            int duplicateCount = 0;

            for (Review review : readLegacyReviews()) {
                if (containsSameReview(existingReviews, review)) {
                    duplicateCount++;
                    continue;
                }

                review.setReviewId(null);
                reviewRepository.save(review);
                existingReviews.add(review);
                importedCount++;
            }

            System.out.println("Legacy reviews import complete. Imported: " + importedCount + ", skipped duplicates: " + duplicateCount);
        } catch (Exception e) {
            System.err.println("Could not import legacy reviews.txt data: " + e.getMessage());
        }
    }

    private List<Review> readLegacyReviews() throws Exception {
        List<Review> reviews = new ArrayList<>();

        if (!Files.exists(LEGACY_REVIEWS_FILE)) {
            return reviews;
        }

        try (BufferedReader reader = Files.newBufferedReader(LEGACY_REVIEWS_FILE)) {
            String line;
            while ((line = reader.readLine()) != null) {
                Review review = parseLegacyReviewLine(line);
                if (review != null) {
                    reviews.add(review);
                }
            }
        }

        return reviews;
    }

    private Review parseLegacyReviewLine(String line) {
        String normalizedLine = line == null ? "" : line.replace("\uFEFF", "").trim();
        if (normalizedLine.isEmpty()) {
            return null;
        }

        String[] rawParts = normalizedLine.split("\\s*\\|\\s*", -1);
        List<String> parts = new ArrayList<>();
        for (String rawPart : rawParts) {
            parts.add(rawPart);
        }

        if (!parts.isEmpty() && "v2".equalsIgnoreCase(parts.get(0))) {
            parts.remove(0);
        }

        if (parts.size() < 7) {
            return null;
        }

        try {
            int index = 0;
            index++; // Legacy file ID. MySQL generates the new reviewId.
            String customerName = parts.get(index++);
            String serviceName = parts.get(index++);
            String stylistName = parts.get(index++);
            int rating = parseRating(parts.get(index++));
            String comment = parts.get(index++);
            String ownerToken = "";
            boolean verified = true;

            if (parts.size() > index) {
                String firstOptionalValue = parts.get(index++);
                if (isBoolean(firstOptionalValue) && parts.size() == index) {
                    verified = Boolean.parseBoolean(firstOptionalValue);
                } else {
                    ownerToken = firstOptionalValue;
                    if (parts.size() > index && isBoolean(parts.get(index))) {
                        verified = Boolean.parseBoolean(parts.get(index));
                    }
                }
            }

            return new Review(
                    null,
                    customerName,
                    serviceName,
                    stylistName,
                    rating,
                    comment,
                    ownerToken,
                    verified
            );
        } catch (Exception e) {
            return null;
        }
    }

    private boolean containsSameReview(List<Review> existingReviews, Review importedReview) {
        for (Review existingReview : existingReviews) {
            if (sameText(existingReview.getCustomerName(), importedReview.getCustomerName())
                    && sameText(existingReview.getServiceName(), importedReview.getServiceName())
                    && sameText(existingReview.getStylistName(), importedReview.getStylistName())
                    && existingReview.getRating() == importedReview.getRating()
                    && sameText(existingReview.getComment(), importedReview.getComment())) {
                return true;
            }
        }
        return false;
    }

    private boolean sameText(String first, String second) {
        return normalizeText(first).equalsIgnoreCase(normalizeText(second));
    }

    private String normalizeText(String value) {
        return value == null ? "" : value.trim();
    }

    private boolean isBoolean(String value) {
        return "true".equalsIgnoreCase(value) || "false".equalsIgnoreCase(value);
    }

    private int parseRating(String value) {
        if (value == null || value.isBlank()) {
            return 5;
        }
        return Integer.parseInt(value.trim());
    }

    private void importServicesIfEmpty() {
        if (serviceRepository.count() > 0) {
            return;
        }

        try {
            for (SalonService service : new ServiceFileManager().readAllServices()) {
                service.setServiceId(0);
                serviceRepository.save(service);
            }
        } catch (Exception ignored) {
        }
    }

    private void importStylistsIfEmpty() {
        if (stylistRepository.count() > 0) {
            return;
        }

        try {
            for (Stylist stylist : new StylistFileManager().readAllStylists()) {
                stylist.setUserId(0);
                stylistRepository.save(stylist);
            }
        } catch (Exception ignored) {
        }
    }

    private void importCustomersIfEmpty() {
        if (customerRepository.count() > 0) {
            return;
        }

        try {
            for (Customer customer : new CustomerFileManager().readAllCustomers()) {
                customer.setUserId(0);
                customerRepository.save(customer);
            }
        } catch (Exception ignored) {
        }
    }

    private void importAppointmentsIfEmpty() {
        if (appointmentRepository.count() > 0) {
            return;
        }

        try {
            for (Appointment appointment : new AppointmentFileManager().readAllAppointments()) {
                appointment.setAppointmentId(0);
                appointmentRepository.save(appointment);
            }
        } catch (Exception ignored) {
        }
    }
}
