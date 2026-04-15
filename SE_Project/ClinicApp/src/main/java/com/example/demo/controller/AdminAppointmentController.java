package com.example.demo.controller;

import com.example.demo.entity.Appointment;
import com.example.demo.entity.AppointmentStatus;
import com.example.demo.repository.AppointmentRepository;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;

@Controller
@RequestMapping("/admin/appointments")
public class AdminAppointmentController {

    private final AppointmentRepository appointmentRepository;

    public AdminAppointmentController(AppointmentRepository appointmentRepository) {
        this.appointmentRepository = appointmentRepository;
    }

    @GetMapping
    public String appointments(@RequestParam(value = "status", required = false) String status,
                               @RequestParam(value = "from", required = false) String from,
                               @RequestParam(value = "to", required = false) String to,
                               Model model) {

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

        long countTotal = appointmentRepository.count();
        long countPending = appointmentRepository.countByStatus(AppointmentStatus.PENDING);
        long countConfirmed = appointmentRepository.countByStatus(AppointmentStatus.CONFIRMED);
        long countCompleted = appointmentRepository.countByStatus(AppointmentStatus.COMPLETED);
        long countCancelled = appointmentRepository.countByStatus(AppointmentStatus.CANCELLED);
        long countNoShow = appointmentRepository.countByStatus(AppointmentStatus.NO_SHOW);

        final AppointmentStatus filterStatusFinal = filterStatus;
        final LocalDate fromDateFinal = fromDate;
        final LocalDate toDateFinal = toDate;

        Pageable pageable = PageRequest.of(0, 1000);
        List<Appointment> page = appointmentRepository.findAll(pageable).getContent();

        List<Appointment> appointments = page.stream()
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

        return "admin/appointments";
    }
}
