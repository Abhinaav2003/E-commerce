package com.example.ecommerce;

import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.ProductRepository;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

@SpringBootApplication
public class EcommerceApplication {
    public static void main(String[] args) {
        SpringApplication.run(EcommerceApplication.class, args);
    }

    @Bean
    CommandLineRunner seedProducts(ProductRepository repository) {
        return args -> {
            if (repository.count() == 0) {
                repository.save(new Product("Wireless Headphones",
                    "Comfortable wireless headphones", new java.math.BigDecimal("1499.00"),
                    "https://placehold.co/600x400?text=Headphones", 20));
                repository.save(new Product("Smart Watch",
                    "Smart watch with fitness tracking", new java.math.BigDecimal("2499.00"),
                    "https://placehold.co/600x400?text=Smart+Watch", 15));
                repository.save(new Product("Laptop Backpack",
                    "Durable everyday laptop backpack", new java.math.BigDecimal("999.00"),
                    "https://placehold.co/600x400?text=Backpack", 30));
            }
        };
    }
}
