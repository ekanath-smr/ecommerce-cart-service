package com.example.ecommerce_cart_service.clients.inventoryClient;

import com.example.ecommerce_cart_service.configs.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.*;

@FeignClient(
        name = "inventory-service",
        url = "${inventory.service.url}",
        configuration = FeignConfig.class
)
public interface InventoryClient {

    // Check stock availability
    @GetMapping("/inventory/{productId}/in-stock")
    Boolean isInStock(@PathVariable Long productId, @RequestParam Integer quantity);

    // Get stock availability
    @GetMapping("/inventory/{productId}/get-stock")
    StockAvailabilityResponseDto getAvailableStock(@PathVariable Long productId);

    // Reserve stock (during checkout)
    @PostMapping("/inventory/{productId}/reserve")
    InventoryResponseDto reserveStock(@PathVariable Long productId, @RequestBody StockOperationRequestDto request);

    // Release stock (if checkout fails)
    @PostMapping("/inventory/{productId}/release")
    InventoryResponseDto releaseStock(@PathVariable Long productId, @RequestBody StockOperationRequestDto request);

    // Confirm sale (after payment success)
    @PostMapping("/inventory/{productId}/confirm-sale")
    InventoryResponseDto confirmSale(@PathVariable Long productId, @RequestBody StockOperationRequestDto request);

    @PostMapping("/{productId}/undo-sale")
    InventoryResponseDto undoConfirmedSale(@PathVariable Long productId, @RequestBody StockOperationRequestDto request);
}