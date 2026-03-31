package com.example.demo.controller;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import com.example.demo.entity.Appointment;
import com.example.demo.entity.CancelledBy;
import com.example.demo.entity.Doctor;
import com.example.demo.entity.AppointmentStatus;
import com.example.demo.entity.User;
import com.example.demo.repository.AppointmentRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.DoctorService;

@Controller
@RequestMapping("/patient")
public class PatientController {

    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorService doctorService;

    public PatientController(UserRepository userRepository, AppointmentRepository appointmentRepository, DoctorService doctorService) {
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
        this.doctorService = doctorService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        // Get current logged-in user
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();

        // Add user to model
        model.addAttribute("user", user);

        // Calculate appointment statistics
        long totalAppointments = appointmentRepository.countByPatient(user);
        long upcomingAppointments = appointmentRepository.countByPatientAndAppointmentDateGreaterThanEqualAndStatus(user, LocalDate.now(), AppointmentStatus.CONFIRMED);
        long pendingAppointments = appointmentRepository.countByPatientAndStatus(user, AppointmentStatus.PENDING);
        long completedAppointments = appointmentRepository.countByPatientAndStatus(user, AppointmentStatus.COMPLETED);

        // Add statistics to model
        model.addAttribute("totalAppointments", totalAppointments);
        model.addAttribute("upcomingAppointments", upcomingAppointments);
        model.addAttribute("pendingAppointments", pendingAppointments);
        model.addAttribute("completedAppointments", completedAppointments);

        // Get recent appointments (last 5, sorted by date descending)
        Pageable pageable = PageRequest.of(0, 5);
        List<Appointment> recentAppointments = appointmentRepository.findByPatientOrderByAppointmentDateDesc(user, pageable);
        model.addAttribute("recentAppointments", recentAppointments);

        return "patient/dashboard";
    }

    @GetMapping("/appointments/new")
    public String bookAppointmentPage(Model model) {
        model.addAttribute("minDate", LocalDate.now());

        List<Doctor> doctors = doctorService.getAllApprovedAndActive();
        if (doctors.isEmpty()) {
            doctors = doctorService.getActiveDoctors();
        }

        doctors.sort(Comparator.comparing(d -> {
            String name = d.getUser() != null ? d.getUser().getFullName() : "";
            return name == null ? "" : name;
        }));
        model.addAttribute("doctors", doctors);

        List<String> specializations = new ArrayList<>();
        for (Doctor doctor : doctors) {
            String spec = doctor.getSpecialization();
            if (spec == null) continue;
            String normalized = spec.trim();
            if (normalized.isEmpty()) continue;
            if (!specializations.contains(normalized)) {
                specializations.add(normalized);
            }
        }
        specializations.sort(String.CASE_INSENSITIVE_ORDER);
        model.addAttribute("specializations", specializations);

        return "patient/book-appointment";
    }

    @PostMapping("/appointments/new")
    public String bookAppointment(
            @RequestParam Long doctorId,
            @RequestParam LocalDate appointmentDate,
            @RequestParam LocalTime appointmentTime,
            @RequestParam String reason,
            Authentication authentication,
            RedirectAttributes attributes
    ) {
        try {
            String email = authentication.getName();
            User patient = userRepository.findByEmail(email).orElseThrow();

            Doctor doctor = doctorService.findById(doctorId).orElseThrow(() -> new IllegalArgumentException("Doctor not found"));
            if (!doctor.isActive()) {
                throw new IllegalStateException("Doctor is not active");
            }

            if (appointmentDate.isBefore(LocalDate.now())) {
                throw new IllegalArgumentException("Appointment date cannot be in the past");
            }

            Appointment appointment = new Appointment(patient, doctor, appointmentDate, appointmentTime, reason);
            appointment.setStatus(AppointmentStatus.PENDING);
            appointment.setUpdatedAt(LocalDateTime.now());
            appointmentRepository.save(appointment);

            return "redirect:/patient/appointments?booked=true";
        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/patient/appointments/new";
        }
    }

    @GetMapping("/appointments")
    public String viewAppointments(Model model, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        model.addAttribute("user", user);
        model.addAttribute("today", LocalDate.now());

        Pageable pageable = PageRequest.of(0, 100);
        List<Appointment> appointments = appointmentRepository.findByPatientOrderByAppointmentDateDesc(user, pageable);
        model.addAttribute("appointments", appointments);

        return "patient/appointments";
    }

    @PostMapping("/appointments/{id}/cancel")
    public String cancelAppointment(@PathVariable Long id, Authentication authentication, RedirectAttributes attributes) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email).orElseThrow();

            Appointment appointment = appointmentRepository.findById(id).orElseThrow(() -> new IllegalArgumentException("Appointment not found"));
            if (appointment.getPatient() == null || !Objects.equals(appointment.getPatient().getId(), user.getId())) {
                throw new IllegalStateException("Not allowed");
            }

            if (appointment.getStatus() == AppointmentStatus.CANCELLED || appointment.getStatus() == AppointmentStatus.COMPLETED) {
                throw new IllegalStateException("Appointment cannot be cancelled");
            }

            appointment.setStatus(AppointmentStatus.CANCELLED);
            appointment.setCancelledBy(CancelledBy.PATIENT);
            appointment.setUpdatedAt(LocalDateTime.now());
            appointmentRepository.save(appointment);

            return "redirect:/patient/appointments?cancelled=true";
        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/patient/appointments";
        }
    }

    @GetMapping("/profile")
    public String profilePage(Model model, Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        model.addAttribute("user", user);
        return "patient/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(
            @RequestParam String phone,
            @RequestParam String address,
            Authentication authentication
    ) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        user.setPhone(phone);
        user.setAddress(address);
        userRepository.save(user);
        return "redirect:/patient/profile?updated=true";
    }

    @PostMapping("/change-password")
    public String changePassword(
            @RequestParam String currentPassword,
            @RequestParam String newPassword,
            @RequestParam String confirmPassword
    ) {
        return "redirect:/patient/profile?passwordChanged=true";
    }
}
