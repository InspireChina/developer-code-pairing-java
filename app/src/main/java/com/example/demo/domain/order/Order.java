package com.example.demo.domain.order;

import com.example.demo.domain.Identities;
import com.example.demo.domain.merchant.MerchantId;
import com.example.demo.domain.user.UserId;
import java.time.Instant;
import java.util.List;
import lombok.Getter;

@Getter
public class Order {
    private final OrderId id;
    private final OrderNumber orderNumber;
    private final UserId userId;
    private final MerchantId merchantId;
    private final List<OrderItem> items;
    private final DeliveryInfo deliveryInfo;
    private final String remark;
    private OrderStatus status;
    private final Pricing pricing;
    private final Instant createdAt;
    private Instant updatedAt;

    public Order(
            UserId userId, MerchantId merchantId, List<OrderItem> items, DeliveryInfo deliveryInfo, String remark) {
        if (items == null || items.isEmpty()) {
            throw new IllegalArgumentException("订单必须至少包含一个餐品");
        }

        if (remark != null && remark.length() > 200) {
            throw new IllegalArgumentException("备注长度不能超过200字符");
        }

        this.id = new OrderId(Identities.generateId());
        this.orderNumber = new OrderNumber();
        this.userId = userId;
        this.merchantId = merchantId;
        this.items = List.copyOf(items);
        this.deliveryInfo = deliveryInfo;
        this.remark = remark;
        this.status = OrderStatus.PENDING_PAYMENT;
        this.pricing = Pricing.calculate(items);
        this.createdAt = Instant.now();
        this.updatedAt = Instant.now();
    }

    public Order(
            OrderId id,
            OrderNumber orderNumber,
            UserId userId,
            MerchantId merchantId,
            List<OrderItem> items,
            DeliveryInfo deliveryInfo,
            String remark,
            OrderStatus status,
            Pricing pricing,
            Instant createdAt,
            Instant updatedAt) {
        this.id = id;
        this.orderNumber = orderNumber;
        this.userId = userId;
        this.merchantId = merchantId;
        this.items = items;
        this.deliveryInfo = deliveryInfo;
        this.remark = remark;
        this.status = status;
        this.pricing = pricing;
        this.createdAt = createdAt;
        this.updatedAt = updatedAt;
    }
}
