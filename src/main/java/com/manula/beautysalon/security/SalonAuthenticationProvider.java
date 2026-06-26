package com.manula.beautysalon.security;

import com.manula.beautysalon.model.Customer;
import com.manula.beautysalon.model.Employee;
import com.manula.beautysalon.model.Stylist;
import com.manula.beautysalon.service.CustomerService;
import com.manula.beautysalon.service.StaffService;
import com.manula.beautysalon.service.StylistService;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.DisabledException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.stereotype.Component;

@Component
public class SalonAuthenticationProvider implements AuthenticationProvider {

    private final CustomerService customerService;
    private final StaffService staffService;
    private final StylistService stylistService;

    public SalonAuthenticationProvider(CustomerService customerService, StaffService staffService, StylistService stylistService) {
        this.customerService = customerService;
        this.staffService = staffService;
        this.stylistService = stylistService;
    }

    @Override
    public Authentication authenticate(Authentication authentication) throws AuthenticationException {
        SalonAuthenticationToken token = (SalonAuthenticationToken) authentication;
        String identifier = stringValue(token.getPrincipal());
        String password = stringValue(token.getCredentials());

        if (!hasText(identifier) || !hasText(password)) {
            throw new BadCredentialsException("Invalid credentials.");
        }

        if (SalonAuthenticationToken.LoginContext.CUSTOMER.equals(token.getLoginContext())) {
            return authenticateCustomer(identifier, password);
        }

        return authenticateStaff(identifier, password);
    }

    @Override
    public boolean supports(Class<?> authentication) {
        return SalonAuthenticationToken.class.isAssignableFrom(authentication);
    }

    private Authentication authenticateCustomer(String email, String password) {
        if (customerService.isPasswordSetupPending(email)) {
            throw new DisabledException(CustomerService.PASSWORD_SETUP_PENDING_LOGIN_MESSAGE);
        }

        Customer customer = customerService.authenticateCustomer(email, password);
        if (customer == null) {
            throw new BadCredentialsException("Invalid email or password.");
        }

        return new SalonAuthenticationToken(
                SalonUserPrincipal.customer(customer),
                SalonAuthenticationToken.LoginContext.CUSTOMER
        );
    }

    private Authentication authenticateStaff(String identifier, String password) {
        Employee employee = staffService.authenticateEmployee(identifier, password);
        if (employee != null && "MANAGER".equalsIgnoreCase(employee.getRole())) {
            return new SalonAuthenticationToken(
                    SalonUserPrincipal.admin(employee),
                    SalonAuthenticationToken.LoginContext.STAFF
            );
        }

        Stylist stylist = staffService.authenticateStylist(identifier, password);
        if (stylist != null) {
            return new SalonAuthenticationToken(
                    SalonUserPrincipal.stylist(stylist),
                    SalonAuthenticationToken.LoginContext.STAFF
            );
        }

        if (stylistService.isPasswordSetupPending(identifier)) {
            throw new DisabledException(StylistService.PASSWORD_SETUP_PENDING_LOGIN_MESSAGE);
        }

        throw new BadCredentialsException("Invalid credentials. Access denied.");
    }

    private String stringValue(Object value) {
        return value == null ? null : value.toString();
    }

    private boolean hasText(String value) {
        return value != null && !value.isBlank();
    }
}
