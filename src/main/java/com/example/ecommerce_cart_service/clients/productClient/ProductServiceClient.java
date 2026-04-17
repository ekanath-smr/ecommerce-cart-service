package com.example.ecommerce_cart_service.clients.productClient;

import com.example.ecommerce_cart_service.exceptions.ExternalServiceUnavailableException;
import com.example.ecommerce_cart_service.exceptions.ProductNotFoundException;
import io.github.resilience4j.circuitbreaker.annotation.CircuitBreaker;
import org.springframework.stereotype.Component;

@Component
public class ProductServiceClient {

    private final ProductClient productClient;

    public ProductServiceClient(ProductClient productClient) {
        this.productClient = productClient;
    }

    @CircuitBreaker(name = "productService", fallbackMethod = "fallback")
    public ProductDto getProduct(Long productId) {
        return productClient.getProductById(productId);
    }

    public ProductDto fallback(Long productId, Exception ex) {
        if (ex instanceof feign.FeignException.NotFound) {
            throw new ProductNotFoundException(productId);
        }
        throw new ExternalServiceUnavailableException(
                "Product service unavailable for productId: " + productId,
                ex
        );
    }
}
