package com.example.ecommerce_cart_service.exceptions;

public class ConcurrentCartUpdateException extends RuntimeException {
    public ConcurrentCartUpdateException() {
        super("Cart was updated concurrently. Please retry.");
    }
}
