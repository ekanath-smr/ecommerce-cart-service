package com.example.ecommerce_cart_service.exceptions;

import lombok.Getter;

@Getter
public class InsufficientStockException extends RuntimeException {
    private final Long productId;
    private final Integer requested;
    private final Integer available;
    private final String message;
    public InsufficientStockException(Long productId, Integer requested, Integer available) {
        super("Insufficient stock for productId: " + productId + ". Requested: " + requested + ", Available: " + available);
        this.productId = productId;
        this.requested = requested;
        this.available = available;
        this.message = "Insufficient stock for productId: " + productId + ". Requested: " + requested + ", Available: " + available;
    }
}
