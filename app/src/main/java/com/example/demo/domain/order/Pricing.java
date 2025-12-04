package com.example.demo.domain.order;

import java.math.BigDecimal;
import java.util.List;

public record Pricing(BigDecimal itemsTotal, BigDecimal packagingFee, BigDecimal deliveryFee, BigDecimal finalAmount) {
    public static final BigDecimal PACKAGING_FEE = new BigDecimal("1.00");
    public static final BigDecimal DELIVERY_FEE = new BigDecimal("3.00");

    public static Pricing calculate(List<OrderItem> items) {
        BigDecimal itemsTotal =
                items.stream().map(OrderItem::subtotal).findFirst().orElse(BigDecimal.ZERO);

        BigDecimal finalAmount = itemsTotal.add(PACKAGING_FEE).add(DELIVERY_FEE);

        return new Pricing(itemsTotal, PACKAGING_FEE, DELIVERY_FEE, finalAmount);
    }
}
