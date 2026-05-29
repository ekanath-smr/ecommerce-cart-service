package com.example.ecommerce_cart_service.clients.productClient;

import com.example.ecommerce_cart_service.clients.productClient.dtos.ProductDto;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import jakarta.validation.constraints.Positive;

@FeignClient(
        name = "ECOMMERCE-PRODUCT-SERVICE",
        url = "${product.service.url:}"
)
public interface ProductClient {

    @GetMapping("/products/{productId}")
    ProductDto getProductById(@PathVariable @Positive Long productId);

    @GetMapping("/products/{productId}/exists")
    Boolean existsProductById(@PathVariable @Positive Long productId);
}
