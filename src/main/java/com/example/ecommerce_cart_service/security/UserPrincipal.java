package com.example.ecommerce_cart_service.security;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Builder
@AllArgsConstructor
public class UserPrincipal {
    private Long userId;
    private String userName;
}
