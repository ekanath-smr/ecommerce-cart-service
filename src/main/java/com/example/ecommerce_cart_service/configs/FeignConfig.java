package com.example.ecommerce_cart_service.configs;

import feign.RequestInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

@Configuration
@Slf4j
public class FeignConfig {

    @Bean
    public RequestInterceptor requestInterceptor() {
        return requestTemplate -> {

            ServletRequestAttributes attrs =
                    (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();

            if (attrs == null) {
                log.debug("FeignInterceptor: No request context available");
                return;
            }

            String authHeader = attrs.getRequest().getHeader("Authorization");

            if (authHeader == null || authHeader.isBlank()) {
                log.warn("FeignInterceptor: Authorization header missing for downstream call");
                return;
            }

            requestTemplate.header("Authorization", authHeader);

            log.debug("FeignInterceptor: Authorization header propagated to Feign client");
        };
    }
}