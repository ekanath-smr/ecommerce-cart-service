package com.example.ecommerce_cart_service.exceptions;

import lombok.Getter;

@Getter
public class ProductNotFoundException extends RuntimeException {
    private final Long productId;
    private final String message;
    public ProductNotFoundException(Long productId, Long productId1) {
        super("Product not found: " + productId);
        this.productId = productId1;
        this.message = "Product not found: " + productId;
    }
}
