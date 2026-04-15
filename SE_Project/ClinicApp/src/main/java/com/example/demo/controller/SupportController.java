package com.example.demo.controller;

import com.example.demo.entity.SupportRequest;
import com.example.demo.entity.User;
import com.example.demo.repository.SupportRequestRepository;
import com.example.demo.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class SupportController {

    private final UserRepository userRepository;
    private final SupportRequestRepository supportRequestRepository;

    public SupportController(UserRepository userRepository, SupportRequestRepository supportRequestRepository) {
        this.userRepository = userRepository;
        this.supportRequestRepository = supportRequestRepository;
    }

    @GetMapping("/support")
    public String supportPage(Model model) {
        model.addAttribute("subject", "");
        model.addAttribute("message", "");
        return "support";
    }

    @PostMapping("/support")
    public String submitSupportRequest(@RequestParam String subject,
                                       @RequestParam String message,
                                       Authentication authentication,
                                       RedirectAttributes attributes) {
        try {
            if (authentication == null) {
                return "redirect:/login";
            }

            String s = subject == null ? "" : subject.trim();
            String m = message == null ? "" : message.trim();

            if (s.isEmpty()) {
                throw new IllegalArgumentException("Subject is required");
            }
            if (m.isEmpty()) {
                throw new IllegalArgumentException("Message is required");
            }

            // basic size limits to avoid abuse
            if (s.length() > 120) {
                throw new IllegalArgumentException("Subject is too long");
            }
            if (m.length() > 4000) {
                throw new IllegalArgumentException("Message is too long");
            }

            User user = userRepository.findByEmail(authentication.getName()).orElseThrow();
            SupportRequest req = new SupportRequest(user, s, m);
            supportRequestRepository.save(req);

            return "redirect:/support?sent=true";
        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/support";
        }
    }
}
