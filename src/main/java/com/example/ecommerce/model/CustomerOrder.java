package com.example.ecommerce.model;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Entity
@Table(name = "customer_orders")
public class CustomerOrder {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String customerEmail;

    @Column(nullable = false, length = 5000)
    private String itemsSummary;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal total;

    @Column(nullable = false)
    private LocalDateTime createdAt = LocalDateTime.now();

    protected CustomerOrder() {}
    public CustomerOrder(String customerEmail, String itemsSummary, BigDecimal total) {
        this.customerEmail = customerEmail;
        this.itemsSummary = itemsSummary;
        this.total = total;
    }
    public Long getId() { return id; }
    public String getCustomerEmail() { return customerEmail; }
    public String getItemsSummary() { return itemsSummary; }
    public BigDecimal getTotal() { return total; }
    public LocalDateTime getCreatedAt() { return createdAt; }
}
