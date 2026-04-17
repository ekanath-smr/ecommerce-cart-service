package com.example.ecommerce_cart_service.mappers;

import com.example.ecommerce_cart_service.dtos.response.CartItemResponseDto;
import com.example.ecommerce_cart_service.dtos.response.CartResponseDto;
import com.example.ecommerce_cart_service.models.Cart;

import java.util.List;

public class CartMapper {

    public static CartResponseDto mapToCartResponse(Cart cart) {

        List<CartItemResponseDto> cartItemResponseDtos =
                cart.getItems() == null ? List.of() : cart.getItems().stream()
                .map(CartItemMapper::mapToCartItemResponse)
                .toList();

        return CartResponseDto.builder()
                .cartId(cart.getId())
                .userId(cart.getUserId())
                .cartStatus(cart.getStatus())
                .totalItems(cart.getTotalItems())
                .totalPrice(cart.getTotalPrice())
                .items(cartItemResponseDtos)
                .build();
    }
}
