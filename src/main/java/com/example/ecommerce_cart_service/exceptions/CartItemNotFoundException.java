package com.example.ecommerce_cart_service.exceptions;

import lombok.Getter;

@Getter
public class CartItemNotFoundException extends RuntimeException {
    private final Long productId;
    private final String message;
    public CartItemNotFoundException(Long productId) {
        super("Cart item not found for productId: " + productId);
        this.productId = productId;
        this.message = "Cart item not found for productId: " + productId;
    }
}
