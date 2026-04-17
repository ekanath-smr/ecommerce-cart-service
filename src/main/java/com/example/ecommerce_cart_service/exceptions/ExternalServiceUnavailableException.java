package com.example.ecommerce_cart_service.exceptions;

import lombok.Getter;

@Getter
public class ExternalServiceUnavailableException extends RuntimeException {
    private final String serviceName;
    private final String message;
    public ExternalServiceUnavailableException(String serviceName, Throwable cause) {
        super("Service unavailable: " + serviceName, cause);
        this.serviceName = serviceName;
        this.message = "Service unavailable: " + serviceName;
    }
}