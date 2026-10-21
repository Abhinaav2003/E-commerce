package com.example.ecommerce.config;

import com.example.ecommerce.model.AppUser;
import com.example.ecommerce.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.security.SecureRandom;
import java.util.Optional;

/**
 * Makes sure an admin account exists on startup.
 *
 * Configure it with the ADMIN_EMAIL and ADMIN_PASSWORD environment variables.
 * If ADMIN_PASSWORD is not set, a random password is generated and printed once in the log.
 */
@Component
public class AdminSeeder implements CommandLineRunner {

    private static final Logger log = LoggerFactory.getLogger(AdminSeeder.class);
    private static final String ADMIN_ROLE = "ROLE_ADMIN";
    private static final String ALPHABET = "abcdefghijkmnpqrstuvwxyzABCDEFGHJKLMNPQRSTUVWXYZ23456789";

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
        String email = adminEmail.trim().toLowerCase();
        Optional<AppUser> existing = users.findByEmail(email);

        if (existing.isPresent()) {
            AppUser user = existing.get();
            if (!ADMIN_ROLE.equals(user.getRole())) {
                user.setRole(ADMIN_ROLE);
                users.save(user);
                log.info("Existing account {} was promoted to admin.", email);
            }
            return;
        }

        boolean generated = adminPassword == null || adminPassword.isBlank();
        String password = generated ? randomPassword() : adminPassword;
        users.save(new AppUser(email, encoder.encode(password), ADMIN_ROLE));

        if (generated) {
            log.warn("==========================================================");
            log.warn(" Admin account created: {}", email);
            log.warn(" Generated password:     {}", password);
            log.warn(" Set ADMIN_PASSWORD to choose your own. Shown only once.");
            log.warn("==========================================================");
        } else {
            log.info("Admin account created: {}", email);
        }
    }

    private static String randomPassword() {
        SecureRandom random = new SecureRandom();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 16; i++) {
            sb.append(ALPHABET.charAt(random.nextInt(ALPHABET.length())));
        }
        return sb.toString();
    }
}