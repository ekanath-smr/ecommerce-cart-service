package com.example.ecommerce_cart_service.clients.inventoryClient;

import com.example.ecommerce_cart_service.exceptions.ExternalServiceUnavailableException;
import com.example.ecommerce_cart_service.exceptions.InsufficientStockException;
import feign.FeignException;
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
        int availableStock = inventoryClient.getAvailableStock(productId).getAvailableStock();
//        Boolean inStock = inventoryClient.isInStock(productId, quantity);
        if (availableStock < quantity) {
            throw new InsufficientStockException(productId, quantity, availableStock);
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
        if(ex instanceof FeignException.NotFound) {
            throw new InsufficientStockException(productId, quantity, 0);
        } else if(ex instanceof InsufficientStockException) {
            throw new InsufficientStockException(productId, quantity, ((InsufficientStockException) ex).getAvailable());
        }
        throw new ExternalServiceUnavailableException(
                "Inventory service unavailable for productId: " + productId,
                ex
        );
    }
}