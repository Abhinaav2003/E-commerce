package com.example.shopping.config;

import com.example.shopping.entity.AppUser;
import com.example.shopping.entity.Product;
import com.example.shopping.repository.AppUserRepository;
import com.example.shopping.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.math.BigDecimal;

@Configuration
public class DataInitializer {
    @Bean
    CommandLineRunner seedData(AppUserRepository users, ProductRepository products,
                               PasswordEncoder encoder) {
        return args -> {
            if (!users.existsByUsername("admin")) {
                users.save(new AppUser("admin", encoder.encode("admin123"), "ADMIN"));
            }
            if (products.count() == 0) {
                products.save(new Product("Notebook", "A simple ruled notebook", new BigDecimal("60.00"), 25));
                products.save(new Product("Pen Pack", "Pack of blue ball pens", new BigDecimal("40.00"), 40));
                products.save(new Product("Water Bottle", "Reusable 1 litre bottle", new BigDecimal("180.00"), 12));
            }
        };
    }
}
