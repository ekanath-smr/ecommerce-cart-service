package com.example.ecommerce_cart_service.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
import jakarta.validation.constraints.Min;
import lombok.*;

import java.math.BigDecimal;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Builder
@Table(
        name = "cart_items",
        indexes = {
                @Index(name = "idx_cart_product", columnList = "cart_id, productId")
        },
        uniqueConstraints = {
                @UniqueConstraint(columnNames = {"cart_id", "productId"})
        }
)
public class CartItem extends BaseModel {

    @Column(nullable = false)
    private Long productId;

    @Column(nullable = false)
    private String productNameSnapshot;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal priceSnapshot;

    @Column(nullable = false)
    @Min(1)
    private Integer quantity;

    @Column(nullable = false, precision = 12, scale = 2)
    private BigDecimal subtotal;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id", nullable = false)
    private Cart cart;
}
