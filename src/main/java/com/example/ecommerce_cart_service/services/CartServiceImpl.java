package com.example.ecommerce_cart_service.services;

import com.example.ecommerce_cart_service.clients.inventoryClient.InventoryServiceClient;
import com.example.ecommerce_cart_service.clients.inventoryClient.dtos.ValidateStockResponseDto;
import com.example.ecommerce_cart_service.clients.orderClient.OrderServiceClient;
import com.example.ecommerce_cart_service.clients.orderClient.dtos.CreateOrderRequestDto;
import com.example.ecommerce_cart_service.clients.orderClient.dtos.OrderItemRequestDto;
import com.example.ecommerce_cart_service.clients.orderClient.dtos.OrderResponseDto;
import com.example.ecommerce_cart_service.clients.productClient.dtos.ProductDto;
import com.example.ecommerce_cart_service.clients.productClient.ProductServiceClient;
import com.example.ecommerce_cart_service.dtos.request.AddToCartRequestDto;
import com.example.ecommerce_cart_service.dtos.request.UpdateCartItemRequestDto;
import com.example.ecommerce_cart_service.dtos.response.CartResponseDto;
import com.example.ecommerce_cart_service.exceptions.*;
import com.example.ecommerce_cart_service.mappers.CartMapper;
import com.example.ecommerce_cart_service.models.Cart;
import com.example.ecommerce_cart_service.models.CartItem;
import com.example.ecommerce_cart_service.models.CartStatus;
import com.example.ecommerce_cart_service.repositories.CartItemRepository;
import com.example.ecommerce_cart_service.repositories.CartRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.retry.annotation.Backoff;
import org.springframework.retry.annotation.Retryable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.Optional;

@Service
@Slf4j
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final CartItemRepository cartItemRepository;
    private final ProductServiceClient productServiceClient;
    private final InventoryServiceClient inventoryServiceClient;
    private final OrderServiceClient orderServiceClient;

    public CartServiceImpl(CartRepository cartRepository, CartItemRepository cartItemRepository, ProductServiceClient productServiceClient,
                           InventoryServiceClient inventoryServiceClient, OrderServiceClient orderServiceClient) {
        this.cartRepository = cartRepository;
        this.cartItemRepository = cartItemRepository;
        this.productServiceClient = productServiceClient;
        this.inventoryServiceClient = inventoryServiceClient;
        this.orderServiceClient = orderServiceClient;
    }

    // =========================
    // GET CART
    // =========================
    @Override
    @Transactional
    public CartResponseDto getCart(Long userId) {
        log.info("Fetching cart for userId={}", userId);
        Cart cart = getOrCreateActiveCart(userId);
        return CartMapper.mapToCartResponse(cart);
    }

    // =========================
    // ADD ITEM TO CART
    // =========================
    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 2,
            backoff = @Backoff(delay = 100)
    )
    @Override
    @Transactional
    public CartResponseDto addItemToCart(Long userId, AddToCartRequestDto request) {
        log.info("Adding item to cart for userId={}, productId={}, quantity={}",
                userId, request.getProductId(), request.getQuantity());
        Cart cart = getOrCreateActiveCart(userId);
        validateCartIsActive(cart);

        Long productId = request.getProductId();
        ProductDto productDto = productServiceClient.getProduct(productId);
        if (productDto == null) {
            log.error("Product not found for productId={}", productId);
            throw new ProductNotFoundException(productId);
        }

//        CartItem cartItem = cartItemRepository
//                .findByCartIdAndProductId(cart.getId(), productId)
//                .orElse(null);
        Optional<CartItem> cartItemOptional = cart.getItems()
                .stream()
                .filter(i -> i.getProductId().equals(productId))
                .findFirst();
        CartItem cartItem;
        if (cartItemOptional.isPresent()) {
            cartItem = cartItemOptional.get();
            int newQuantity = cartItem.getQuantity() + request.getQuantity();
            log.info("Updating existing cart item. productId={}, newQuantity={}", productId, newQuantity);
            validateStock(productId, newQuantity);
            cartItem.setQuantity(newQuantity);
            cartItem.setSubtotal(
                    cartItem.getPriceSnapshot()
                            .multiply(BigDecimal.valueOf(newQuantity))
            );
        } else {
            log.info("Creating new cart item for productId={}", productId);
            validateStock(productId, request.getQuantity());
            String name = productDto.getTitle();
            BigDecimal price = productDto.getPrice();
            cartItem = CartItem.builder()
                    .productId(productId)
                    .productNameSnapshot(name)
                    .priceSnapshot(price)
                    .quantity(request.getQuantity())
                    .subtotal(price.multiply(BigDecimal.valueOf(request.getQuantity())))
                    .cart(cart)
                    .build();
            cart.getItems().add(cartItem);
        }
        recalculateCart(cart);
//        cartRepository.save(cart);
        log.info("Cart updated successfully for userId={}", userId);
        return CartMapper.mapToCartResponse(cart);
    }

    // =========================
    // UPDATE ITEM
    // =========================
    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 2,
            backoff = @Backoff(delay = 100)
    )
    @Override
    @Transactional
    public CartResponseDto updateItemInCart(Long userId, Long productId, UpdateCartItemRequestDto request) {
        log.info("Updating cart item for userId={}, productId={}, quantity={}",
                userId, productId, request.getQuantity());
        Cart cart = getOrCreateActiveCart(userId);
        validateCartIsActive(cart);
        CartItem cartItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> {
                    log.error("Cart item not found for productId={}", productId);
                    return new CartItemNotFoundException(productId);
                });
        if (request.getQuantity() == 0) {
            log.info("Removing cart item due to quantity=0 for productId={}", productId);
            cart.getItems().remove(cartItem);
            cartItemRepository.delete(cartItem);
        } else {
            validateStock(productId, request.getQuantity());
            cartItem.setQuantity(request.getQuantity());
            cartItem.setSubtotal(
                    cartItem.getPriceSnapshot()
                            .multiply(BigDecimal.valueOf(cartItem.getQuantity()))
            );
        }
        recalculateCart(cart);
        cartRepository.save(cart);
        log.info("Cart item updated successfully for userId={}", userId);
        return CartMapper.mapToCartResponse(cart);
    }

    // =========================
    // REMOVE ITEM
    // =========================
    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 2,
            backoff = @Backoff(delay = 100)
    )
    @Override
    @Transactional
    public CartResponseDto removeItemFromCart(Long userId, Long productId) {
        log.info("Removing item from cart for userId={}, productId={}", userId, productId);
        Cart cart = getOrCreateActiveCart(userId);
        validateCartIsActive(cart);

        CartItem cartItem = cartItemRepository
                .findByCartIdAndProductId(cart.getId(), productId)
                .orElseThrow(() -> {
                    log.error("Cart item not found for removal, productId={}", productId);
                    return new CartItemNotFoundException(productId);
                });

        cart.getItems().remove(cartItem);
        cartItemRepository.delete(cartItem);
        recalculateCart(cart);
        cartRepository.save(cart);
        log.info("Cart item removed successfully for userId={}", userId);
        return CartMapper.mapToCartResponse(cart);
    }

    // =========================
    // CLEAR CART
    // =========================
    @Retryable(
            retryFor = ObjectOptimisticLockingFailureException.class,
            maxAttempts = 2,
            backoff = @Backoff(delay = 100)
    )
    @Override
    @Transactional
    public CartResponseDto clearCart(Long userId) {
        log.info("Clearing cart for userId={}", userId);
        Cart cart = getOrCreateActiveCart(userId);
        validateCartIsActive(cart);
        cart.getItems().clear();
        recalculateCart(cart);
        cartRepository.save(cart);
        log.info("Cart cleared successfully for userId={}", userId);
        return CartMapper.mapToCartResponse(cart);
    }

    // =========================
    // CHECKOUT CART
    // =========================
    //  "In production, I would not rely on synchronous compensation (Saga Pattern with Full compensation coverage).
    //  I would publish events and use retry mechanisms (Kafka + DLQ) to guarantee eventual consistency."
    // “I avoided using automatic retries for checkout because it involves non-idempotent distributed operations like stock reservation and confirmation.
    // Instead, I implemented explicit compensating transactions using the Saga pattern.
    // In production, I would move this to an event-driven model with idempotent operations and retry via messaging systems.”
    @Override
    @Transactional
    public CartResponseDto checkoutCart(Long userId) {
        log.info("Checkout started for userId={}", userId);
        Cart cart = getOrCreateActiveCart(userId);
        validateCartIsActive(cart);
        if (cart.getItems().isEmpty()) {
            log.error("Checkout failed: cart is empty for userId={}", userId);
            throw new InvalidCartOperationException("Cannot checkout empty cart");
        }
        CreateOrderRequestDto orderRequest = CreateOrderRequestDto.builder()
                .userId(userId)
                .items(
                        cart.getItems()
                                .stream()
                                .map(item -> new OrderItemRequestDto(item.getProductId(), item.getQuantity()))
                                .toList()
                )
                .build();

        String idempotencyKey = "cart-" + cart.getId() + "-v" + cart.getVersion();

        OrderResponseDto orderResponse;
        try {
            orderResponse = orderServiceClient.createOrder(orderRequest, idempotencyKey);
        } catch (Exception ex) {
            log.error("Checkout failed while creating order for cartId={}", cart.getId(), ex);
            if(ex instanceof InsufficientStockException) {
                throw ex;
            }
            throw new ExternalServiceUnavailableException(
                    "Order service unavailable for idempotencyKey: " + idempotencyKey,
                    ex
            );
        }

        cart.getItems().clear();
        cart.setTotalItems(0);
        cart.setTotalPrice(BigDecimal.ZERO);
        cartRepository.save(cart);

        log.info("Checkout successful for userId={}, cartId={}, orderId={}",
                userId, cart.getId(), orderResponse.getOrderId());
        return CartMapper.mapToCartResponse(cart);
    }

    // =========================
    // INTERNAL HELPER METHODS
    // =========================

    // @Synchronized is good for single instance, but for distributed system we need to use distributed locking.
    // never relay on synchronized locks on distributed systems, always relay on database constraints + retries.
    // The best approach is to create unique constrain (userId, CartStatus),
    // but this will restrict user to have only one checkedOut cart, or only one expiredCart, which i don't want.
    // If we use postgreSql, we can implement Partial Unique index and the problem is solved, but can't do that in mysql (limitation).
    private Cart getOrCreateActiveCart(Long userId) {
        return cartRepository
                .findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseGet(() -> {
                    log.info("Creating new cart for userId={}", userId);
                    return cartRepository.save(
                            Cart.builder()
                                    .userId(userId)
                                    .status(CartStatus.ACTIVE)
                                    .totalItems(0)
                                    .totalPrice(BigDecimal.ZERO)
                                    .build()
                    );
                });
    }

    private Cart getActiveCart(Long userId) {
        return cartRepository
                .findByUserIdAndStatus(userId, CartStatus.ACTIVE)
                .orElseThrow(() -> new ActiveCartNotFoundException(userId));
    }

    private void validateCartIsActive(Cart cart) {
        if (cart.getStatus() == CartStatus.CHECKED_OUT) {
            log.error("Attempt to modify CHECKED_OUT cartId={}", cart.getId());
            throw new CartAlreadyCheckedOutException(cart.getId());
        }
        if (cart.getStatus() == CartStatus.EXPIRED) {
            log.error("Attempt to modify EXPIRED cartId={}", cart.getId());
            throw new InvalidCartOperationException("Cannot modify expired cart");
        }
    }

    private void recalculateCart(Cart cart) {
        int totalItems = 0;
        BigDecimal totalPrice = BigDecimal.ZERO;
        for (CartItem item : cart.getItems()) {
            totalItems += item.getQuantity();
            totalPrice = totalPrice.add(item.getSubtotal());
        }
        cart.setTotalItems(totalItems);
        cart.setTotalPrice(totalPrice);
    }

    private void validateStock(Long productId, Integer newQuantity) {
        ValidateStockResponseDto validateStockResponseDto;
        validateStockResponseDto = inventoryServiceClient.validateStock(productId, newQuantity);
        if(!validateStockResponseDto.getIsStockAvailable()) {
            throw new InsufficientStockException(validateStockResponseDto.getProductId(),
                    validateStockResponseDto.getRequestedQuantity(), validateStockResponseDto.getAvailableQuantity());
        }
    }

}