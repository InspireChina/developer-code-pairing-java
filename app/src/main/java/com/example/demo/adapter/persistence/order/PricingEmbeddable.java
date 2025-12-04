package com.example.demo.adapter.persistence.order;

import jakarta.persistence.Embeddable;
import java.math.BigDecimal;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class PricingEmbeddable {
    private BigDecimal itemsTotal;
    private BigDecimal packagingFee;
    private BigDecimal deliveryFee;
    private BigDecimal finalAmount;
}
