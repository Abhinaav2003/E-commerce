package com.example.ecommerce.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

/** An uploaded product picture. The id is the product's id, so there is at most one per product. */
@Entity
@Table(name = "product_images")
public class ProductImage {
    @Id
    private Long productId;

    @Column(nullable = false, length = 50)
    private String contentType;

    @Column(nullable = false, length = 5000000)
    private byte[] data;

    protected ProductImage() {}

    public ProductImage(Long productId, String contentType, byte[] data) {
        this.productId = productId;
        this.contentType = contentType;
        this.data = data;
    }

    public Long getProductId() { return productId; }
    public String getContentType() { return contentType; }
    public byte[] getData() { return data; }
}