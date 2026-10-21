package com.example.ecommerce.controller;

import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin/products")
public class AdminController {
    private final ProductRepository products;
    public AdminController(ProductRepository products) { this.products = products; }

    @GetMapping
    public String list(Model model) {
        model.addAttribute("products", products.findAll());
        return "admin/products";
    }

    @GetMapping("/new")
    public String createForm(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("formTitle", "Add Product");
        return "admin/product-form";
    }

    @PostMapping
    public String create(@Valid @ModelAttribute Product product, BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("formTitle", "Add Product");
            return "admin/product-form";
        }
        products.save(product);
        return "redirect:/admin/products";
    }

    @GetMapping("/{id}/edit")
    public String editForm(@PathVariable Long id, Model model) {
        Product product = products.findById(id).orElseThrow();
        model.addAttribute("product", product);
        model.addAttribute("formTitle", "Edit Product");
        return "admin/product-form";
    }

    @PostMapping("/{id}")
    public String update(@PathVariable Long id, @Valid @ModelAttribute Product product,
                         BindingResult result, Model model) {
        if (result.hasErrors()) {
            model.addAttribute("formTitle", "Edit Product");
            return "admin/product-form";
        }
        product.setId(id);
        products.save(product);
        return "redirect:/admin/products";
    }

    @PostMapping("/{id}/delete")
    public String delete(@PathVariable Long id) {
        products.deleteById(id);
        return "redirect:/admin/products";
    }
}
