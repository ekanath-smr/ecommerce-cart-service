package com.example.ecommerce_cart_service.clients.orderClient;

import com.example.ecommerce_cart_service.clients.orderClient.dtos.CreateOrderRequestDto;
import com.example.ecommerce_cart_service.clients.orderClient.dtos.OrderResponseDto;

import com.example.ecommerce_cart_service.exceptions.ExternalServiceUnavailableException;
import com.example.ecommerce_cart_service.exceptions.InsufficientStockException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderServiceClient {

    private final OrderClient orderClient;

    @CircuitBreaker(name = "orderService", fallbackMethod = "fallbackCreateOrder")
    public OrderResponseDto createOrder(CreateOrderRequestDto request, String idempotencyKey) {
        log.info("Calling Order Service with idempotencyKey={}", idempotencyKey);
        return orderClient.createOrder(request, idempotencyKey);
    }

    public OrderResponseDto fallbackCreateOrder(CreateOrderRequestDto request, String idempotencyKey, Throwable ex) {

        if (ex instanceof FeignException feignEx) {
            int status = feignEx.status();
            String responseBody = feignEx.contentUTF8();
            log.warn("Order service error status={}, response={}", status, responseBody);

            // BUSINESS ERROR (409)
            if (status == 409) {
                if (responseBody.contains("Insufficient stock")) {
                    throw new InsufficientStockException(responseBody);
                }
                throw new RuntimeException("Conflict occurred: " + responseBody);
            }

            // BAD REQUEST
            if (status == 400) {
                throw new RuntimeException("Invalid request: " + responseBody);
            }

            // NOT FOUND
            if (status == 404) {
                throw new RuntimeException("Order not found");
            }

            // SERVER ERROR → fallback
            if (status >= 500) {
                log.error("Order service 5xx error", ex);
            }
        }

        // NETWORK / TIMEOUT / UNKNOWN
        log.error("Order Service unavailable for idempotencyKey={}", idempotencyKey, ex);

        throw new ExternalServiceUnavailableException(
                "Order service unavailable for idempotencyKey: " + idempotencyKey, ex);
    }
}
