package com.example.ecommerce_cart_service.exceptions;

import lombok.Getter;

@Getter
public class CartNotFoundException extends RuntimeException {
    private final Long userId;
    private final String message;
    public CartNotFoundException(Long userId) {
        super("Cart not found for userId: " + userId);
        this.userId = userId;
        this.message = "Cart not found for userId: " + userId;
    }
}
