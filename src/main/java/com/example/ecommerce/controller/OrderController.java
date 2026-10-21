package com.example.ecommerce.controller;

import com.example.ecommerce.model.CustomerOrder;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.Map;

@Controller
public class OrderController {
    private final ProductRepository products;
    private final OrderRepository orders;

    public OrderController(ProductRepository products, OrderRepository orders) {
        this.products = products; this.orders = orders;
    }

    @SuppressWarnings("unchecked")
    @PostMapping("/checkout")
    @Transactional
    public String checkout(Authentication auth, HttpSession session, Model model) {
        Map<Long, Integer> cart = (Map<Long, Integer>) session.getAttribute("cart");
        if (cart == null || cart.isEmpty()) return "redirect:/cart";

        BigDecimal total = BigDecimal.ZERO;
        StringBuilder summary = new StringBuilder();
        for (var entry : cart.entrySet()) {
            Product p = products.findById(entry.getKey()).orElse(null);
            if (p == null || entry.getValue() < 1 || p.getStock() < entry.getValue()) {
                model.addAttribute("error", "A product is unavailable or stock changed. Review your cart.");
                return showCart(session, model);
            }
            total = total.add(p.getPrice().multiply(BigDecimal.valueOf(entry.getValue())));
            if (!summary.isEmpty()) summary.append("; ");
            summary.append(p.getName()).append(" x ").append(entry.getValue());
        }
        for (var entry : cart.entrySet()) {
            Product p = products.findById(entry.getKey()).orElseThrow();
            p.setStock(p.getStock() - entry.getValue());
            products.save(p);
        }
        orders.save(new CustomerOrder(auth.getName(), summary.toString(), total));
        cart.clear();
        return "redirect:/orders";
    }

    private String showCart(HttpSession session, Model model) {
        Map<Long, Integer> cart = (Map<Long, Integer>) session.getAttribute("cart");
        java.util.List<Map<String, Object>> items = new java.util.ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        if (cart != null) for (var e : cart.entrySet()) {
            Product p = products.findById(e.getKey()).orElse(null);
            if (p != null) {
                BigDecimal subtotal = p.getPrice().multiply(BigDecimal.valueOf(e.getValue()));
                Map<String, Object> item = new java.util.HashMap<>();
                item.put("product", p); item.put("quantity", e.getValue()); item.put("subtotal", subtotal);
                items.add(item); total = total.add(subtotal);
            }
        }
        model.addAttribute("items", items); model.addAttribute("total", total);
        return "cart";
    }

    @GetMapping("/orders")
    public String history(Authentication auth, Model model) {
        model.addAttribute("orders",
            orders.findByCustomerEmailOrderByCreatedAtDesc(auth.getName()));
        return "orders";
    }
}
