package com.example.ecommerce.controller;

import com.example.ecommerce.model.AppUser;
import com.example.ecommerce.repository.UserRepository;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.springframework.security.web.context.SecurityContextRepository;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Admin login with a password only. The password belongs to the account in ADMIN_EMAIL.
 * After 5 wrong attempts the same IP address is locked out for 5 minutes.
 */
@Controller
public class AdminAuthController {

    private static final int MAX_ATTEMPTS = 5;
    private static final long LOCK_MILLIS = 5 * 60 * 1000L;

    private record Attempts(int count, long lockedUntil) {}

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final String adminEmail;
    private final SecurityContextRepository securityContextRepository = new HttpSessionSecurityContextRepository();
    private final Map<String, Attempts> attempts = new ConcurrentHashMap<>();

    public AdminAuthController(UserRepository users,
                               PasswordEncoder encoder,
                               @Value("${app.admin.email:admin@cheetah.com}") String adminEmail) {
        this.users = users;
        this.encoder = encoder;
        this.adminEmail = adminEmail.trim().toLowerCase();
    }

    @GetMapping("/admin/login")
    public String page() {
        return "admin-login";
    }

    @PostMapping("/admin/login")
    public String login(@RequestParam String password,
                        HttpServletRequest request,
                        HttpServletResponse response) {
        String ip = request.getRemoteAddr();
        if (isLocked(ip)) {
            return "redirect:/admin/login?locked";
        }

        AppUser admin = users.findByEmail(adminEmail)
                .filter(u -> "ROLE_ADMIN".equals(u.getRole()))
                .orElse(null);

        if (admin == null || !encoder.matches(password, admin.getPassword())) {
            recordFailure(ip);
            return "redirect:/admin/login?error";
        }
        attempts.remove(ip);

        // New session id after login (protects against session fixation)
        if (request.getSession(false) != null) {
            request.changeSessionId();
        }

        UserDetails principal = User.withUsername(admin.getEmail())
                .password("[protected]")
                .authorities(admin.getRole())
                .build();
        Authentication token = UsernamePasswordAuthenticationToken.authenticated(
                principal, null, principal.getAuthorities());

        SecurityContext context = SecurityContextHolder.createEmptyContext();
        context.setAuthentication(token);
        SecurityContextHolder.setContext(context);
        securityContextRepository.saveContext(context, request, response);

        return "redirect:/admin";
    }

    private boolean isLocked(String ip) {
        Attempts a = attempts.get(ip);
        return a != null && a.lockedUntil() > System.currentTimeMillis();
    }

    private void recordFailure(String ip) {
        long now = System.currentTimeMillis();
        if (attempts.size() > 10_000) {
            attempts.clear();
        }
        attempts.compute(ip, (key, a) -> {
            if (a == null || (a.lockedUntil() != 0 && a.lockedUntil() <= now)) {
                a = new Attempts(0, 0);
            }
            int count = a.count() + 1;
            return new Attempts(count, count >= MAX_ATTEMPTS ? now + LOCK_MILLIS : 0);
        });
    }
}