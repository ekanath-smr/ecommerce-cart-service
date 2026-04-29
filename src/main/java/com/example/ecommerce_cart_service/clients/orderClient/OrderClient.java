package com.example.ecommerce_cart_service.clients.orderClient;

import com.example.ecommerce_cart_service.clients.orderClient.dtos.CreateOrderRequestDto;
import com.example.ecommerce_cart_service.clients.orderClient.dtos.OrderResponseDto;
import com.example.ecommerce_cart_service.configs.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;

@FeignClient(
        name = "order-service",
        url = "${order.service.url}",
        configuration = FeignConfig.class
)
public interface OrderClient {

    @PostMapping("/orders")
    OrderResponseDto createOrder(
            @RequestBody CreateOrderRequestDto request,
            @RequestHeader("Idempotency-Key") String idempotencyKey
    );
}
