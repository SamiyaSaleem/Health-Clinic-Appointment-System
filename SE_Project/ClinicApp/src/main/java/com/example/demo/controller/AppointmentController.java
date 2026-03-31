package com.example.demo.controller;

import com.example.demo.entity.Appointment;
import com.example.demo.repository.AppointmentRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

/**
 * DEPRECATED: Appointment endpoints are now handled by role-specific controllers:
 * - PatientController (/patient/appointments/*)
 * - DoctorController (/doctor/appointments/*)
 * - AdminController (/admin/appointments/*)
 * 
 * This controller is kept for backward compatibility only.
 */
@Controller
@RequestMapping("/appointments")
public class AppointmentController {

    private final AppointmentRepository repo;

    public AppointmentController(AppointmentRepository repo) {
        this.repo = repo;
    }

    @GetMapping("/list")
    public String viewAppointments(Model model) {
        model.addAttribute("appointments", repo.findAll());
        return "appointments-list";
    }
}