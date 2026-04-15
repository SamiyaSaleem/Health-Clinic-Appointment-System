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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ResponseBody;
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
import com.example.demo.service.AppointmentService;
import com.example.demo.service.DoctorService;

@Controller
@RequestMapping("/patient")
public class PatientController {

    private final UserRepository userRepository;
    private final AppointmentRepository appointmentRepository;
    private final DoctorService doctorService;
    private final AppointmentService appointmentService;
    private final PasswordEncoder passwordEncoder;

    public PatientController(UserRepository userRepository, AppointmentRepository appointmentRepository, DoctorService doctorService, AppointmentService appointmentService, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.appointmentRepository = appointmentRepository;
        this.doctorService = doctorService;
        this.appointmentService = appointmentService;
        this.passwordEncoder = passwordEncoder;
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
            appointmentService.bookAppointment(patient, doctor, appointmentDate, appointmentTime, reason);

            return "redirect:/patient/appointments?booked=true";
        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/patient/appointments/new";
        }
    }

    @GetMapping("/appointments/slots")
    @ResponseBody
    public List<String> availableSlots(
            @RequestParam Long doctorId,
            @RequestParam LocalDate appointmentDate
    ) {
        Doctor doctor = doctorService.findById(doctorId).orElseThrow(() -> new IllegalArgumentException("Doctor not found"));
        return appointmentService.getAvailableSlotStrings(doctor, appointmentDate);
    }

    @GetMapping("/appointments")
    public String viewAppointments(Model model,
                                   Authentication authentication,
                                   @RequestParam(value = "tab", required = false) String tab,
                                   @RequestParam(value = "from", required = false) String from,
                                   @RequestParam(value = "to", required = false) String to,
                                   @RequestParam(value = "q", required = false) String q) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        model.addAttribute("user", user);

        LocalDate today = LocalDate.now();
        model.addAttribute("today", today);

        String activeTab = (tab == null || tab.trim().isEmpty()) ? "all" : tab.trim().toLowerCase();

        LocalDate fromDate = null;
        if (from != null && !from.trim().isEmpty()) {
            try {
                fromDate = LocalDate.parse(from.trim());
            } catch (Exception ignored) {
                fromDate = null;
            }
        }

        LocalDate toDate = null;
        if (to != null && !to.trim().isEmpty()) {
            try {
                toDate = LocalDate.parse(to.trim());
            } catch (Exception ignored) {
                toDate = null;
            }
        }

        String query = q != null ? q.trim().toLowerCase() : "";

        Pageable pageable = PageRequest.of(0, 500);
        List<Appointment> allAppointments = appointmentRepository.findByPatientOrderByAppointmentDateDesc(user, pageable);

        long countAll = allAppointments.size();
        long countPending = allAppointments.stream().filter(a -> a.getStatus() == AppointmentStatus.PENDING).count();
        long countCancelled = allAppointments.stream().filter(a -> a.getStatus() == AppointmentStatus.CANCELLED).count();
        long countUpcoming = allAppointments.stream()
                .filter(a -> (a.getStatus() == AppointmentStatus.PENDING || a.getStatus() == AppointmentStatus.CONFIRMED))
                .filter(a -> a.getAppointmentDate() != null && !a.getAppointmentDate().isBefore(today))
                .count();
        long countPast = allAppointments.stream()
                .filter(a -> {
                    if (a.getStatus() == AppointmentStatus.COMPLETED || a.getStatus() == AppointmentStatus.NO_SHOW) {
                        return true;
                    }
                    LocalDate d = a.getAppointmentDate();
                    if (d == null) {
                        return false;
                    }
                    return d.isBefore(today) && (a.getStatus() == AppointmentStatus.PENDING || a.getStatus() == AppointmentStatus.CONFIRMED);
                })
                .count();

        final String activeTabFinal = activeTab;
        final LocalDate todayFinal = today;
        final LocalDate fromDateFinal = fromDate;
        final LocalDate toDateFinal = toDate;
        final String queryFinal = query;

        List<Appointment> appointments = allAppointments.stream()
                .filter(a -> {
                    return switch (activeTabFinal) {
                        case "pending" -> a.getStatus() == AppointmentStatus.PENDING;
                        case "upcoming" -> (a.getStatus() == AppointmentStatus.PENDING || a.getStatus() == AppointmentStatus.CONFIRMED)
                                && a.getAppointmentDate() != null
                                && !a.getAppointmentDate().isBefore(todayFinal);
                        case "past" -> {
                            if (a.getStatus() == AppointmentStatus.COMPLETED || a.getStatus() == AppointmentStatus.NO_SHOW) {
                                yield true;
                            }
                            LocalDate d = a.getAppointmentDate();
                            yield d != null && d.isBefore(todayFinal) && (a.getStatus() == AppointmentStatus.PENDING || a.getStatus() == AppointmentStatus.CONFIRMED);
                        }
                        case "cancelled" -> a.getStatus() == AppointmentStatus.CANCELLED;
                        default -> true;
                    };
                })
                .filter(a -> {
                    if (fromDateFinal == null && toDateFinal == null) {
                        return true;
                    }
                    LocalDate d = a.getAppointmentDate();
                    if (d == null) {
                        return false;
                    }
                    if (fromDateFinal != null && d.isBefore(fromDateFinal)) {
                        return false;
                    }
                    if (toDateFinal != null && d.isAfter(toDateFinal)) {
                        return false;
                    }
                    return true;
                })
                .filter(a -> {
                    if (queryFinal.isEmpty()) {
                        return true;
                    }
                    String doctorName = a.getDoctor() != null && a.getDoctor().getUser() != null && a.getDoctor().getUser().getFullName() != null
                            ? a.getDoctor().getUser().getFullName().toLowerCase() : "";
                    String spec = a.getDoctor() != null && a.getDoctor().getSpecialization() != null
                            ? a.getDoctor().getSpecialization().toLowerCase() : "";
                    return doctorName.contains(queryFinal) || spec.contains(queryFinal);
                })
                .toList();

        model.addAttribute("appointments", appointments);
        model.addAttribute("activeTab", activeTab);
        model.addAttribute("countAll", countAll);
        model.addAttribute("countPending", countPending);
        model.addAttribute("countUpcoming", countUpcoming);
        model.addAttribute("countPast", countPast);
        model.addAttribute("countCancelled", countCancelled);
        model.addAttribute("filterFrom", fromDate);
        model.addAttribute("filterTo", toDate);
        model.addAttribute("filterQ", q != null ? q.trim() : "");

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

            if (appointment.getStatus() != AppointmentStatus.PENDING && appointment.getStatus() != AppointmentStatus.CONFIRMED) {
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
            @RequestParam String confirmPassword,
            Authentication authentication,
            RedirectAttributes attributes
    ) {
        try {
            String email = authentication.getName();
            User user = userRepository.findByEmail(email).orElseThrow();

            if (!passwordEncoder.matches(currentPassword, user.getPasswordHash())) {
                attributes.addFlashAttribute("error", "Current password is incorrect");
                return "redirect:/patient/profile";
            }
            if (!newPassword.equals(confirmPassword)) {
                attributes.addFlashAttribute("error", "New passwords do not match");
                return "redirect:/patient/profile";
            }
            if (newPassword.length() < 6) {
                attributes.addFlashAttribute("error", "New password must be at least 6 characters");
                return "redirect:/patient/profile";
            }

            user.setPasswordHash(passwordEncoder.encode(newPassword));
            userRepository.save(user);
            return "redirect:/patient/profile?passwordChanged=true";
        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/patient/profile";
        }
    }
}
