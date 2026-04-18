package com.example.ecommerce_cart_service.advices;

import com.example.ecommerce_cart_service.dtos.response.ErrorResponseDto;
import com.example.ecommerce_cart_service.exceptions.*;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.orm.ObjectOptimisticLockingFailureException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;

@RestControllerAdvice
public class GlobalExceptionHandler {

    // =========================
    // Optimistic Locking (Concurrency)
    // =========================
    @ExceptionHandler(ObjectOptimisticLockingFailureException.class)
    public ResponseEntity<ErrorResponseDto> handleOptimisticLock(ObjectOptimisticLockingFailureException ex) {
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
    public ResponseEntity<ErrorResponseDto> handleCartCheckedOut(CartAlreadyCheckedOutException ex) {
        return buildResponse(
                HttpStatus.CONFLICT,
                "Cart Already Checked Out",
                ex.getMessage()
        );
    }

    @ExceptionHandler(CartItemNotFoundException.class)
    public ResponseEntity<ErrorResponseDto> handleCartItemNotFound(CartItemNotFoundException ex) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                "Cart Item Not Found",
                ex.getMessage()
        );
    }

    @ExceptionHandler(InvalidCartOperationException.class)
    public ResponseEntity<ErrorResponseDto> handleInvalidCartOperation(InvalidCartOperationException ex) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Invalid Cart Operation",
                ex.getMessage()
        );
    }

    @ExceptionHandler(ConcurrentCartUpdateException.class)
    public ResponseEntity<ErrorResponseDto> handleConcurrentUpdate(ConcurrentCartUpdateException ex) {
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
    public ResponseEntity<ErrorResponseDto> handleProductNotFound(ProductNotFoundException ex) {
        return buildResponse(
                HttpStatus.NOT_FOUND,
                "Product Not Found",
                ex.getMessage()
        );
    }

    @ExceptionHandler(InsufficientStockException.class)
    public ResponseEntity<ErrorResponseDto> handleStock(InsufficientStockException ex) {
        return buildResponse(
                HttpStatus.BAD_REQUEST,
                "Insufficient Stock",
                ex.getMessage()
        );
    }

    @ExceptionHandler(ExternalServiceUnavailableException.class)
    public ResponseEntity<ErrorResponseDto> handleExternalService(ExternalServiceUnavailableException ex) {
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
    public ResponseEntity<ErrorResponseDto> handleGeneric(Exception ex) {
        return buildResponse(
                HttpStatus.INTERNAL_SERVER_ERROR,
                "Internal Server Error",
                ex.getMessage()
        );
    }

    // =========================
    // Helper
    // =========================
    private ResponseEntity<ErrorResponseDto> buildResponse(
            HttpStatus status,
            String error,
            String message
    ) {
        ErrorResponseDto response = ErrorResponseDto.builder()
                .timestamp(LocalDateTime.now())
                .status(status.value())
                .error(error)
                .message(message)
                .build();

        return new ResponseEntity<>(response, status);
    }
}