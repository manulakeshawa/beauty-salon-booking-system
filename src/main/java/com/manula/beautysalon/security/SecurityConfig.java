package com.manula.beautysalon.security;

import com.manula.beautysalon.service.PasswordService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.util.matcher.RequestMatcher;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    private final SalonAuthenticationProvider authenticationProvider;
    private final SalonAuthenticationEntryPoint authenticationEntryPoint;
    private final SalonAccessDeniedHandler accessDeniedHandler;

    public SecurityConfig(
            SalonAuthenticationProvider authenticationProvider,
            SalonAuthenticationEntryPoint authenticationEntryPoint,
            SalonAccessDeniedHandler accessDeniedHandler
    ) {
        this.authenticationProvider = authenticationProvider;
        this.authenticationEntryPoint = authenticationEntryPoint;
        this.accessDeniedHandler = accessDeniedHandler;
    }

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .authenticationProvider(authenticationProvider)
                .authorizeHttpRequests(authorize -> authorize
                        // Login, registration, password setup/reset, and static files must be reachable
                        // before a user has a session. The setup/reset pages are bearer-token flows, so
                        // protecting them with role checks would prevent users from using emailed links.
                        .requestMatchers(staticResources()).permitAll()
                        .requestMatchers("/", "/reviews", "/forgot-password", "/reset-password", "/password-setup").permitAll()
                        .requestMatchers(HttpMethod.GET, "/staff-login").permitAll()
                        .requestMatchers(HttpMethod.POST, "/staff-login").permitAll()
                        .requestMatchers(customerPublicActions()).permitAll()
                        .requestMatchers("/access-denied").authenticated()
                        // CUSTOMER, STYLIST, and ADMIN are separate authorities because each portal
                        // exposes different account data and operations.
                        .requestMatchers(customerActions()).hasRole("CUSTOMER")
                        .requestMatchers(
                                "/my-portal",
                                "/customer-profile",
                                "/public-review",
                                "/submitPublicReview",
                                "/editReview",
                                "/updateReview",
                                "/deleteReview",
                                "/receipt"
                        ).hasRole("CUSTOMER")
                        // Staff appointment actions can be performed by the assigned stylist workflow
                        // or by an administrator overseeing salon operations.
                        .requestMatchers(stylistAppointmentActions()).hasAnyRole("STYLIST", "ADMIN")
                        .requestMatchers("/stylist-portal", "/stylist-profile").hasRole("STYLIST")
                        .requestMatchers("/staff-logout").hasAnyRole("STYLIST", "ADMIN")
                        .requestMatchers("/admin", "/admin/**", "/admin-password").hasRole("ADMIN")
                        .requestMatchers("/customers", "/stylists", "/services", "/appointments").hasRole("ADMIN")
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form.disable())
                .httpBasic(basic -> basic.disable())
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/customers?action=login")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                )
                .exceptionHandling(exceptions -> exceptions
                        .authenticationEntryPoint(authenticationEntryPoint)
                        .accessDeniedHandler(accessDeniedHandler)
                );

        return http.build();
    }

    @Bean
    public AuthenticationManager authenticationManager() {
        return new ProviderManager(authenticationProvider);
    }

    @Bean
    public PasswordEncoder passwordEncoder(PasswordService passwordService) {
        // Spring Security delegates password comparisons to the same hashing service used by
        // registration, reset, and first-time setup so there is one password format to maintain.
        return new PasswordServicePasswordEncoder(passwordService);
    }

    private RequestMatcher staticResources() {
        return request -> {
            String path = request.getServletPath();
            return path.equals("/favicon.ico")
                    || path.equals("/style.css")
                    || path.equals("/storefront.css")
                    || path.equals("/lumiere-theme.css")
                    || path.startsWith("/images/")
                    || path.startsWith("/css/")
                    || path.startsWith("/js/")
                    || path.startsWith("/webjars/");
        };
    }

    private RequestMatcher customerPublicActions() {
        return request -> pathEquals(request, "/customers") && isAnyAction(request, "login", "public-register");
    }

    private RequestMatcher customerActions() {
        return request -> pathEquals(request, "/appointments") && isAnyAction(request, "public-book", "public-cancel")
                || pathEquals(request, "/customers") && isAnyAction(request, "change-password");
    }

    private RequestMatcher stylistAppointmentActions() {
        return request -> pathEquals(request, "/appointments") && isAnyAction(request, "stylist-checkin", "stylist-complete");
    }

    private boolean pathEquals(HttpServletRequest request, String path) {
        return path.equals(request.getServletPath());
    }

    private boolean isAnyAction(HttpServletRequest request, String... expectedActions) {
        String action = request.getParameter("action");
        if (action == null) {
            return false;
        }
        for (String expectedAction : expectedActions) {
            if (expectedAction.equalsIgnoreCase(action)) {
                return true;
            }
        }
        return false;
    }
}
