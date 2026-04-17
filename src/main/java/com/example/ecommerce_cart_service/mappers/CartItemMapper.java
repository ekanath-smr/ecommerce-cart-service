package com.example.ecommerce_cart_service.mappers;

import com.example.ecommerce_cart_service.dtos.response.CartItemResponseDto;
import com.example.ecommerce_cart_service.models.CartItem;

public class CartItemMapper {
    public static CartItemResponseDto mapToCartItemResponse(CartItem cartItem) {
        return CartItemResponseDto.builder()
                .productId(cartItem.getProductId())
                .productName(cartItem.getProductNameSnapshot())
                .unitPrice(cartItem.getPriceSnapshot())
                .quantity(cartItem.getQuantity())
                .subtotal(cartItem.getSubtotal())
                .build();
    }
}
