package com.example.ecommerce_cart_service.services;

import com.example.ecommerce_cart_service.clients.inventoryClient.InventoryServiceClient;
import com.example.ecommerce_cart_service.clients.inventoryClient.dtos.ValidateStockResponseDto;
import com.example.ecommerce_cart_service.clients.orderClient.OrderServiceClient;
import com.example.ecommerce_cart_service.clients.orderClient.dtos.CreateOrderRequestDto;
import com.example.ecommerce_cart_service.clients.orderClient.dtos.OrderResponseDto;
import com.example.ecommerce_cart_service.clients.productClient.ProductServiceClient;
import com.example.ecommerce_cart_service.clients.productClient.dtos.ProductDto;
import com.example.ecommerce_cart_service.dtos.request.AddToCartRequestDto;
import com.example.ecommerce_cart_service.dtos.request.UpdateCartItemRequestDto;
import com.example.ecommerce_cart_service.exceptions.CartItemNotFoundException;
import com.example.ecommerce_cart_service.exceptions.InsufficientStockException;
import com.example.ecommerce_cart_service.models.Cart;
import com.example.ecommerce_cart_service.models.CartItem;
import com.example.ecommerce_cart_service.models.CartStatus;
import com.example.ecommerce_cart_service.repositories.CartItemRepository;
import com.example.ecommerce_cart_service.repositories.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock private CartRepository cartRepository;
    @Mock private CartItemRepository cartItemRepository;
    @Mock private ProductServiceClient productServiceClient;
    @Mock private InventoryServiceClient inventoryServiceClient;
    @Mock private OrderServiceClient orderServiceClient;

    @InjectMocks
    private CartServiceImpl cartService;

    private Cart cart;

    @BeforeEach
    void setUp() {
        cart = Cart.builder()
                .id(1L)
                .userId(100L)
                .status(CartStatus.ACTIVE)
                .totalItems(0)
                .totalPrice(BigDecimal.ZERO)
                .build();
    }

    // =========================
    // GET CART
    // =========================
    @Test
    void getCart_shouldReturnCart() {
        when(cartRepository.findByUserIdAndStatus(100L, CartStatus.ACTIVE))
                .thenReturn(Optional.of(cart));

        var result = cartService.getCart(100L);

        assertNotNull(result);
        verify(cartRepository).findByUserIdAndStatus(100L, CartStatus.ACTIVE);
    }

    // =========================
    // ADD ITEM
    // =========================
    @Test
    void addItem_shouldAddNewItem() {
        AddToCartRequestDto request = new AddToCartRequestDto(10L, 2);

        ProductDto product = new ProductDto();
        product.setId(10L);
        product.setTitle("Phone");
        product.setPrice(BigDecimal.valueOf(500));

        ValidateStockResponseDto stock = new ValidateStockResponseDto();
        stock.setIsStockAvailable(true);
        stock.setProductId(10L);
        stock.setAvailableQuantity(10);
        stock.setRequestedQuantity(2);

        when(cartRepository.findByUserIdAndStatus(100L, CartStatus.ACTIVE))
                .thenReturn(Optional.of(cart));

        when(productServiceClient.getProduct(10L)).thenReturn(product);
        when(inventoryServiceClient.validateStock(10L, 2)).thenReturn(stock);

        when(cartRepository.save(any())).thenReturn(cart);

        var result = cartService.addItemToCart(100L, request);

        assertNotNull(result);
        verify(cartRepository).save(any(Cart.class));
    }

    // =========================
    // UPDATE ITEM
    // =========================
    @Test
    void updateItem_shouldThrowIfItemNotFound() {
        UpdateCartItemRequestDto request = new UpdateCartItemRequestDto();
        request.setQuantity(3);

        when(cartRepository.findByUserIdAndStatus(100L, CartStatus.ACTIVE))
                .thenReturn(Optional.of(cart));

        when(cartItemRepository.findByCartIdAndProductId(1L, 10L))
                .thenReturn(Optional.empty());

        assertThrows(CartItemNotFoundException.class,
                () -> cartService.updateItemInCart(100L, 10L, request));
    }

    // =========================
    // REMOVE ITEM
    // =========================
    @Test
    void removeItem_shouldRemoveSuccessfully() {
        CartItem item = CartItem.builder()
                .id(1L)
                .productId(10L)
                .quantity(2)
                .subtotal(BigDecimal.valueOf(1000))
                .cart(cart)
                .build();

        cart.getItems().add(item);

        when(cartRepository.findByUserIdAndStatus(100L, CartStatus.ACTIVE))
                .thenReturn(Optional.of(cart));

        when(cartItemRepository.findByCartIdAndProductId(1L, 10L))
                .thenReturn(Optional.of(item));

        when(cartRepository.save(any())).thenReturn(cart);

        var result = cartService.removeItemFromCart(100L, 10L);

        assertNotNull(result);
        verify(cartItemRepository).delete(item);
    }

    // =========================
    // CLEAR CART
    // =========================
    @Test
    void clearCart_shouldEmptyCart() {
        when(cartRepository.findByUserIdAndStatus(100L, CartStatus.ACTIVE))
                .thenReturn(Optional.of(cart));

        when(cartRepository.save(any())).thenReturn(cart);

        var result = cartService.clearCart(100L);

        assertNotNull(result);
        assertTrue(cart.getItems().isEmpty());
    }

    // =========================
    // CHECKOUT CART
    // =========================
    @Test
    void checkout_shouldFailIfOrderServiceThrowsInsufficientStock() {

        Long userId = 100L;

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUserId(userId);
        cart.setVersion(1L);
        cart.setItems(new ArrayList<>()); // IMPORTANT: mutable list

        CartItem item = new CartItem();
        item.setProductId(10L);
        item.setQuantity(5);
        cart.getItems().add(item);

        when(cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE))
                .thenReturn(Optional.of(cart));

        when(orderServiceClient.createOrder(any(CreateOrderRequestDto.class), anyString()))
                .thenThrow(new InsufficientStockException("Insufficient stock"));

        // ACT + ASSERT
        assertThrows(InsufficientStockException.class,
                () -> cartService.checkoutCart(userId));

        // VERIFY order service called
        verify(orderServiceClient, times(1))
                .createOrder(any(CreateOrderRequestDto.class), anyString());

        // VERIFY cart NOT cleared (because failure happened before success flow completes)
        verify(cartRepository, never()).save(any());
    }

    @Test
    void checkout_shouldSuccessAndClearCart() {

        Long userId = 100L;

        Cart cart = new Cart();
        cart.setId(1L);
        cart.setUserId(userId);
        cart.setVersion(1L);
        cart.setItems(new ArrayList<>());

        CartItem item = new CartItem();
        item.setProductId(10L);
        item.setQuantity(2);
        cart.getItems().add(item);

        OrderResponseDto response = new OrderResponseDto();
        response.setOrderId(999L);

        when(cartRepository.findByUserIdAndStatus(userId, CartStatus.ACTIVE))
                .thenReturn(Optional.of(cart));

        when(orderServiceClient.createOrder(any(), anyString()))
                .thenReturn(response);

        when(cartRepository.save(any())).thenReturn(cart);

        var result = cartService.checkoutCart(userId);

        assertNotNull(result);

        verify(orderServiceClient, times(1))
                .createOrder(any(CreateOrderRequestDto.class), anyString());

        verify(cartRepository, times(1))
                .save(any(Cart.class));

        assertTrue(cart.getItems().isEmpty());
    }
}