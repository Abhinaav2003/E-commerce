package com.example.ecommerce.controller;

import com.example.ecommerce.repository.ProductRepository;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class HomeController {
    private final ProductRepository products;
    public HomeController(ProductRepository products) { this.products = products; }

    @GetMapping("/")
    public String home(@RequestParam(defaultValue = "") String search, Model model) {
        model.addAttribute("products", search.isBlank()
            ? products.findAll() : products.findByNameContainingIgnoreCase(search.trim()));
        model.addAttribute("search", search);
        return "index";
    }
}
