package com.example.ecommerce_cart_service.exceptions;

import lombok.Setter;

@Setter
public class ProductUnavailableException extends RuntimeException {
    private final Long productId;
    private final String message;
    public ProductUnavailableException(Long productId) {
        super("Product is unavailable: " + productId);
        this.productId = productId;
        this.message = "Product is unavailable: " + productId;
    }
}
