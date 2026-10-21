package com.example.shopping.controller;

import com.example.shopping.entity.Product;
import com.example.shopping.repository.CustomerOrderRepository;
import com.example.shopping.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
@RequestMapping("/admin")
public class AdminController {
    private final ProductRepository products;
    private final CustomerOrderRepository orders;

    public AdminController(ProductRepository products, CustomerOrderRepository orders) {
        this.products = products;
        this.orders = orders;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("products", products.findAll());
        model.addAttribute("orders", orders.findAllByOrderByOrderDateDesc());
        return "admin-dashboard";
    }

    @PostMapping("/products")
    public String addProduct(@Valid @ModelAttribute("product") Product product,
                             BindingResult result, Model model,
                             RedirectAttributes redirectAttributes) {
        if (result.hasErrors()) {
            model.addAttribute("products", products.findAll());
            model.addAttribute("orders", orders.findAllByOrderByOrderDateDesc());
            return "admin-dashboard";
        }
        products.save(product);
        redirectAttributes.addFlashAttribute("message", "Product added successfully.");
        return "redirect:/admin/dashboard";
    }

    @PostMapping("/products/{id}/delete")
    public String deleteProduct(@PathVariable Long id, RedirectAttributes redirectAttributes) {
        if (products.existsById(id)) {
            products.deleteById(id);
            redirectAttributes.addFlashAttribute("message", "Product deleted.");
        }
        return "redirect:/admin/dashboard";
    }
}
