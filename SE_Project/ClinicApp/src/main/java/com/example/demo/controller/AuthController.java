package com.example.demo.controller;

import java.time.LocalDate;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;

import com.example.demo.entity.Patient;
import com.example.demo.repository.PatientRepository;

@Controller
public class AuthController {

    private final PatientRepository repo;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthController(PatientRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/register")
    public String registerPage() {
        return "register";
    }

    @PostMapping("/register")
    public String register(
            @RequestParam String fullName,
            @RequestParam String email,
            @RequestParam String phone,
            @RequestParam LocalDate dateOfBirth,
            @RequestParam String address,
            @RequestParam String password
    ) {
        if (repo.existsByEmail(email)) {
            return "redirect:/register?error=email";
        }

        Patient p = new Patient();
        p.setFullName(fullName);
        p.setEmail(email);
        p.setPhone(phone);
        p.setDateOfBirth(dateOfBirth);
        p.setAddress(address);
        p.setPasswordHash(encoder.encode(password));

        repo.save(p);
        return "redirect:/login";
    }
    
    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }
    
}