package com.manula.beautysalon.controller;

import com.manula.beautysalon.model.PackageService;
import com.manula.beautysalon.model.SalonService;
import com.manula.beautysalon.model.StandardService;
import com.manula.beautysalon.service.SalonServiceService;
import com.manula.beautysalon.service.StylistService;
import com.manula.beautysalon.util.SecurityUtils;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.List;

@Controller
public class ServiceWebController {

    private final SalonServiceService salonServiceService;
    private final StylistService stylistService;

    public ServiceWebController(SalonServiceService salonServiceService, StylistService stylistService) {
        this.salonServiceService = salonServiceService;
        this.stylistService = stylistService;
    }

    @GetMapping("/services")
    public String handleServicesGet(
            @RequestParam(defaultValue = "list") String action,
            @RequestParam(required = false) Integer serviceId,
            HttpSession session,
            Model model
    ) {
        switch (action.toLowerCase()) {
            case "new":
                if (!SecurityUtils.isManager(session)) {
                    return adminAccessRedirect();
                }
                model.addAttribute("generatedServiceId", salonServiceService.generateNextServiceId());
                model.addAttribute("stylists", stylistService.readActiveAvailableStylists());
                return "AddService";
            case "edit":
                if (!SecurityUtils.isManager(session)) {
                    return adminAccessRedirect();
                }
                if (serviceId == null) {
                    return "redirect:/services?action=list";
                }
                SalonService service = salonServiceService.findById(serviceId);
                if (service == null) {
                    return "redirect:/services?action=list";
                }
                model.addAttribute("service", service);
                model.addAttribute("typeValue", service instanceof PackageService ? "package" : "standard");
                model.addAttribute("stylists", stylistService.readActiveAvailableStylists());
                return "EditService";
            case "delete":
                if (!SecurityUtils.isManager(session)) {
                    return adminAccessRedirect();
                }
                return "redirect:/services?action=list";
            case "list":
            default:
                if (!SecurityUtils.isAdmin()) {
                    return adminAccessRedirect();
                }
                List<SalonService> services = salonServiceService.readAllServices();
                model.addAttribute("services", services);
                return "ServiceList";
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
            @RequestParam(required = false) String stylistName,
            HttpSession session
    ) {
        if (!SecurityUtils.isManager(session)) {
            return adminAccessRedirect();
        }

        if ("delete".equalsIgnoreCase(action)) {
            if (serviceId != null && serviceId > 0) {
                salonServiceService.deleteService(serviceId);
            }
            return "redirect:/services?action=list";
        }

        boolean isNew = "new".equalsIgnoreCase(action) || serviceId == null || serviceId == 0;
        if (type == null || name == null || description == null || basePrice == null) {
            return "redirect:/services?action=list";
        }

        SalonService existingService = isNew ? null : salonServiceService.findById(serviceId);

        // Preserve existing media/assignment values when the admin edits only the core service
        // details, so partial form submissions do not accidentally blank catalog cards.
        String imageToUse;
        if (imageFileName != null && !imageFileName.isBlank()) {
            imageToUse = imageFileName;
        } else if (existingService != null && existingService.getImageFileName() != null) {
            imageToUse = existingService.getImageFileName();
        } else {
            imageToUse = "default-service.png";
        }

        String stylistToUse;
        if (stylistName != null && !stylistName.isBlank()) {
            stylistToUse = stylistName;
        } else if (existingService != null && existingService.getStylistName() != null) {
            stylistToUse = existingService.getStylistName();
        } else {
            stylistToUse = "Unassigned";
        }

        int finalServiceId = isNew ? 0 : serviceId;
        SalonService service = toService(finalServiceId, type, name, description, basePrice, imageToUse, stylistToUse);

        if (service == null) {
            return "redirect:/services?action=list";
        }

        if (isNew) {
            salonServiceService.createService(service);
        } else {
            salonServiceService.updateService(service);
        }

        return "redirect:/services?action=list";
    }

    private String adminAccessRedirect() {
        return SecurityUtils.isAuthenticated() ? "redirect:/access-denied" : "redirect:/staff-login";
    }

    private SalonService toService(int serviceId, String type, String name, String description, double basePrice, String imageFileName, String stylistName) {
        // The form chooses the concrete service subtype; the model layer stores both subtypes
        // in one table using the discriminator documented on SalonService.
        if ("package".equalsIgnoreCase(type)) {
            return new PackageService(serviceId, name, description, basePrice, imageFileName, stylistName);
        }
        if ("standard".equalsIgnoreCase(type)) {
            return new StandardService(serviceId, name, description, basePrice, imageFileName, stylistName);
        }
        return null;
    }
}
