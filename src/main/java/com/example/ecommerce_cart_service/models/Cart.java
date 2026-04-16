package com.example.ecommerce_cart_service.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(
        name = "carts",
        indexes = {
                @Index(name = "idx_cart_user", columnList = "userId")
        }
)
public class Cart extends BaseModel{

    @Column(nullable = false)
    private Long userId;

    @Enumerated(EnumType.STRING)
    private CartStatus status;

    @Builder.Default
    private Integer totalItems = 0;

    @Builder.Default
    @Column(precision = 12, scale = 2)
    private BigDecimal totalPrice = BigDecimal.ZERO;

    @Builder.Default
    @OneToMany(mappedBy = "cart", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<CartItem> items = new ArrayList<>();

}
