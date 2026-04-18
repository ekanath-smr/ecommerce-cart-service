package com.example.ecommerce_cart_service.dtos.response;

import lombok.Builder;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Builder
public class ErrorResponseDto {
    private LocalDateTime timestamp;
    private Integer status;
    private String error;
    private String message;
}