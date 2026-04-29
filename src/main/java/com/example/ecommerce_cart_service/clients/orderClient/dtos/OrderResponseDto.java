package com.example.ecommerce_cart_service.clients.orderClient.dtos;

import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrderResponseDto {
    private Long orderId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
    private Long userId;
    private String status;
    private BigDecimal totalAmount;
    private List<OrderItemResponseDto> items;
}
