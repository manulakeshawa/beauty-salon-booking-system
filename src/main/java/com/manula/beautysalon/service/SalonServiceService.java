package com.manula.beautysalon.service;

import com.manula.beautysalon.model.SalonService;
import com.manula.beautysalon.repository.AppointmentRepository;
import com.manula.beautysalon.repository.ReviewRepository;
import com.manula.beautysalon.repository.SalonServiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SalonServiceService {

    private final SalonServiceRepository serviceRepository;
    private final AppointmentRepository appointmentRepository;
    private final ReviewRepository reviewRepository;

    public SalonServiceService(SalonServiceRepository serviceRepository, AppointmentRepository appointmentRepository, ReviewRepository reviewRepository) {
        this.serviceRepository = serviceRepository;
        this.appointmentRepository = appointmentRepository;
        this.reviewRepository = reviewRepository;
    }

    public SalonService createService(SalonService service) {
        service.setServiceId(0);
        service.setActive(true);
        return serviceRepository.save(service);
    }

    public List<SalonService> readAllServices() {
        return readActiveServices();
    }

    public List<SalonService> readActiveServices() {
        return serviceRepository.findActiveOrderByServiceIdAsc();
    }

    public List<SalonService> readAllServicesIncludingInactive() {
        return serviceRepository.findAllByOrderByServiceIdAsc();
    }

    @Transactional
    public boolean updateService(SalonService updatedService) {
        SalonService existingService = findById(updatedService.getServiceId());
        if (existingService == null) {
            return false;
        }

        if (!existingService.getClass().equals(updatedService.getClass())) {
            if (hasServiceHistory(existingService.getName())) {
                existingService.setActive(false);
                serviceRepository.save(existingService);
            } else {
                serviceRepository.delete(existingService);
                serviceRepository.flush();
            }
            updatedService.setServiceId(0);
            updatedService.setActive(true);
            serviceRepository.save(updatedService);
            return true;
        }

        String previousName = existingService.getName();
        copyFields(existingService, updatedService);
        serviceRepository.save(existingService);

        if (hasText(previousName) && hasText(existingService.getName())
                && !previousName.equalsIgnoreCase(existingService.getName())) {
            appointmentRepository.updateServiceNameIgnoreCase(previousName, existingService.getName());
            reviewRepository.updateServiceNameIgnoreCase(previousName, existingService.getName());
        }
        return true;
    }

    @Transactional
    public boolean deleteService(int serviceId) {
        SalonService service = findById(serviceId);
        if (service == null) {
            return false;
        }
        if (hasServiceHistory(service.getName())) {
            service.setActive(false);
            serviceRepository.save(service);
            return true;
        }
        serviceRepository.delete(service);
        return true;
    }

    public SalonService findById(int serviceId) {
        return serviceRepository.findById(serviceId).orElse(null);
    }

    public SalonService findByNameIncludingInactive(String serviceName) {
        if (!hasText(serviceName)) {
            return null;
        }

        List<SalonService> matches = serviceRepository.findByNameIgnoreCaseOrderByServiceIdDesc(serviceName);
        if (matches.isEmpty()) {
            return null;
        }

        return matches.stream()
                .filter(SalonService::isActive)
                .findFirst()
                .orElse(matches.get(0));
    }

    public int generateNextServiceId() {
        return serviceRepository.findTopByOrderByServiceIdDesc()
                .map(service -> service.getServiceId() + 1)
                .orElse(1);
    }

    private void copyFields(SalonService target, SalonService source) {
        target.setName(source.getName());
        target.setDescription(source.getDescription());
        target.setBasePrice(source.getBasePrice());
        target.setImageFileName(source.getImageFileName());
        target.setStylistName(source.getStylistName());
    }

    private boolean hasServiceHistory(String serviceName) {
        return hasText(serviceName)
                && (appointmentRepository.existsByServiceNameIgnoreCase(serviceName)
                || reviewRepository.existsByServiceNameIgnoreCase(serviceName));
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
