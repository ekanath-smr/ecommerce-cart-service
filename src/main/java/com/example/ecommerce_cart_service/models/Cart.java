package com.example.ecommerce_cart_service.models;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
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
        },
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"userId", "isActive"})
        }
)
public class Cart extends BaseModel{

    @Column(nullable = false)
    private Long userId;

    @Column(nullable = false)
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

//    “I use optimistic locking with a version field in the Cart entity to prevent lost updates.
//    If concurrent modification happens, one request fails and can be retried.”
    @Version
    private Long version;

    // “To ensure only one active cart per user in MySQL, I introduced an isActive flag and enforced a unique constraint
    // on (userId, isActive). This avoids the limitation of missing partial indexes in MySQL while still allowing multiple historical carts.”
    // “I used optimistic locking with Spring Retry so concurrent cart updates automatically retry up to 2 times before failing.”
    @Builder.Default
    @Column(nullable = false)
    private Boolean isActive = true;

}
