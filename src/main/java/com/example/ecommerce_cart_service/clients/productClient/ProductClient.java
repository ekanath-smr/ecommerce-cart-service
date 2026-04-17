package com.example.ecommerce_cart_service.clients.productClient;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import jakarta.validation.constraints.Positive;

@FeignClient(
        name = "product-service",
        url = "${product.service.url}"
)
public interface ProductClient {
    @GetMapping("/products/{productId}")
    ProductDto getProductById(@PathVariable @Positive Long productId);
}
