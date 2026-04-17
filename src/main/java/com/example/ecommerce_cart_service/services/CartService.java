package com.example.ecommerce_cart_service.services;

import com.example.ecommerce_cart_service.dtos.request.AddToCartRequestDto;
import com.example.ecommerce_cart_service.dtos.request.UpdateCartItemRequestDto;
import com.example.ecommerce_cart_service.dtos.response.CartResponseDto;

public interface CartService {
    CartResponseDto getCart(Long userId);
    CartResponseDto addItemToCart(Long userId, AddToCartRequestDto request);
    CartResponseDto updateItemInCart(Long userId, Long productId, UpdateCartItemRequestDto request);
    CartResponseDto removeItemFromCart(Long userId, Long productId);
    CartResponseDto clearCart(Long userId);
    CartResponseDto checkoutCart(Long userId);
}
