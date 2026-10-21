package com.example.ecommerce.config;

import com.example.ecommerce.model.AppUser;
import com.example.ecommerce.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

/**
 * Creates the first admin account on startup.
 * Set app.admin.email and app.admin.password (see README); nothing is created if the password is empty.
 */
@Component
public class AdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);

    private final UserRepository users;
    private final PasswordEncoder encoder;
    private final String adminEmail;
    private final String adminPassword;

    public AdminSeeder(UserRepository users,
                       PasswordEncoder encoder,
                       @Value("${app.admin.email:admin@cheetah.com}") String adminEmail,
                       @Value("${app.admin.password:}") String adminPassword) {
        this.users = users;
        this.encoder = encoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
    }

    @Override
    public void run(String... args) {
        if (adminPassword.isBlank()) {
            log.warn("app.admin.password is not set, so no admin account was created.");
            return;
        }
        if (!users.existsByEmail(adminEmail)) {
            users.save(new AppUser(adminEmail, encoder.encode(adminPassword), "ROLE_ADMIN"));
            log.info("Created admin account {}", adminEmail);
        }
    }
}