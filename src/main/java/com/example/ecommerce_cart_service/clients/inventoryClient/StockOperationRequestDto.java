package com.example.ecommerce_cart_service.clients.inventoryClient;

import jakarta.validation.constraints.Positive;
import lombok.Data;

@Data
public class StockOperationRequestDto {
    @Positive
    private Integer quantity;
    private String referenceId; // optional, useful for order/payment linkage

    public StockOperationRequestDto(Integer quantity) {
        this.quantity = quantity;
    }
}
