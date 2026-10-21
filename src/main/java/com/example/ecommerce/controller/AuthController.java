package com.example.ecommerce.controller;

import com.example.ecommerce.model.AppUser;
import com.example.ecommerce.repository.UserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class AuthController {
    private final UserRepository users;
    private final PasswordEncoder encoder;

    public AuthController(UserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/register")
    public String register() { return "register"; }

    @PostMapping("/register")
    public String createAccount(@RequestParam String email,
                                @RequestParam String password, Model model) {
        String normalizedEmail = email.trim().toLowerCase();
        if (!normalizedEmail.matches("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$")) {
            model.addAttribute("error", "Enter a valid email address.");
            return "register";
        }
        if (password.length() < 8) {
            model.addAttribute("error", "Password must contain at least 8 characters.");
            return "register";
        }
        if (users.existsByEmail(normalizedEmail)) {
            model.addAttribute("error", "Email is already registered.");
            return "register";
        }
        users.save(new AppUser(normalizedEmail, encoder.encode(password), "ROLE_USER"));
        return "redirect:/login?registered";
    }
}
