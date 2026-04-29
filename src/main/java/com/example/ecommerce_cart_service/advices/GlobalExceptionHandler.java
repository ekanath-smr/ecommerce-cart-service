package com.example.ecommerce_cart_service.advices;

import com.example.ecommerce_cart_service.dtos.response.ErrorResponseDto;
import com.example.ecommerce_cart_service.exceptions.*;
import jakarta.servlet.http.HttpServletRequest;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
@Slf4j
public class GlobalExceptionHandler {

    // =========================
    // Optimistic Locking (Concurrency)
    // =========================
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponseDto> handleOptimisticLock(
            ObjectOptimisticLockingFailureException ex, HttpServletRequest request) {
        log.warn("Optimistic locking failure at {} - {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(
                HttpStatus.CONFLICT,
                "Optimistic Lock Failure",
                "Cart was updated by another request. Please retry."
        );
    }

    // =========================
    // Cart Exceptions
    // =========================
    @ExceptionHandler(CartAlreadyCheckedOutException.class)
    public ResponseEntity<ErrorResponseDto> handleCartCheckedOut(
            CartAlreadyCheckedOutException ex, HttpServletRequest request) {
        log.warn("Cart already checked out at {} - {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(
                HttpStatus.CONFLICT,
                "Cart Already Checked Out",
                ex.getMessage()
        );
    }

    @ExceptionHandler(CartItemNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleCartItemNotFound(
            CartItemNotFoundException ex, HttpServletRequest request) {
        log.warn("Cart item not found at {} - {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(
                HttpStatus.NOT_FOUND,
                "Cart Item Not Found",
                ex.getMessage()
        );
    }

    @ExceptionHandler(InvalidCartOperationException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidCartOperation(
            InvalidCartOperationException ex, HttpServletRequest request) {
        log.warn("Invalid cart operation at {} - {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid Cart Operation",
                ex.getMessage()
        );
    }

    @ExceptionHandler(ConcurrentCartUpdateException.class)
    public ResponseEntity<ErrorResponseDto> handleConcurrentUpdate(
            ConcurrentCartUpdateException ex, HttpServletRequest request) {
        log.warn("Concurrent cart update at {} - {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(
                HttpStatus.CONFLICT,
                "Concurrent Cart Update",
                ex.getMessage()
        );
    }

    // =========================
    // Product / Inventory Exceptions
    // =========================
    @ExceptionHandler(ProductNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleProductNotFound(
            ProductNotFoundException ex, HttpServletRequest request) {
        log.warn("Product not found at {} - {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(
                HttpStatus.NOT_FOUND,
                "Product Not Found",
                ex.getMessage()
        );
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponseDto> handleStock(
            InsufficientStockException ex, HttpServletRequest request) {
        log.warn("Insufficient stock at {} - {}", request.getRequestURI(), ex.getMessage());
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Insufficient Stock",
                ex.getMessage()
        );
    }

    @ExceptionHandler(ExternalServiceUnavailableException.class)
    public ResponseEntity<ErrorResponseDto> handleExternalService(
            ExternalServiceUnavailableException ex, HttpServletRequest request) {
        log.error("External service unavailable at {} - {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildResponse(
                HttpStatus.SERVICE_UNAVAILABLE,
                "External Service Failure",
                ex.getMessage()
        );
    }

    // =========================
    // Generic fallback
    // =========================
    @ExceptionHandler(Exception.class)
    public ResponseEntity<ErrorResponseDto> handleGeneric(
            Exception ex, HttpServletRequest request) {
        log.error("Unhandled exception at {} - {}", request.getRequestURI(), ex.getMessage(), ex);
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                "Something went wrong. Please contact support."
        );
    }

    // =========================
    // Helper
    // =========================
    private ResponseEntity<ErrorResponseDto> buildResponse(
            HttpStatus status, String error, String message) {
        ErrorResponseDto response = ErrorResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(error)
                .message(message)
                .build();
        return new ResponseEntity<>(response, status);
    }
}