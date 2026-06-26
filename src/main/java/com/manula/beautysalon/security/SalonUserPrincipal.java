package com.manula.beautysalon.security;

import com.manula.beautysalon.model.Customer;
import com.manula.beautysalon.model.Employee;
import com.manula.beautysalon.model.Stylist;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Collection;
import java.util.List;

public class SalonUserPrincipal implements UserDetails {

    private final int userId;
    private final SalonAccountType accountType;
    private final String username;
    private final String email;
    private final String displayName;
    private final String password;
    private final List<GrantedAuthority> authorities;
    private final boolean enabled;

    private SalonUserPrincipal(
            int userId,
            SalonAccountType accountType,
            String username,
            String email,
            String displayName,
            String password,
            boolean enabled
    ) {
        this.userId = userId;
        this.accountType = accountType;
        this.username = username;
        this.email = email;
        this.displayName = displayName;
        this.password = password;
        // Spring Security expects ROLE_ authorities; SecurityConfig maps routes to these
        // three account types instead of sharing one broad staff/customer permission.
        this.authorities = List.of(new SimpleGrantedAuthority("ROLE_" + accountType.name()));
        this.enabled = enabled;
    }

    public static SalonUserPrincipal customer(Customer customer) {
        return new SalonUserPrincipal(
                customer.getUserId(),
                SalonAccountType.CUSTOMER,
                customer.getEmail(),
                customer.getEmail(),
                customer.getName(),
                customer.getPassword(),
                customer.isPasswordSet()
        );
    }

    public static SalonUserPrincipal stylist(Stylist stylist) {
        return new SalonUserPrincipal(
                stylist.getUserId(),
                SalonAccountType.STYLIST,
                stylist.getEmail(),
                stylist.getEmail(),
                stylist.getName(),
                stylist.getPassword(),
                stylist.isActive() && stylist.isPasswordSet()
        );
    }

    public static SalonUserPrincipal admin(Employee employee) {
        return new SalonUserPrincipal(
                employee.getUserId(),
                SalonAccountType.ADMIN,
                employee.getUsername(),
                employee.getEmail(),
                employee.getFullName(),
                employee.getPassword(),
                employee.isPasswordSet()
        );
    }

    public int getUserId() {
        return userId;
    }

    public SalonAccountType getAccountType() {
        return accountType;
    }

    public String getEmail() {
        return email;
    }

    public String getDisplayName() {
        return displayName;
    }

    public boolean isCustomer() {
        return SalonAccountType.CUSTOMER.equals(accountType);
    }

    public boolean isStylist() {
        return SalonAccountType.STYLIST.equals(accountType);
    }

    public boolean isAdmin() {
        return SalonAccountType.ADMIN.equals(accountType);
    }

    public String getStaffRole() {
        if (isAdmin()) {
            return "MANAGER";
        }
        if (isStylist()) {
            return "STYLIST";
        }
        return null;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return enabled;
    }
}
