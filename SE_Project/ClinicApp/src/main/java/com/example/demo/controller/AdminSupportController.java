package com.example.demo.controller;

import com.example.demo.entity.SupportRequest;
import com.example.demo.repository.SupportRequestRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/support")
public class AdminSupportController {

    private final SupportRequestRepository supportRequestRepository;

    public AdminSupportController(SupportRequestRepository supportRequestRepository) {
        this.supportRequestRepository = supportRequestRepository;
    }

    @GetMapping
    public String supportRequests(Model model) {
        List<SupportRequest> requests = supportRequestRepository.findAllByOrderByCreatedAtDesc();
        model.addAttribute("requests", requests);
        model.addAttribute("openCount", supportRequestRepository.countByResolved(false));
        model.addAttribute("resolvedCount", supportRequestRepository.countByResolved(true));
        return "admin/support";
    }

    @PostMapping("/{id}/resolve")
    public String resolve(@PathVariable Long id, RedirectAttributes attributes) {
        try {
            SupportRequest req = supportRequestRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("Support request not found"));
            req.setResolved(true);
            supportRequestRepository.save(req);
            return "redirect:/admin/support?resolved=true";
        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/support";
        }
    }
}
