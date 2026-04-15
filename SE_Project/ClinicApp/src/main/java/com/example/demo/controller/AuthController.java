package com.example.demo.controller;

import java.time.LocalDate;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.User;
import com.example.demo.entity.Doctor;
import com.example.demo.entity.Role;
import com.example.demo.service.UserService;
import com.example.demo.service.DoctorService;

@Controller
public class AuthController {

    private final UserService userService;
    private final DoctorService doctorService;
    private final BCryptPasswordEncoder encoder = new BCryptPasswordEncoder();

    public AuthController(UserService userService, DoctorService doctorService) {
        this.userService = userService;
        this.doctorService = doctorService;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
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
            @RequestParam String role,
            @RequestParam String password,
            @RequestParam String confirmPassword,
            @RequestParam(required = false) String specialization,
            @RequestParam(required = false) String qualifications,
            @RequestParam(required = false) Integer experienceYears,
            @RequestParam(required = false) String availableTimeStart,
            @RequestParam(required = false) String availableTimeEnd,
            @RequestParam(required = false) Integer appointmentDuration,
            RedirectAttributes attributes
    ) {
        try {
            // Validate password match
            if (!password.equals(confirmPassword)) {
                attributes.addFlashAttribute("error", "Passwords do not match");
                return "redirect:/register";
            }

            // Check if email already exists
            if (userService.findByEmail(email).isPresent()) {
                attributes.addFlashAttribute("error", "Email already registered");
                return "redirect:/register";
            }

            // Handle PATIENT registration
            if ("PATIENT".equals(role)) {
                userService.registerPatient(fullName, email, phone, address, dateOfBirth, password);
                attributes.addFlashAttribute("success", "Account created successfully. Please log in.");
                return "redirect:/login";
            }
            // Handle DOCTOR registration
            else if ("DOCTOR".equals(role)) {
                User doctorUser = new User();
                doctorUser.setFullName(fullName);
                doctorUser.setEmail(email);
                doctorUser.setPhone(phone);
                doctorUser.setAddress(address);
                doctorUser.setDateOfBirth(dateOfBirth);
                doctorUser.setPasswordHash(encoder.encode(password));
                doctorUser.setRole(Role.DOCTOR);
                doctorUser.setEnabled(true);

                User savedUser = userService.save(doctorUser);

                // Create doctor profile (pending approval)
                Doctor doctor = new Doctor();
                doctor.setUser(savedUser);
                doctor.setSpecialization(specialization);
                doctor.setQualifications(qualifications);
                doctor.setExperienceYears(experienceYears);
                doctor.setApproved(false);
                doctor.setActive(false);
                
                if (availableTimeStart != null) {
                    doctor.setAvailableTimeStart(java.time.LocalTime.parse(availableTimeStart));
                }
                if (availableTimeEnd != null) {
                    doctor.setAvailableTimeEnd(java.time.LocalTime.parse(availableTimeEnd));
                }
                if (appointmentDuration != null) {
                    doctor.setAppointmentDurationMinutes(appointmentDuration);
                }

                doctorService.createDoctor(doctor);
                attributes.addFlashAttribute("success", "Application submitted successfully. Please wait for admin approval.");
                return "redirect:/login";
            }

            attributes.addFlashAttribute("error", "Invalid account type selected");
            return "redirect:/register";

        } catch (Exception e) {
            e.printStackTrace();
            attributes.addFlashAttribute("error", "Registration failed: " + e.getMessage());
            return "redirect:/register";
        }
    }

}