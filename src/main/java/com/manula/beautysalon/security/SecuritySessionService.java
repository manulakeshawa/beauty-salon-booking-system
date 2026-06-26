package com.manula.beautysalon.security;

import com.manula.beautysalon.model.Customer;
import com.manula.beautysalon.model.Employee;
import com.manula.beautysalon.model.Stylist;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.logout.SecurityContextLogoutHandler;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Service;

@Service
public class SecuritySessionService {

    private final AuthenticationManager authenticationManager;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();

    public SecuritySessionService(AuthenticationManager authenticationManager) {
        this.authenticationManager = authenticationManager;
    }

    public SalonUserPrincipal loginCustomer(
            String email,
            String password,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        Authentication authentication = authenticationManager.authenticate(
                new SalonAuthenticationToken(email, password, SalonAuthenticationToken.LoginContext.CUSTOMER)
        );
        saveAuthentication(authentication, request, response);
        return (SalonUserPrincipal) authentication.getPrincipal();
    }

    public SalonUserPrincipal loginStaff(
            String identifier,
            String password,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        Authentication authentication = authenticationManager.authenticate(
                new SalonAuthenticationToken(identifier, password, SalonAuthenticationToken.LoginContext.STAFF)
        );
        saveAuthentication(authentication, request, response);
        return (SalonUserPrincipal) authentication.getPrincipal();
    }

    public void refreshCustomer(Customer customer, HttpServletRequest request, HttpServletResponse response) {
        saveAuthentication(
                new SalonAuthenticationToken(SalonUserPrincipal.customer(customer), SalonAuthenticationToken.LoginContext.CUSTOMER),
                request,
                response
        );
    }

    public void refreshStylist(Stylist stylist, HttpServletRequest request, HttpServletResponse response) {
        saveAuthentication(
                new SalonAuthenticationToken(SalonUserPrincipal.stylist(stylist), SalonAuthenticationToken.LoginContext.STAFF),
                request,
                response
        );
    }

    public void refreshAdmin(Employee employee, HttpServletRequest request, HttpServletResponse response) {
        saveAuthentication(
                new SalonAuthenticationToken(SalonUserPrincipal.admin(employee), SalonAuthenticationToken.LoginContext.STAFF),
                request,
                response
        );
    }

    public SalonUserPrincipal currentPrincipal() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication != null && authentication.getPrincipal() instanceof SalonUserPrincipal principal) {
            return principal;
        }
        return null;
    }

    public void logout(HttpServletRequest request, HttpServletResponse response) {
        new SecurityContextLogoutHandler().logout(request, response, SecurityContextHolder.getContext().getAuthentication());
    }

    private void saveAuthentication(
            Authentication authentication,
            HttpServletRequest request,
            HttpServletResponse response
    ) {
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }
        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(authentication);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);
        applySessionAttributes((SalonUserPrincipal) authentication.getPrincipal(), request.getSession(true));
    }

    private void applySessionAttributes(SalonUserPrincipal principal, HttpSession session) {
        if (principal.isCustomer()) {
            clearStaffAttributes(session);
            session.setAttribute("loggedInCustomerEmail", principal.getEmail());
            session.setAttribute("loggedInCustomerName", principal.getDisplayName());
            return;
        }

        clearCustomerAttributes(session);
        session.setAttribute("staffRole", principal.getStaffRole());
        session.setAttribute("staffName", principal.getDisplayName());
        session.setAttribute("staffEmail", principal.getEmail());
        if (principal.isAdmin()) {
            session.setAttribute("staffUsername", principal.getUsername());
            session.removeAttribute("staffUserId");
        } else {
            session.setAttribute("staffUserId", principal.getUserId());
            session.removeAttribute("staffUsername");
        }
    }

    private void clearCustomerAttributes(HttpSession session) {
        session.removeAttribute("loggedInCustomerEmail");
        session.removeAttribute("loggedInCustomerName");
    }

    private void clearStaffAttributes(HttpSession session) {
        session.removeAttribute("staffRole");
        session.removeAttribute("staffName");
        session.removeAttribute("staffEmail");
        session.removeAttribute("staffUsername");
        session.removeAttribute("staffUserId");
    }
}
