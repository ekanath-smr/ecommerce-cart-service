package com.example.ecommerce_cart_service.exceptions;

import lombok.Getter;

@Getter
public class ExternalServiceException extends RuntimeException {
    private final String serviceName;
    private final String message;
    public ExternalServiceException(String serviceName) {
        super("Service unavailable: " + serviceName);
        this.serviceName = serviceName;
        this.message = "Service unavailable: " + serviceName;
    }
}