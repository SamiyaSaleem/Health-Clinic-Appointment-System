package com.example.demo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Controller
public class DashboardController {

    @GetMapping("/")
    public String home(Authentication auth) {
        if (auth == null) {
            return "redirect:/login";
        }
        
        // Redirect based on user role
        String role = auth.getAuthorities().stream()
            .findFirst()
            .map(a -> a.getAuthority().replace("ROLE_", ""))
            .orElse("");
        
        switch (role) {
            case "PATIENT":
                return "redirect:/patient/dashboard";
            case "DOCTOR":
                return "redirect:/doctor/dashboard";
            case "ADMIN":
                return "redirect:/admin/dashboard";
            default:
                return "redirect:/login";
        }
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication auth) {
        return home(auth);
    }
}