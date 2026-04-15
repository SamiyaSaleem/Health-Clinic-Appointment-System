package com.example.demo.controller;

import com.example.demo.entity.Doctor;
import com.example.demo.service.DoctorService;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/doctors")
public class AdminDoctorController {

    private final DoctorService doctorService;

    public AdminDoctorController(DoctorService doctorService) {
        this.doctorService = doctorService;
    }

    @GetMapping
    public String doctors(Model model) {
        List<Doctor> pending = doctorService.getPendingApprovals();
        List<Doctor> all = doctorService.getAllDoctors();

        model.addAttribute("pending", pending);
        model.addAttribute("allDoctors", all);
        model.addAttribute("pendingCount", pending.size());
        model.addAttribute("totalCount", all.size());

        return "admin/doctors";
    }

    @GetMapping("/requests")
    public String doctorRequests(Model model) {
        List<Doctor> pending = doctorService.getPendingApprovals();
        model.addAttribute("pending", pending);
        model.addAttribute("pendingCount", pending.size());
        return "admin/doctor-requests";
    }

    @PostMapping("/{id}/approve")
    public String approve(@PathVariable Long id, RedirectAttributes attributes) {
        try {
            doctorService.approveDoctor(id);
            return "redirect:/admin/doctors?approved=true";
        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/doctors";
        }
    }

    @PostMapping("/{id}/toggle-active")
    public String toggleActive(@PathVariable Long id, RedirectAttributes attributes) {
        try {
            doctorService.toggleActive(id);
            return "redirect:/admin/doctors?updated=true";
        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/doctors";
        }
    }

    @PostMapping("/{id}/reject")
    public String reject(@PathVariable Long id, RedirectAttributes attributes) {
        try {
            doctorService.rejectDoctor(id);
            return "redirect:/admin/doctors?rejected=true";
        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/doctors";
        }
    }

    @PostMapping("/{id}/approve-from-requests")
    public String approveFromRequests(@PathVariable Long id, RedirectAttributes attributes) {
        try {
            doctorService.approveDoctor(id);
            attributes.addFlashAttribute("success", "Doctor approved successfully.");
            return "redirect:/admin/doctors/requests?approved=true";
        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/doctors/requests";
        }
    }

    @PostMapping("/{id}/reject-from-requests")
    public String rejectFromRequests(@PathVariable Long id, RedirectAttributes attributes) {
        try {
            doctorService.rejectDoctor(id);
            attributes.addFlashAttribute("success", "Doctor request rejected.");
            return "redirect:/admin/doctors/requests?rejected=true";
        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/doctors/requests";
        }
    }
}
