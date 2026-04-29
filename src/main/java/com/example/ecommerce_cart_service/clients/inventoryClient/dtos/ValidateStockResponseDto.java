package com.example.ecommerce_cart_service.clients.inventoryClient.dtos;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ValidateStockResponseDto {
    private Long productId;
    private Integer requestedQuantity;
    private Integer availableQuantity;
    private Boolean isStockAvailable;
}
