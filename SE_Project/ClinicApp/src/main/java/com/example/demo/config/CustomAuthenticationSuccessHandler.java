package com.example.demo.config;

import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;

@Component
public class CustomAuthenticationSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request, HttpServletResponse response,
            Authentication authentication) throws IOException, ServletException {
        boolean hasPatientRole = authentication.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_PATIENT"));
        boolean hasDoctorRole = authentication.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_DOCTOR"));
        boolean hasAdminRole = authentication.getAuthorities().stream()
            .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"));

        String redirectUrl = "/";
        
        if (hasPatientRole) {
            redirectUrl = "/patient/dashboard";
        } else if (hasDoctorRole) {
            redirectUrl = "/doctor/dashboard";
        } else if (hasAdminRole) {
            redirectUrl = "/admin/dashboard";
        }

        response.sendRedirect(redirectUrl);
    }
}
