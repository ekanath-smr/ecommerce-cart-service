package com.example.ecommerce_cart_service.controllers;

import com.example.ecommerce_cart_service.dtos.request.AddToCartRequestDto;
import com.example.ecommerce_cart_service.dtos.request.UpdateCartItemRequestDto;
import com.example.ecommerce_cart_service.dtos.response.CartResponseDto;
import com.example.ecommerce_cart_service.security.UserPrincipal;
import com.example.ecommerce_cart_service.services.CartService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/cart")
public class CartController {

    private final CartService cartService;

    public CartController(CartService cartService) {
        this.cartService = cartService;
    }

    @PreAuthorize("hasRole('USER')")
    @GetMapping
    public ResponseEntity<CartResponseDto> getCart(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(cartService.getCart(principal.getUserId()));
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/items")
    public ResponseEntity<CartResponseDto> addItemToCart(
            @AuthenticationPrincipal UserPrincipal principal, @Valid @RequestBody AddToCartRequestDto request) {
        return ResponseEntity.ok(cartService.addItemToCart(principal.getUserId(), request));
    }

    @PreAuthorize("hasRole('USER')")
    @PutMapping("/items/{productId}")
    public ResponseEntity<CartResponseDto> updateItemInCart(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long productId,
            @Valid @RequestBody UpdateCartItemRequestDto request) {
        return ResponseEntity.ok(cartService.updateItemInCart(principal.getUserId(), productId, request));
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping("/items/{productId}")
    public ResponseEntity<CartResponseDto> removeItemFromCart(
            @AuthenticationPrincipal UserPrincipal principal, @PathVariable Long productId) {
        return ResponseEntity.ok(cartService.removeItemFromCart(principal.getUserId(), productId));
    }

    @PreAuthorize("hasRole('USER')")
    @DeleteMapping
    public ResponseEntity<CartResponseDto> clearCart(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(cartService.clearCart(principal.getUserId()));
    }

    @PreAuthorize("hasRole('USER')")
    @PostMapping("/checkout")
    public ResponseEntity<CartResponseDto> checkoutCart(@AuthenticationPrincipal UserPrincipal principal) {
        return ResponseEntity.ok(cartService.checkoutCart(principal.getUserId()));
    }
}