package com.example.ecommerce_cart_service.clients.inventoryClient;

import com.example.ecommerce_cart_service.clients.inventoryClient.dtos.InventoryResponseDto;
import com.example.ecommerce_cart_service.clients.inventoryClient.dtos.StockOperationRequestDto;
import com.example.ecommerce_cart_service.clients.inventoryClient.dtos.ValidateStockResponseDto;
import com.example.ecommerce_cart_service.exceptions.ExternalServiceUnavailableException;
import com.example.ecommerce_cart_service.exceptions.InsufficientStockException;
import feign.FeignException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class InventoryServiceClient {

    private final InventoryClient inventoryClient;

    public InventoryServiceClient(InventoryClient inventoryClient) {
        this.inventoryClient = inventoryClient;
    }

    @Retry(name = "inventoryService")
    @CircuitBreaker(name = "inventoryService", fallbackMethod = "fallbackInventory")
    public InventoryResponseDto reserveStock(Long productId, Integer quantity, Long orderId) {
        return inventoryClient.reserveStock(productId, new StockOperationRequestDto(quantity, getReferenceId(orderId)));
    }

    @Retry(name = "inventoryService")
    @CircuitBreaker(name = "inventoryService", fallbackMethod = "fallbackInventory")
    public InventoryResponseDto releaseStock(Long productId, Integer quantity, Long orderId) {
        return inventoryClient.releaseStock(productId, new StockOperationRequestDto(quantity, getReferenceId(orderId)));
    }

    @Retry(name = "inventoryService")
    @CircuitBreaker(name = "inventoryService", fallbackMethod = "fallbackInventory")
    public InventoryResponseDto confirmSale(Long productId, Integer quantity, Long orderId) {
        return inventoryClient.confirmSale(productId, new StockOperationRequestDto(quantity, getReferenceId(orderId)));
    }

    @Retry(name = "inventoryService")
    @CircuitBreaker(name = "inventoryService", fallbackMethod = "fallbackInventory")
    public InventoryResponseDto undoConfirmedSale(Long productId, Integer quantity, Long orderId) {
        return inventoryClient.undoConfirmedSale(productId, new StockOperationRequestDto(quantity, getReferenceId(orderId)));
    }

    @Retry(name = "inventoryService")
    @CircuitBreaker(name = "inventoryService", fallbackMethod = "fallbackValidate")
    public ValidateStockResponseDto validateStock(Long productId, Integer quantity) {
        int availableQuantity = inventoryClient.getAvailableStock(productId).getAvailableStock();
        return ValidateStockResponseDto.builder()
                .productId(productId)
                .requestedQuantity(quantity)
                .availableQuantity(availableQuantity)
                .isStockAvailable(availableQuantity >= quantity)
                .build();
    }

    public InventoryResponseDto fallbackInventory(Long productId, Integer quantity, Throwable ex) {
        log.error("Inventory service failure for 'inventoryOperationCall' productId={} quantity={}", productId, quantity, ex);
        throw new ExternalServiceUnavailableException(
                "Inventory service unavailable during inventory stock operation for productId: " + productId,
                ex
        );
    }

    public ValidateStockResponseDto fallbackValidate(Long productId, Integer quantity, Throwable ex) {
        log.error("Inventory service failure for 'validateStockCall' productId={} quantity={}", productId, quantity, ex);
        throw new ExternalServiceUnavailableException(
                "Inventory service unavailable during stock validation for productId: " + productId,
                ex
        );
    }

    private String getReferenceId(Long orderId) {
        String referenceId;
        if(orderId == null) {
            referenceId = null;
        } else {
            referenceId = orderId.toString();
        }
        return referenceId;
    }

//    public void fallback(Long productId, Integer quantity, Throwable ex) {
////        if(ex instanceof FeignException.NotFound) {
////            throw new InsufficientStockException(productId, quantity, 0);
////        } else if(ex instanceof InsufficientStockException) {
////            throw new InsufficientStockException(productId, quantity, ((InsufficientStockException) ex).getAvailable());
////        }
//        throw new InventoryServiceException(
//                "Inventory service unavailable for productId: " + productId,
//                ex
//        );
//    }

}