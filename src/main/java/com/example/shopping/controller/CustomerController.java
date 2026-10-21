package com.example.shopping.controller;

import com.example.shopping.entity.CustomerOrder;
import com.example.shopping.entity.Product;
import com.example.shopping.repository.CustomerOrderRepository;
import com.example.shopping.repository.ProductRepository;
import jakarta.transaction.Transactional;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

import java.math.BigDecimal;

@Controller
@RequestMapping("/customer")
public class CustomerController {
    private final ProductRepository products;
    private final CustomerOrderRepository orders;

    public CustomerController(ProductRepository products, CustomerOrderRepository orders) {
        this.products = products;
        this.orders = orders;
    }

    @GetMapping("/products")
    public String products(Model model) {
        model.addAttribute("products", products.findAll());
        return "customer-products";
    }

    @PostMapping("/order/{id}")
    @Transactional
    public String order(@PathVariable Long id, @RequestParam Integer quantity,
                        Authentication authentication, RedirectAttributes redirectAttributes) {
        if (quantity == null || quantity < 1) {
            redirectAttributes.addFlashAttribute("error", "Please enter a quantity of at least 1.");
            return "redirect:/customer/products";
        }
        Product product = products.findById(id).orElse(null);
        if (product == null) {
            redirectAttributes.addFlashAttribute("error", "Product not found.");
            return "redirect:/customer/products";
        }
        if (product.getQuantity() < quantity) {
            redirectAttributes.addFlashAttribute("error", "Not enough stock available.");
            return "redirect:/customer/products";
        }

        BigDecimal total = product.getPrice().multiply(BigDecimal.valueOf(quantity));
        orders.save(new CustomerOrder(authentication.getName(), product.getId(),
                product.getName(), quantity, product.getPrice(), total));
        product.setQuantity(product.getQuantity() - quantity);
        products.save(product);
        redirectAttributes.addFlashAttribute("message", "Your order has been placed.");
        return "redirect:/customer/orders";
    }

    @GetMapping("/orders")
    public String myOrders(Authentication authentication, Model model) {
        model.addAttribute("orders",
                orders.findByCustomerUsernameOrderByOrderDateDesc(authentication.getName()));
        return "customer-orders";
    }
}
