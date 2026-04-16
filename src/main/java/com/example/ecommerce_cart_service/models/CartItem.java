package com.example.ecommerce_cart_service.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
import jakarta.persistence.*;
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
        uniqueConstraints = {@UniqueConstraint(columnNames = {"cart_id", "productId"})}
)
public class CartItem extends BaseModel {

    private Long productId;

    private String productNameSnapshot;

    @Column(precision = 12, scale = 2)
    private BigDecimal priceSnapshot;

    private Integer quantity;

    @Column(precision = 12, scale = 2)
    private BigDecimal subtotal;

    @JsonIgnore
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "cart_id")
    private Cart cart;
}
