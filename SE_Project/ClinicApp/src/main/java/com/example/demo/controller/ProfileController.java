package com.example.demo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * DEPRECATED: Profile endpoints are now handled by role-specific controllers:
 * - PatientController (/patient/profile)
 * - DoctorController (/doctor/profile)
 * - AdminController (/admin/users)
 */
@Controller
public class ProfileController {

    @GetMapping("/profile")
    public String profile(Authentication auth) {
        if (auth == null) {
            return "redirect:/login";
        }
        
        String role = auth.getAuthorities().stream()
            .findFirst()
            .map(a -> a.getAuthority().replace("ROLE_", ""))
            .orElse("");
        
        switch (role) {
            case "PATIENT":
                return "redirect:/patient/profile";
            case "DOCTOR":
                return "redirect:/doctor/profile";
            case "ADMIN":
                return "redirect:/admin/users";
            default:
                return "redirect:/login";
        }
    }
}