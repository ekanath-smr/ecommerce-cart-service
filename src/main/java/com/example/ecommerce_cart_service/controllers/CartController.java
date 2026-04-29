package com.example.ecommerce_cart_service.controllers;

import com.example.ecommerce_cart_service.dtos.request.AddToCartRequestDto;
import com.example.ecommerce_cart_service.dtos.request.UpdateCartItemRequestDto;
import com.example.ecommerce_cart_service.dtos.response.CartResponseDto;
import com.example.ecommerce_cart_service.security.UserPrincipal;
import com.example.ecommerce_cart_service.services.CartService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
@Tag(name = "Cart APIs", description = "Operations related to user shopping cart")
@Slf4j
@SecurityRequirement(name = "BearerAuth")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @Operation(summary = "Get current user's cart")
    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public ResponseEntity<CartResponseDto> getCart(@AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal.getUserId();
        log.info("GET /cart - userId={}", userId);
        return ResponseEntity.ok(cartService.getCart(userId));
    }

    @Operation(summary = "Add item to cart")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/items")
    public ResponseEntity<CartResponseDto> addItemToCart(
            @AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody AddToCartRequestDto request) {
        Long userId = principal.getUserId();
        log.info("POST /cart/items - userId={}, productId={}, quantity={}",
                userId, request.getProductId(), request.getQuantity());
        return ResponseEntity.ok(cartService.addItemToCart(userId, request));
    }

    @Operation(summary = "Update item quantity in cart")
    @PreAuthorize("hasRole('USER')")
    @PutMapping("/items/{productId}")
    public ResponseEntity<CartResponseDto> updateItemInCart(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long productId,
            @Valid @RequestBody UpdateCartItemRequestDto request) {
        Long userId = principal.getUserId();
        log.info("PUT /cart/items/{} - userId={}, quantity={}",
                productId, userId, request.getQuantity());
        return ResponseEntity.ok(
                cartService.updateItemInCart(userId, productId, request)
        );
    }

    @Operation(summary = "Remove item from cart")
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponseDto> removeItemFromCart(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long productId) {
        Long userId = principal.getUserId();
        log.info("DELETE /cart/items/{} - userId={}", productId, userId);
        return ResponseEntity.ok(
                cartService.removeItemFromCart(userId, productId)
        );
    }

    @Operation(summary = "Clear entire cart")
    @PreAuthorize("hasRole('USER')")
    @DeleteMapping
    public ResponseEntity<CartResponseDto> clearCart(@AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal.getUserId();
        log.info("DELETE /cart - userId={}", userId);
        return ResponseEntity.ok(cartService.clearCart(userId));
    }

    @Operation(summary = "Checkout cart")
    @PreAuthorize("hasRole('USER')")
    @PostMapping("/checkout")
    public ResponseEntity<CartResponseDto> checkoutCart(@AuthenticationPrincipal UserPrincipal principal) {
        Long userId = principal.getUserId();
        log.info("POST /cart/checkout - userId={}", userId);
        return ResponseEntity.ok(cartService.checkoutCart(userId));
    }
}