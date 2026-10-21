package com.example.ecommerce.controller;

import com.example.ecommerce.model.Product;
import com.example.ecommerce.repository.ProductRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;
import java.util.*;

@Controller
public class CartController {
    private final ProductRepository products;
    public CartController(ProductRepository products) { this.products = products; }

    @SuppressWarnings("unchecked")
    private Map<Long, Integer> cart(HttpSession session) {
        Map<Long, Integer> cart = (Map<Long, Integer>) session.getAttribute("cart");
        if (cart == null) {
            cart = new LinkedHashMap<>();
            session.setAttribute("cart", cart);
        }
        return cart;
    }

    @PostMapping("/cart/add/{id}")
    public String add(@PathVariable Long id, HttpSession session) {
        products.findById(id).ifPresent(p -> {
            Map<Long, Integer> c = cart(session);
            int current = c.getOrDefault(id, 0);
            if (p.getStock() > current) c.put(id, current + 1);
        });
        return "redirect:/cart";
    }

    @GetMapping("/cart")
    public String view(HttpSession session, Model model) {
        Map<Long, Integer> c = cart(session);
        List<Map<String, Object>> items = new ArrayList<>();
        BigDecimal total = BigDecimal.ZERO;
        Iterator<Map.Entry<Long, Integer>> iterator = c.entrySet().iterator();
        while (iterator.hasNext()) {
            var entry = iterator.next();
            Product p = products.findById(entry.getKey()).orElse(null);
            if (p == null) { iterator.remove(); continue; }
            int qty = entry.getValue();
            BigDecimal subtotal = p.getPrice().multiply(BigDecimal.valueOf(qty));
            Map<String, Object> item = new HashMap<>();
            item.put("product", p); item.put("quantity", qty); item.put("subtotal", subtotal);
            items.add(item);
            total = total.add(subtotal);
        }
        model.addAttribute("items", items);
        model.addAttribute("total", total);
        return "cart";
    }

    @PostMapping("/cart/update/{id}")
    public String update(@PathVariable Long id, @RequestParam int quantity, HttpSession session) {
        Map<Long, Integer> c = cart(session);
        Product p = products.findById(id).orElse(null);
        if (p == null || quantity <= 0) c.remove(id);
        else c.put(id, Math.min(quantity, p.getStock()));
        return "redirect:/cart";
    }

    @PostMapping("/cart/remove/{id}")
    public String remove(@PathVariable Long id, HttpSession session) {
        cart(session).remove(id);
        return "redirect:/cart";
    }
}
