package com.example.ecommerce.controller;

import com.example.ecommerce.repository.ProductImageRepository;
import org.springframework.http.CacheControl;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.time.Duration;

/** Serves uploaded product pictures at /products/{id}/image (public, so the shop can show them). */
@Controller
public class ImageController {

    private final ProductImageRepository images;

    public ImageController(ProductImageRepository images) {
        this.images = images;
    }

    @GetMapping("/products/{id}/image")
    public ResponseEntity<byte[]> image(@PathVariable Long id) {
        return images.findById(id)
                .map(img -> ResponseEntity.ok()
                        .contentType(MediaType.parseMediaType(img.getContentType()))
                        .cacheControl(CacheControl.maxAge(Duration.ofDays(30)).cachePublic())
                        .body(img.getData()))
                .orElseGet(() -> ResponseEntity.notFound().build());
    }
}