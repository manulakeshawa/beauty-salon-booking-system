package com.example.beautysalonreview;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.util.List;

@Controller
public class ServiceWebController {

    private final ServiceFileManager serviceFileManager = new ServiceFileManager();

    @GetMapping("/services")
    public String handleServicesGet(
            @RequestParam(defaultValue = "list") String action,
            @RequestParam(required = false) Integer serviceId,
            HttpSession session,
            Model model
    ) {
        if (session.getAttribute("staffRole") == null) { return "redirect:/staff-login"; }
        try {
            switch (action.toLowerCase()) {
                case "new":
                    if (!SecurityUtils.isManager(session)) {
                        return "redirect:/admin?error=unauthorized";
                    }
                    model.addAttribute("generatedServiceId", serviceFileManager.generateNextServiceId());
                    return "AddService";
                case "edit":
                    if (!SecurityUtils.isManager(session)) {
                        return "redirect:/admin?error=unauthorized";
                    }
                    if (serviceId == null) {
                        return "redirect:/services?action=list";
                    }
                    SalonService service = findById(serviceId);
                    if (service == null) {
                        return "redirect:/services?action=list";
                    }
                    model.addAttribute("service", service);
                    model.addAttribute("typeValue", service instanceof PackageService ? "package" : "standard");
                    return "EditService";
                case "delete":
                    if (!SecurityUtils.isManager(session)) {
                        return "redirect:/admin?error=unauthorized";
                    }
                    if (serviceId != null) {
                        serviceFileManager.deleteService(serviceId);
                    }
                    return "redirect:/services?action=list";
                case "list":
                default:
                    List<SalonService> services = serviceFileManager.readAllServices();
                    model.addAttribute("services", services);
                    return "ServiceList";
            }
        } catch (IOException ignored) {
            return "redirect:/services?action=list";
        }
    }

    @PostMapping("/services")
    public String handleServicesPost(
            @RequestParam(required = false) String action,
            @RequestParam(required = false, defaultValue = "0") Integer serviceId,
            @RequestParam(required = false) String type,
            @RequestParam(required = false) String name,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Double basePrice,
            HttpSession session
    ) {
        if (session.getAttribute("staffRole") == null) { return "redirect:/staff-login"; }
        if (!SecurityUtils.isManager(session)) {
            return "redirect:/admin?error=unauthorized";
        }
        try {
            boolean isNew = "new".equalsIgnoreCase(action) || serviceId == null || serviceId == 0;
            if (type == null || name == null || description == null || basePrice == null) {
                return "redirect:/services?action=list";
            }
            
            // Generates a bulletproof ID if it's new, otherwise uses the existing one
            int finalServiceId = isNew ? serviceFileManager.generateNextServiceId() : serviceId;

            SalonService existingService = null;
            if (!isNew) {
                existingService = findById(finalServiceId);
            }

            // Preserves the existing image if updating, otherwise use default
            String imageToUse = (existingService != null && existingService.getImageFileName() != null) 
                                ? existingService.getImageFileName() 
                                : "default-service.jpg";

            SalonService service = toService(finalServiceId, type, name, description, basePrice.doubleValue(), imageToUse);
            
            if (service == null) {
                return "redirect:/services?action=list";
            }

            if (isNew) {
                serviceFileManager.createService(service);
            } else {
                serviceFileManager.updateService(service);
            }
        } catch (IOException ignored) {
            return "redirect:/services?action=list";
        }

        return "redirect:/services?action=list";
    }

    private SalonService findById(int serviceId) throws IOException {
        List<SalonService> services = serviceFileManager.readAllServices();
        for (SalonService service : services) {
            if (service.getServiceId() == serviceId) {
                return service;
            }
        }
        return null;
    }

    private SalonService toService(int serviceId, String type, String name, String description, double basePrice, String imageFileName) {
        if ("package".equalsIgnoreCase(type)) {
            return new PackageService(serviceId, name, description, basePrice, imageFileName);
        }
        if ("standard".equalsIgnoreCase(type)) {
            return new StandardService(serviceId, name, description, basePrice, imageFileName);
        }
        return null;
    }
}