package com.example.demo.controller;

import com.example.demo.entity.Appointment;
import com.example.demo.entity.User;
import com.example.demo.repository.AppointmentRepository;
import com.example.demo.repository.SupportRequestRepository;
import com.example.demo.repository.UserRepository;
import com.example.demo.service.DoctorService;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;

import java.util.List;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final UserRepository userRepository;
    private final DoctorService doctorService;
    private final AppointmentRepository appointmentRepository;
    private final SupportRequestRepository supportRequestRepository;

    public AdminController(UserRepository userRepository,
                           DoctorService doctorService,
                           AppointmentRepository appointmentRepository,
                           SupportRequestRepository supportRequestRepository) {
        this.userRepository = userRepository;
        this.doctorService = doctorService;
        this.appointmentRepository = appointmentRepository;
        this.supportRequestRepository = supportRequestRepository;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("totalUsers", userRepository.count());
        model.addAttribute("pendingDoctorApprovals", doctorService.getPendingApprovals().size());
        model.addAttribute("totalAppointments", appointmentRepository.count());
        model.addAttribute("openSupportRequests", supportRequestRepository.countByResolved(false));

        List<Appointment> recentAppointments = appointmentRepository
                .findAll(PageRequest.of(0, 5, Sort.by(Sort.Order.desc("appointmentDate"), Sort.Order.desc("appointmentTime"))))
                .getContent();
        model.addAttribute("recentAppointments", recentAppointments);

        List<User> recentUsers = userRepository
                .findAll(PageRequest.of(0, 5, Sort.by(Sort.Order.desc("createdAt"))))
                .getContent();
        model.addAttribute("recentUsers", recentUsers);

        return "admin/dashboard";
    }
}
