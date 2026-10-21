package com.example.ecommerce.controller;

import com.example.ecommerce.model.CustomerOrder;
import com.example.ecommerce.model.Product;
import com.example.ecommerce.model.ProductImage;
import com.example.ecommerce.repository.OrderRepository;
import com.example.ecommerce.repository.ProductImageRepository;
import com.example.ecommerce.repository.ProductRepository;
import jakarta.validation.Valid;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Controller;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.List;
import java.util.Set;

/** Admin area: dashboard, products (with images) and orders. Requires ROLE_ADMIN. */
@Controller
@RequestMapping("/admin")
public class AdminController {

    private static final int LOW_STOCK_LIMIT = 5;
    private static final long MAX_IMAGE_BYTES = 2L * 1024 * 1024;
    private static final Set<String> STATUSES = Set.of("PLACED", "SHIPPED", "DELIVERED", "CANCELLED");

    private final ProductRepository products;
    private final OrderRepository orders;
    private final ProductImageRepository images;

    public AdminController(ProductRepository products, OrderRepository orders, ProductImageRepository images) {
        this.products = products;
        this.orders = orders;
        this.images = images;
    }

    // ---------- Dashboard ----------

    @GetMapping
    public String dashboard(Model model) {
        List<Product> allProducts = products.findAll();
        List<CustomerOrder> allOrders = orders.findAll(Sort.by(Sort.Direction.DESC, "createdAt"));

        List<Product> lowStock = allProducts.stream()
                .filter(p -> p.getStock() <= LOW_STOCK_LIMIT)
                .toList();

        BigDecimal revenue = allOrders.stream()
                .filter(o -> !"CANCELLED".equals(o.getStatus()))
                .map(CustomerOrder::getTotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        model.addAttribute("productCount", allProducts.size());
        model.addAttribute("orderCount", allOrders.size());
        model.addAttribute("revenue", revenue);
        model.addAttribute("lowStockCount", lowStock.size());
        model.addAttribute("lowStockProducts", lowStock);
        model.addAttribute("recentOrders", allOrders.stream().limit(5).toList());
        return "admin-dashboard";
    }

    // ---------- Orders ----------

    @GetMapping("/orders")
    public String orderList(Model model) {
        model.addAttribute("orders", orders.findAll(Sort.by(Sort.Direction.DESC, "createdAt")));
        return "admin-orders";
    }

    @PostMapping("/orders/{id}/status")
    public String updateStatus(@PathVariable Long id, @RequestParam String status) {
        if (STATUSES.contains(status)) {
            orders.findById(id).ifPresent(order -> {
                order.setStatus(status);
                orders.save(order);
            });
        }
        return "redirect:/admin/orders";
    }

    // ---------- Products ----------

    @GetMapping("/products")
    public String productList(Model model) {
        model.addAttribute("products", products.findAll(Sort.by("id")));
        return "admin-products";
    }

    @GetMapping("/products/new")
    public String newProduct(Model model) {
        model.addAttribute("product", new Product());
        model.addAttribute("formTitle", "Add product");
        return "product-form";
    }

    @PostMapping("/products")
    @Transactional
    public String createProduct(@Valid @ModelAttribute("product") Product product,
                                BindingResult result,
                                @RequestParam(value = "image", required = false) MultipartFile image,
                                @RequestParam(value = "imageLink", required = false) String imageLink,
                                Model model) {
        ImageInput input = readImage(image, imageLink, false);
        product.setId(null);
        product.setImageUrl(null);
        if (result.hasErrors() || input.error != null) {
            model.addAttribute("imageError", input.error);
            model.addAttribute("formTitle", "Add product");
            return "product-form";
        }
        Product saved = products.save(product);
        applyImage(saved, input, null);
        products.save(saved);
        return "redirect:/admin/products";
    }

    @GetMapping("/products/{id}/edit")
    public String editProduct(@PathVariable Long id, Model model) {
        return products.findById(id).map(p -> {
            model.addAttribute("product", p);
            model.addAttribute("formTitle", "Edit product");
            return "product-form";
        }).orElse("redirect:/admin/products");
    }

    @PostMapping("/products/{id}")
    @Transactional
    public String updateProduct(@PathVariable Long id,
                                @Valid @ModelAttribute("product") Product product,
                                BindingResult result,
                                @RequestParam(value = "image", required = false) MultipartFile image,
                                @RequestParam(value = "imageLink", required = false) String imageLink,
                                @RequestParam(value = "removeImage", defaultValue = "false") boolean removeImage,
                                Model model) {
        Product existing = products.findById(id).orElse(null);
        if (existing == null) {
            return "redirect:/admin/products";
        }
        String currentUrl = existing.getImageUrl();
        ImageInput input = readImage(image, imageLink, removeImage);
        product.setId(id);
        if (result.hasErrors() || input.error != null) {
            product.setImageUrl(currentUrl);
            model.addAttribute("imageError", input.error);
            model.addAttribute("formTitle", "Edit product");
            return "product-form";
        }
        applyImage(product, input, currentUrl);
        products.save(product);
        return "redirect:/admin/products";
    }

    @PostMapping("/products/{id}/delete")
    @Transactional
    public String deleteProduct(@PathVariable Long id) {
        if (images.existsById(id)) {
            images.deleteById(id);
        }
        if (products.existsById(id)) {
            products.deleteById(id);
        }
        return "redirect:/admin/products";
    }

    // ---------- Image helpers ----------

    private static class ImageInput {
        byte[] data;
        String type;
        String link;
        boolean remove;
        String error;
    }

    private ImageInput readImage(MultipartFile file, String link, boolean remove) {
        ImageInput in = new ImageInput();
        in.remove = remove;

        if (file != null && !file.isEmpty()) {
            if (file.getSize() > MAX_IMAGE_BYTES) {
                in.error = "The image must be 2 MB or smaller.";
                return in;
            }
            byte[] bytes;
            try {
                bytes = file.getBytes();
            } catch (IOException e) {
                in.error = "The image could not be read. Please try again.";
                return in;
            }
            String type = detectType(bytes);
            if (type == null) {
                in.error = "Please upload a JPG, PNG, WebP or GIF image.";
                return in;
            }
            in.data = bytes;
            in.type = type;
            return in;
        }

        if (link != null && !link.isBlank()) {
            String l = link.trim();
            boolean ok = (l.startsWith("http://") || l.startsWith("https://")) && l.length() <= 255;
            if (ok) {
                in.link = l;
            } else {
                in.error = "Enter a valid http(s) image link (255 characters at most).";
            }
        }
        return in;
    }

    /** Decides what to store: uploaded file, pasted link, nothing (removed) or the existing picture. */
    private void applyImage(Product product, ImageInput in, String currentUrl) {
        Long id = product.getId();
        if (in.data != null) {
            images.save(new ProductImage(id, in.type, in.data));
            product.setImageUrl("/products/" + id + "/image?v=" + System.currentTimeMillis());
        } else if (in.link != null) {
            deleteStoredImage(id);
            product.setImageUrl(in.link);
        } else if (in.remove) {
            deleteStoredImage(id);
            product.setImageUrl(null);
        } else {
            product.setImageUrl(currentUrl);
        }
    }

    private void deleteStoredImage(Long id) {
        if (id != null && images.existsById(id)) {
            images.deleteById(id);
        }
    }

    /** Checks the first bytes of the file, so only real JPG, PNG, GIF and WebP files are accepted. */
    private static String detectType(byte[] b) {
        if (b.length > 3 && (b[0] & 0xFF) == 0xFF && (b[1] & 0xFF) == 0xD8 && (b[2] & 0xFF) == 0xFF) {
            return "image/jpeg";
        }
        if (b.length > 8 && (b[0] & 0xFF) == 0x89 && b[1] == 'P' && b[2] == 'N' && b[3] == 'G') {
            return "image/png";
        }
        if (b.length > 6 && b[0] == 'G' && b[1] == 'I' && b[2] == 'F' && b[3] == '8') {
            return "image/gif";
        }
        if (b.length > 12 && b[0] == 'R' && b[1] == 'I' && b[2] == 'F' && b[3] == 'F'
                && b[8] == 'W' && b[9] == 'E' && b[10] == 'B' && b[11] == 'P') {
            return "image/webp";
        }
        return null;
    }
}