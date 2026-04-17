package com.example.ecommerce_cart_service.exceptions;

import lombok.Getter;

@Getter
public class ActiveCartNotFoundException extends RuntimeException {
    private final Long userId;
    private final String message;
    public ActiveCartNotFoundException(Long userId) {
        super("Active Cart not found for userId: " + userId);
        this.userId = userId;
        this.message = "Active Cart not found for userId: " + userId;
    }
}
