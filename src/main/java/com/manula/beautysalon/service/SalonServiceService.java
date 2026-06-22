package com.manula.beautysalon.service;

import com.manula.beautysalon.model.SalonService;
import com.manula.beautysalon.repository.SalonServiceRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class SalonServiceService {

    private final SalonServiceRepository serviceRepository;

    public SalonServiceService(SalonServiceRepository serviceRepository) {
        this.serviceRepository = serviceRepository;
    }

    public SalonService createService(SalonService service) {
        service.setServiceId(0);
        return serviceRepository.save(service);
    }

    public List<SalonService> readAllServices() {
        return serviceRepository.findAllByOrderByServiceIdAsc();
    }

    @Transactional
    public boolean updateService(SalonService updatedService) {
        SalonService existingService = findById(updatedService.getServiceId());
        if (existingService == null) {
            return false;
        }

        if (!existingService.getClass().equals(updatedService.getClass())) {
            serviceRepository.delete(existingService);
            serviceRepository.flush();
            updatedService.setServiceId(0);
            serviceRepository.save(updatedService);
            return true;
        }

        copyFields(existingService, updatedService);
        serviceRepository.save(existingService);
        return true;
    }

    public boolean deleteService(int serviceId) {
        if (!serviceRepository.existsById(serviceId)) {
            return false;
        }
        serviceRepository.deleteById(serviceId);
        return true;
    }

    public SalonService findById(int serviceId) {
        return serviceRepository.findById(serviceId).orElse(null);
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
}
