package com.example.shopping.controller;

import com.example.shopping.entity.AppUser;
import com.example.shopping.repository.AppUserRepository;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.crypto.password.PasswordEncoder;

@Controller
public class AuthController {
    private final AppUserRepository users;
    private final PasswordEncoder encoder;

    public AuthController(AppUserRepository users, PasswordEncoder encoder) {
        this.users = users;
        this.encoder = encoder;
    }

    @GetMapping("/")
    public String home() { return "redirect:/login"; }

    @GetMapping("/login")
    public String login() { return "login"; }

    @GetMapping("/register")
    public String registerForm(Model model) {
        model.addAttribute("username", "");
        return "register";
    }

    @PostMapping("/register")
    public String register(@RequestParam @NotBlank @Size(min = 3, max = 60) String username,
                           @RequestParam @NotBlank @Size(min = 6, max = 100) String password,
                           Model model) {
        String cleanUsername = username.trim();
        if (cleanUsername.isBlank() || cleanUsername.length() < 3) {
            model.addAttribute("error", "Username must have at least 3 characters.");
            model.addAttribute("username", username);
            return "register";
        }
        if (users.existsByUsername(cleanUsername)) {
            model.addAttribute("error", "That username is already taken.");
            model.addAttribute("username", cleanUsername);
            return "register";
        }
        users.save(new AppUser(cleanUsername, encoder.encode(password), "CUSTOMER"));
        return "redirect:/login?registered";
    }
}
