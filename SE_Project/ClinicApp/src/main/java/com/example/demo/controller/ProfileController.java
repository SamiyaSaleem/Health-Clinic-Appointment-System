package com.example.demo.controller;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.Patient;
import com.example.demo.repository.PatientRepository;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;


@Controller
public class ProfileController {

    private final PatientRepository repo;

    public ProfileController(PatientRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/profile")
    public String profile(Authentication auth, Model model) {
        String email = auth.getName();
        Patient p = repo.findByEmail(email).orElseThrow();

        model.addAttribute("patient", p);
        return "profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(Authentication auth,
                                @RequestParam String phone,
                                @RequestParam String address) {

        String email = auth.getName();
        Patient p = repo.findByEmail(email).orElseThrow();

        p.setPhone(phone);
        p.setAddress(address);

        repo.save(p);

        return "redirect:/profile?success=true";
    }
    
    @GetMapping("/profile/change-password")
    public String changePasswordPage() {
        return "change-password";
    }

    @PostMapping("/profile/change-password")
    public String changePassword(Authentication auth,
                                 @RequestParam String oldPassword,
                                 @RequestParam String newPassword,
                                 Model model) {

        String email = auth.getName();
        Patient p = repo.findByEmail(email).orElseThrow();

        BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

        if (!encoder.matches(oldPassword, p.getPasswordHash())) {
            model.addAttribute("error", "Old password incorrect");
            return "change-password";
        }

        p.setPasswordHash(encoder.encode(newPassword));
        repo.save(p);

        return "redirect:/profile?passwordChanged=true";
    }
    
}