package com.manula.beautysalon.util;


import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {
    public static boolean isManager(HttpSession session) {
        return hasRole("ADMIN");
    }

    public static boolean isAdmin() {
        return hasRole("ADMIN");
    }

    public static boolean isStylist() {
        return hasRole("STYLIST");
    }

    public static boolean isCustomer() {
        return hasRole("CUSTOMER");
    }

    public static boolean isStaff() {
        return isAdmin() || isStylist();
    }

    public static boolean isAuthenticated() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        return authentication != null
                && authentication.isAuthenticated()
                && !"anonymousUser".equals(authentication.getPrincipal());
    }

    public static boolean hasRole(String role) {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }
        String authorityName = "ROLE_" + role;
        return authentication.getAuthorities().stream()
                .anyMatch(authority -> authorityName.equals(authority.getAuthority()));
    }
}
