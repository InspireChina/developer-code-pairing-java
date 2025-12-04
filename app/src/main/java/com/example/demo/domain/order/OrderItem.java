package com.example.demo.domain.order;

import com.example.demo.domain.dish.DishId;
import java.math.BigDecimal;

public record OrderItem(DishId dishId, String dishName, int quantity, BigDecimal price) {
    public OrderItem {
        if (dishId == null) {
            throw new IllegalArgumentException("餐品ID不能为空");
        }
        if (dishName == null || dishName.isBlank()) {
            throw new IllegalArgumentException("餐品名称不能为空");
        }
        if (quantity <= 0) {
            throw new IllegalArgumentException("数量必须大于0");
        }
        if (price == null || price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException("价格必须大于等于0");
        }
    }

    public BigDecimal subtotal() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}
