package com.example.ecommerce_cart_service.clients.productClient;

import com.example.ecommerce_cart_service.clients.productClient.dtos.ProductDto;
import com.example.ecommerce_cart_service.exceptions.ExternalServiceUnavailableException;
import com.example.ecommerce_cart_service.exceptions.ProductNotFoundException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import io.github.resilience4j.retry.annotation.Retry;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Component
@Slf4j
public class ProductServiceClient {

    private final ProductClient productClient;

    public ProductServiceClient(ProductClient productClient) {
        this.productClient = productClient;
    }

    // ================= GET PRODUCT =================

    @Retry(name = "productService")
    @CircuitBreaker(name = "productService", fallbackMethod = "getProductFallback")
    public ProductDto getProduct(Long productId) {
        return productClient.getProductById(productId);
    }

    public ProductDto getProductFallback(Long productId, Throwable ex) {
        if (ex instanceof feign.FeignException.NotFound) {
            log.warn("Product not found in downstream service, productId={}", productId);
            throw new ProductNotFoundException(productId);
        }
        log.error("Product service failure: getProduct, productId={}, error={}",
                productId, ex.getMessage(), ex);
        throw new ExternalServiceUnavailableException(
                "Product service unavailable for productId: " + productId,
                ex
        );
    }

    // ================= VALIDATE PRODUCT =================

    @Retry(name = "productService")
    @CircuitBreaker(name = "productService", fallbackMethod = "validateProductFallback")
    public boolean validateProduct(Long productId) {
        return productClient.existsProductById(productId);
    }

    public boolean validateProductFallback(Long productId, Throwable ex) {
        if (ex instanceof feign.FeignException.NotFound) {
            log.warn("Product not found during validation, productId={}", productId);
            return false;
        }
        log.error("Product service failure: validateProduct, productId={}, error={}",
                productId, ex.getMessage(), ex);
        return false;
    }
}
