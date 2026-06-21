package com.manula.beautysalon.controller;

import com.manula.beautysalon.model.PackageService;
import com.manula.beautysalon.model.SalonService;
import com.manula.beautysalon.model.StandardService;
import com.manula.beautysalon.repository.file.ServiceFileManager;
import com.manula.beautysalon.repository.file.StylistFileManager;
import com.manula.beautysalon.util.SecurityUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.io.IOException;
import java.util.List;

@Controller
public class ServiceWebController {

    private final ServiceFileManager serviceFileManager = new ServiceFileManager();
    // NEW: Inject StylistFileManager to fetch available stylists for assignment
    private final StylistFileManager stylistFileManager = new StylistFileManager();

    @GetMapping("/services")
    public String handleServicesGet(
            @RequestParam(defaultValue = "list") String action,
            @RequestParam(required = false) Integer serviceId,
            HttpSession session,
            Model model
    ) {
        if (session.getAttribute("staffRole") == null) {
            return "redirect:/staff-login";
        }
        try {
            switch (action.toLowerCase()) {
                case "new":
                    if (!SecurityUtils.isManager(session)) {
                        return "redirect:/admin?error=unauthorized";
                    }
                    model.addAttribute("generatedServiceId", serviceFileManager.generateNextServiceId());
                    // NEW: Pass the list of all stylists to the AddService view
                    model.addAttribute("stylists", stylistFileManager.readAllStylists());
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
                    // NEW: Pass the list of all stylists to the EditService view
                    model.addAttribute("stylists", stylistFileManager.readAllStylists());
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
            @RequestParam(required = false) String imageFileName,
            @RequestParam(required = false) String stylistName, // NEW: Catches the assigned stylist
            HttpSession session
    ) {
        if (session.getAttribute("staffRole") == null) {
            return "redirect:/staff-login";
        }
        if (!SecurityUtils.isManager(session)) {
            return "redirect:/admin?error=unauthorized";
        }
        try {
            boolean isNew = "new".equalsIgnoreCase(action) || serviceId == null || serviceId == 0;
            if (type == null || name == null || description == null || basePrice == null) {
                return "redirect:/services?action=list";
            }

            int finalServiceId = isNew ? serviceFileManager.generateNextServiceId() : serviceId;

            SalonService existingService = null;
            if (!isNew) {
                existingService = findById(finalServiceId);
            }

            String imageToUse;
            if (imageFileName != null && !imageFileName.isBlank()) {
                imageToUse = imageFileName;
            } else if (existingService != null && existingService.getImageFileName() != null) {
                imageToUse = existingService.getImageFileName();
            } else {
                imageToUse = "default-service.png";
            }

            // NEW: Prioritize form input > existing data > fallback
            String stylistToUse;
            if (stylistName != null && !stylistName.isBlank()) {
                stylistToUse = stylistName;
            } else if (existingService != null && existingService.getStylistName() != null) {
                stylistToUse = existingService.getStylistName();
            } else {
                stylistToUse = "Unassigned";
            }

            // Pass the assigned stylist to the factory method
            SalonService service = toService(finalServiceId, type, name, description, basePrice.doubleValue(), imageToUse, stylistToUse);

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
        return serviceFileManager.readAllServices().stream()
                .filter(service -> service.getServiceId() == serviceId)
                .findFirst().orElse(null);
    }

    // UPDATED: Factory method now accepts the assigned stylist name
    private SalonService toService(int serviceId, String type, String name, String description, double basePrice, String imageFileName, String stylistName) {
        if ("package".equalsIgnoreCase(type)) {
            return new PackageService(serviceId, name, description, basePrice, imageFileName, stylistName);
        }
        if ("standard".equalsIgnoreCase(type)) {
            return new StandardService(serviceId, name, description, basePrice, imageFileName, stylistName);
        }
        return null;
    }
}
