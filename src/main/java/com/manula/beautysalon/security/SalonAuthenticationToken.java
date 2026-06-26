package com.manula.beautysalon.security;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;

public class SalonAuthenticationToken extends UsernamePasswordAuthenticationToken {

    private final LoginContext loginContext;

    public SalonAuthenticationToken(String identifier, String password, LoginContext loginContext) {
        super(identifier, password);
        this.loginContext = loginContext;
    }

    public SalonAuthenticationToken(SalonUserPrincipal principal, LoginContext loginContext) {
        super(principal, null, principal.getAuthorities());
        this.loginContext = loginContext;
    }

    public LoginContext getLoginContext() {
        return loginContext;
    }

    public enum LoginContext {
        CUSTOMER,
        STAFF
    }
}
