package com.example.ecommerce.controller;

import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.multipart.MaxUploadSizeExceededException;

/** Shows a friendly message instead of an error page when an uploaded file is too large. */
@ControllerAdvice
public class UploadErrorHandler {

    @ExceptionHandler(MaxUploadSizeExceededException.class)
    public String tooLarge() {
        return "redirect:/admin/products?tooLarge";
    }
}