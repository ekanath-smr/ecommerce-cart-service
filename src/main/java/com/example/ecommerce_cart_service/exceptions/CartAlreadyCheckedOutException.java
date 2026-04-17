package com.example.ecommerce_cart_service.exceptions;

import lombok.Getter;

@Getter
public class CartAlreadyCheckedOutException extends RuntimeException {
    private final Long cartId;
    private final String message;
    public CartAlreadyCheckedOutException(Long cartId) {
        super("Cart already checked out: " + cartId);
        this.cartId = cartId;
        this.message = "Cart already checked out: " + cartId;
    }
}
