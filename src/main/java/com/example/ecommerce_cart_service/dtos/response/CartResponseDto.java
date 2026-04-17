package com.example.ecommerce_cart_service.dtos.response;

import com.example.ecommerce_cart_service.models.CartStatus;
import lombok.Builder;
import lombok.Getter;

import java.math.BigDecimal;
import java.util.List;

@Getter
@Builder
public class CartResponseDto {
    private Long cartId;
    private Long userId;
    private CartStatus cartStatus;
    private Integer totalItems;
    private BigDecimal totalPrice;
    @Builder.Default
    private List<CartItemResponseDto> items = List.of();
}
