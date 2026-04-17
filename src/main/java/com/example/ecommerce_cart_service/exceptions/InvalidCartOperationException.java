package com.example.ecommerce_cart_service.exceptions;

import lombok.Getter;

@Getter
public class InvalidCartOperationException extends RuntimeException {
    private final String message;
    public InvalidCartOperationException(String message) {
        super(message);
        this.message = message;
    }
}
