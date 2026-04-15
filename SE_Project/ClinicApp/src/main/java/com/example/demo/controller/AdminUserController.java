package com.example.demo.controller;

import com.example.demo.entity.User;
import com.example.demo.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.util.List;

@Controller
@RequestMapping("/admin/users")
public class AdminUserController {

    private final UserRepository userRepository;

    public AdminUserController(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @GetMapping
    public String users(Model model) {
        List<User> users = userRepository.findAll();
        model.addAttribute("users", users);
        model.addAttribute("totalCount", users.size());
        return "admin/users";
    }

    @PostMapping("/{id}/toggle-enabled")
    public String toggleEnabled(@PathVariable Long id,
                                Authentication authentication,
                                RedirectAttributes attributes) {
        try {
            if (authentication != null) {
                User current = userRepository.findByEmail(authentication.getName()).orElse(null);
                if (current != null && current.getId() != null && current.getId().equals(id)) {
                    attributes.addFlashAttribute("error", "You cannot disable your own account");
                    return "redirect:/admin/users";
                }
            }

            User user = userRepository.findById(id)
                    .orElseThrow(() -> new IllegalArgumentException("User not found"));
            user.setEnabled(!user.isEnabled());
            userRepository.save(user);
            return "redirect:/admin/users?updated=true";
        } catch (Exception e) {
            attributes.addFlashAttribute("error", e.getMessage());
            return "redirect:/admin/users";
        }
    }
}
