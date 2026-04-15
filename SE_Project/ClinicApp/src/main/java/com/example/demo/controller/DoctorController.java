package com.example.demo.controller;

import com.example.demo.entity.Appointment;
import com.example.demo.entity.AppointmentStatus;
import com.example.demo.entity.Doctor;
import com.example.demo.entity.User;
import com.example.demo.repository.AppointmentRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.AppointmentService;
import com.example.demo.service.DoctorService;
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

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/doctor")
public class DoctorController {

    private final UserRepository userRepository;
    private final DoctorService doctorService;
    private final AppointmentRepository appointmentRepository;
    private final AppointmentService appointmentService;

    public DoctorController(UserRepository userRepository,
                            DoctorService doctorService,
                            AppointmentRepository appointmentRepository,
                            AppointmentService appointmentService) {
        this.userRepository = userRepository;
        this.doctorService = doctorService;
        this.appointmentRepository = appointmentRepository;
        this.appointmentService = appointmentService;
    }

    private Doctor currentDoctor(Authentication authentication) {
        String email = authentication.getName();
        User user = userRepository.findByEmail(email).orElseThrow();
        return doctorService.findByUserId(user.getId()).orElseThrow(() -> new IllegalStateException("Doctor profile not found"));
    }

    private String safeReturnTo(String returnTo, String fallback) {
        if (returnTo == null) {
            return fallback;
        }
        String trimmed = returnTo.trim();
        if (trimmed.isEmpty()) {
            return fallback;
        }
        if (!trimmed.startsWith("/doctor/")) {
            return fallback;
        }
        if (trimmed.contains("://") || trimmed.contains("\\") || trimmed.contains("\r") || trimmed.contains("\n")) {
            return fallback;
        }
        return trimmed;
    }

    private String withFlag(String path, String flag) {
        if (path.contains("?")) {
            return path + "&" + flag + "=true";
        }
        return path + "?" + flag + "=true";
    }

    @GetMapping("/dashboard")
    public String dashboard(Authentication authentication, Model model) {
        Doctor doctor = currentDoctor(authentication);
        model.addAttribute("doctor", doctor);

        long totalAppointments = appointmentRepository.countByDoctor(doctor);
        long pendingAppointments = appointmentRepository.countByDoctorAndStatus(doctor, AppointmentStatus.PENDING);
        long confirmedAppointments = appointmentRepository.countByDoctorAndStatus(doctor, AppointmentStatus.CONFIRMED);

        model.addAttribute("totalAppointments", totalAppointments);
        model.addAttribute("pendingAppointments", pendingAppointments);
        model.addAttribute("confirmedAppointments", confirmedAppointments);

        Pageable pageable = PageRequest.of(0, 200);
        List<Appointment> recent = appointmentRepository.findByDoctorOrderByAppointmentDateDesc(doctor, pageable);
        recent.sort(Comparator
                .comparing(Appointment::getAppointmentDate, Comparator.nullsLast(Comparator.naturalOrder()))
                .thenComparing(Appointment::getAppointmentTime, Comparator.nullsLast(Comparator.naturalOrder())));

        List<Appointment> pendingList = recent.stream().filter(a -> a.getStatus() == AppointmentStatus.PENDING).toList();
        List<Appointment> upcomingConfirmed = recent.stream()
                .filter(a -> a.getStatus() == AppointmentStatus.CONFIRMED)
                .filter(a -> a.getAppointmentDate() != null && !a.getAppointmentDate().isBefore(LocalDate.now()))
                .toList();

        model.addAttribute("pendingList", pendingList);
        model.addAttribute("upcomingConfirmed", upcomingConfirmed);

        return "doctor/dashboard";
    }

    @GetMapping("/appointments")
    public String appointments(Authentication authentication,
                               @RequestParam(value = "status", required = false) String status,
                               @RequestParam(value = "from", required = false) String from,
                               @RequestParam(value = "to", required = false) String to,
                               @RequestParam(value = "q", required = false) String q,
                               Model model) {
        Doctor doctor = currentDoctor(authentication);
        model.addAttribute("doctor", doctor);

        LocalDate today = LocalDate.now();
        model.addAttribute("today", today);

        AppointmentStatus filterStatus = null;
        if (status != null && !status.trim().isEmpty()) {
            try {
                filterStatus = AppointmentStatus.valueOf(status.trim().toUpperCase());
            } catch (Exception ignored) {
                filterStatus = null;
            }
        }

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
        List<Appointment> allAppointments = appointmentRepository.findByDoctorOrderByAppointmentDateDesc(doctor, pageable);

        long countTotal = allAppointments.size();
        long countPending = allAppointments.stream().filter(a -> a.getStatus() == AppointmentStatus.PENDING).count();
        long countConfirmed = allAppointments.stream().filter(a -> a.getStatus() == AppointmentStatus.CONFIRMED).count();
        long countCompleted = allAppointments.stream().filter(a -> a.getStatus() == AppointmentStatus.COMPLETED).count();
        long countCancelled = allAppointments.stream().filter(a -> a.getStatus() == AppointmentStatus.CANCELLED).count();
        long countNoShow = allAppointments.stream().filter(a -> a.getStatus() == AppointmentStatus.NO_SHOW).count();

        final AppointmentStatus filterStatusFinal = filterStatus;
        final LocalDate fromDateFinal = fromDate;
        final LocalDate toDateFinal = toDate;
        final String queryFinal = query;

        List<Appointment> appointments = allAppointments.stream()
                .filter(a -> filterStatusFinal == null || a.getStatus() == filterStatusFinal)
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
                    String patientName = a.getPatient() != null && a.getPatient().getFullName() != null ? a.getPatient().getFullName().toLowerCase() : "";
                    String patientEmail = a.getPatient() != null && a.getPatient().getEmail() != null ? a.getPatient().getEmail().toLowerCase() : "";
                    return patientName.contains(queryFinal) || patientEmail.contains(queryFinal);
                })
                .sorted(Comparator
                        .comparing(Appointment::getAppointmentDate, Comparator.nullsLast(Comparator.naturalOrder()))
                        .thenComparing(Appointment::getAppointmentTime, Comparator.nullsLast(Comparator.naturalOrder())))
                .toList();

        model.addAttribute("appointments", appointments);

        model.addAttribute("countTotal", countTotal);
        model.addAttribute("countPending", countPending);
        model.addAttribute("countConfirmed", countConfirmed);
        model.addAttribute("countCompleted", countCompleted);
        model.addAttribute("countCancelled", countCancelled);
        model.addAttribute("countNoShow", countNoShow);

        model.addAttribute("filterStatus", filterStatus != null ? filterStatus.name() : null);
        model.addAttribute("filterFrom", fromDate);
        model.addAttribute("filterTo", toDate);
        model.addAttribute("filterQ", q != null ? q.trim() : "");

        return "doctor/appointments";
    }

    @PostMapping("/appointments/{id}/approve")
    public String approve(@PathVariable Long id,
                          @RequestParam(value = "returnTo", required = false) String returnTo,
                          Authentication authentication,
                          RedirectAttributes attributes) {
        String target = safeReturnTo(returnTo, "/doctor/dashboard");
        try {
            Doctor doctor = currentDoctor(authentication);
            appointmentService.approveAppointmentByDoctor(doctor, id);
            return "redirect:" + withFlag(target, "approved");
        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return "redirect:" + target;
        }
    }

    @PostMapping("/appointments/{id}/reject")
    public String reject(@PathVariable Long id,
                         @RequestParam("reason") String reason,
                         @RequestParam(value = "returnTo", required = false) String returnTo,
                         Authentication authentication,
                         RedirectAttributes attributes) {
        String target = safeReturnTo(returnTo, "/doctor/dashboard");
        try {
            Doctor doctor = currentDoctor(authentication);
            appointmentService.rejectAppointmentByDoctor(doctor, id, reason);
            return "redirect:" + withFlag(target, "rejected");
        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return "redirect:" + target;
        }
    }

    @PostMapping("/appointments/{id}/complete")
    public String complete(@PathVariable Long id,
                           @RequestParam(value = "notes", required = false) String notes,
                           @RequestParam(value = "returnTo", required = false) String returnTo,
                           Authentication authentication,
                           RedirectAttributes attributes) {
        String target = safeReturnTo(returnTo, "/doctor/appointments");
        try {
            Doctor doctor = currentDoctor(authentication);
            appointmentService.completeAppointmentByDoctor(doctor, id, notes);
            return "redirect:" + withFlag(target, "completed");
        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return "redirect:" + target;
        }
    }

    @PostMapping("/appointments/{id}/no-show")
    public String noShow(@PathVariable Long id,
                         @RequestParam(value = "returnTo", required = false) String returnTo,
                         Authentication authentication,
                         RedirectAttributes attributes) {
        String target = safeReturnTo(returnTo, "/doctor/appointments");
        try {
            Doctor doctor = currentDoctor(authentication);
            appointmentService.markNoShowByDoctor(doctor, id);
            return "redirect:" + withFlag(target, "noShow");
        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return "redirect:" + target;
        }
    }

    @PostMapping("/appointments/{id}/cancel")
    public String cancel(@PathVariable Long id,
                         @RequestParam("reason") String reason,
                         @RequestParam(value = "returnTo", required = false) String returnTo,
                         Authentication authentication,
                         RedirectAttributes attributes) {
        String target = safeReturnTo(returnTo, "/doctor/appointments");
        try {
            Doctor doctor = currentDoctor(authentication);
            appointmentService.cancelAppointmentByDoctor(doctor, id, reason);
            return "redirect:" + withFlag(target, "cancelled");
        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return "redirect:" + target;
        }
    }

    @GetMapping("/profile")
    public String profilePage(Model model, Authentication authentication) {
        Doctor doctor = currentDoctor(authentication);
        model.addAttribute("doctor", doctor);
        return "doctor/profile";
    }

    @PostMapping("/profile/update")
    public String updateProfile(@RequestParam String phone,
                                @RequestParam String address,
                                Authentication authentication,
                                RedirectAttributes attributes) {
        try {
            Doctor doctor = currentDoctor(authentication);
            User user = doctor.getUser();
            user.setPhone(phone);
            user.setAddress(address);
            userRepository.save(user);
            return "redirect:/doctor/profile?updated=true";
        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/doctor/profile";
        }
    }

    @PostMapping("/profile/professional")
    public String updateProfessional(@RequestParam(required = false) String specialization,
                                     @RequestParam(required = false) String qualifications,
                                     @RequestParam(required = false) Integer experienceYears,
                                     @RequestParam(required = false) String availableDays,
                                     @RequestParam(required = false, name = "availableTimeStart") String availableTimeStartStr,
                                     @RequestParam(required = false, name = "availableTimeEnd") String availableTimeEndStr,
                                     @RequestParam(required = false) Integer appointmentDurationMinutes,
                                     Authentication authentication,
                                     RedirectAttributes attributes) {
        try {
            Doctor doctor = currentDoctor(authentication);

            doctor.setSpecialization(specialization != null ? specialization.trim() : null);
            doctor.setQualifications(qualifications != null ? qualifications.trim() : null);
            doctor.setExperienceYears(experienceYears);
            doctor.setAvailableDays(availableDays != null ? availableDays.trim() : null);

            if (availableTimeStartStr != null && !availableTimeStartStr.trim().isEmpty()) {
                doctor.setAvailableTimeStart(LocalTime.parse(availableTimeStartStr.trim()));
            } else {
                doctor.setAvailableTimeStart(null);
            }

            if (availableTimeEndStr != null && !availableTimeEndStr.trim().isEmpty()) {
                doctor.setAvailableTimeEnd(LocalTime.parse(availableTimeEndStr.trim()));
            } else {
                doctor.setAvailableTimeEnd(null);
            }

            if (appointmentDurationMinutes != null) {
                if (appointmentDurationMinutes <= 0) {
                    throw new IllegalArgumentException("Slot duration must be a positive number");
                }
                doctor.setAppointmentDurationMinutes(appointmentDurationMinutes);
            }

            doctorService.updateDoctor(doctor);
            return "redirect:/doctor/profile?updated=true";
        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/doctor/profile";
        }
    }
}
