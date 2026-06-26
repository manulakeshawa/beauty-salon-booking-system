package com.manula.beautysalon.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class SalonAuthenticationEntryPoint implements AuthenticationEntryPoint {

    @Override
    public void commence(
            HttpServletRequest request,
            HttpServletResponse response,
            AuthenticationException authException
    ) throws IOException, ServletException {
        response.sendRedirect(loginPathFor(request));
    }

    private String loginPathFor(HttpServletRequest request) {
        String path = request.getServletPath();
        String action = request.getParameter("action");

        if (isCustomerPath(path, action)) {
            return request.getContextPath() + "/customers?action=login";
        }

        return request.getContextPath() + "/staff-login";
    }

    private boolean isCustomerPath(String path, String action) {
        return "/my-portal".equals(path)
                || "/customer-profile".equals(path)
                || "/public-review".equals(path)
                || "/submitPublicReview".equals(path)
                || "/editReview".equals(path)
                || "/updateReview".equals(path)
                || "/deleteReview".equals(path)
                || "/receipt".equals(path)
                || ("/appointments".equals(path) && isAnyAction(action, "public-book", "public-cancel"))
                || ("/customers".equals(path) && isAnyAction(action, "change-password"));
    }

    private boolean isAnyAction(String actual, String... expected) {
        if (actual == null) {
            return false;
        }
        for (String value : expected) {
            if (value.equalsIgnoreCase(actual)) {
                return true;
            }
        }
        return false;
    }
}
