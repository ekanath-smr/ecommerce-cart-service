package com.example.ecommerce_cart_service.clients.inventoryClient;

import com.example.ecommerce_cart_service.exceptions.ExternalServiceUnavailableException;
import com.example.ecommerce_cart_service.exceptions.InsufficientStockException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Component;

@Component
public class InventoryServiceClient {

    private final InventoryClient inventoryClient;

    public InventoryServiceClient(InventoryClient inventoryClient) {
        this.inventoryClient = inventoryClient;
    }

    @CircuitBreaker(name = "inventoryService", fallbackMethod = "fallback")
    public void validateStock(Long productId, Integer quantity) {
        Boolean inStock = inventoryClient.isInStock(productId, quantity);
        if (Boolean.FALSE.equals(inStock)) {
            throw new InsufficientStockException(productId, quantity, 0);
        }
    }

    @CircuitBreaker(name = "inventoryService", fallbackMethod = "fallback")
    public void reserveStock(Long productId, Integer quantity) {
        InventoryResponseDto responseDto = inventoryClient.reserveStock(productId, new StockOperationRequestDto(quantity));
    }

    @CircuitBreaker(name = "inventoryService", fallbackMethod = "fallback")
    public void releaseStock(Long productId, Integer quantity) {
        InventoryResponseDto responseDto = inventoryClient.releaseStock(productId, new StockOperationRequestDto(quantity));
    }

    @CircuitBreaker(name = "inventoryService", fallbackMethod = "fallback")
    public void confirmSale(Long productId, Integer quantity) {
        InventoryResponseDto responseDto = inventoryClient.confirmSale(productId, new StockOperationRequestDto(quantity));
    }

    @CircuitBreaker(name = "inventoryService", fallbackMethod = "fallback")
    public void undoConfirmedSale(Long productId, Integer quantity) {
        InventoryResponseDto responseDto = inventoryClient.undoConfirmedSale(productId, new StockOperationRequestDto(quantity));
    }

    public void fallback(Long productId, Integer quantity, Exception ex) {
        throw new ExternalServiceUnavailableException(
                "Inventory service unavailable for productId: " + productId,
                ex
        );
    }
}