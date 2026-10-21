package com.example.ecommerce.controller;

import com.example.ecommerce.model.Order;                 // adjust if your entity has another name
import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.OrderRepository;  // adjust to your repository names
import com.example.ecommerce.repository.ProductRepository;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;

import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/**
 * Admin dashboard and order management.
 * Product add/edit/delete stays in your existing /admin/products controller.
 */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final int LOW_STOCK_LIMIT = 5;
    private static final Set<String> STATUSES = Set.of("PLACED", "SHIPPED", "DELIVERED", "CANCELLED");

    private final ProductRepository productRepository;
    private final OrderRepository orderRepository;

    public AdminController(ProductRepository productRepository, OrderRepository orderRepository) {
        this.productRepository = productRepository;
        this.orderRepository = orderRepository;
    }

    @GetMapping
    public String dashboard(Model model) {
        List<Product> products = productRepository.findAll();
        List<Order> orders = orderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));

        List<Product> lowStock = products.stream()
                .filter(p -> p.getStock() <= LOW_STOCK_LIMIT)
                .toList();

        // String.valueOf works whether total is BigDecimal, Double, etc.
        BigDecimal revenue = orders.stream()
                .filter(o -> !"CANCELLED".equals(String.valueOf(o.getStatus())))
                .map(o -> new BigDecimal(String.valueOf(o.getTotal())))
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("productCount", products.size());
        model.addAttribute("orderCount", orders.size());
        model.addAttribute("revenue", revenue);
        model.addAttribute("lowStockCount", lowStock.size());
        model.addAttribute("lowStockProducts", lowStock);
        model.addAttribute("recentOrders", orders.stream().limit(5).toList());
        return "admin-dashboard";
    }

    @GetMapping("/orders")
    public String orders(Model model) {
        model.addAttribute("orders", orderRepository.findAll(Sort.by(Sort.Direction.DESC, "createdAt")));
        return "admin-orders";
    }

    @PostMapping("/orders/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam String status) {
        if (!STATUSES.contains(status)) {
            return "redirect:/admin/orders";
        }
        orderRepository.findById(id).ifPresent(order -> {
            order.setStatus(status);     // if status is an enum: order.setStatus(OrderStatus.valueOf(status))
            orderRepository.save(order);
        });
        return "redirect:/admin/orders";
    }
}