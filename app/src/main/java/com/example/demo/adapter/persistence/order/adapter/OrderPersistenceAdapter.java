package com.example.demo.adapter.persistence.order.adapter;

import com.example.demo.adapter.persistence.order.DeliveryInfoEmbeddable;
import com.example.demo.adapter.persistence.order.OrderEntity;
import com.example.demo.adapter.persistence.order.OrderEntityRepository;
import com.example.demo.adapter.persistence.order.OrderItemEntity;
import com.example.demo.adapter.persistence.order.PricingEmbeddable;
import com.example.demo.application.port.LoadOrderPort;
import com.example.demo.application.port.SaveOrderPort;
import com.example.demo.domain.dish.DishId;
import com.example.demo.domain.merchant.MerchantId;
import com.example.demo.domain.order.DeliveryInfo;
import com.example.demo.domain.order.Order;
import com.example.demo.domain.order.OrderId;
import com.example.demo.domain.order.OrderItem;
import com.example.demo.domain.order.OrderNumber;
import com.example.demo.domain.order.Pricing;
import com.example.demo.domain.user.UserId;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class OrderPersistenceAdapter implements SaveOrderPort, LoadOrderPort {

    private final OrderEntityRepository orderEntityRepository;

    public void save(Order order) {
        OrderEntity entity = toEntity(order);
        orderEntityRepository.save(entity);
    }

    public Optional<Order> findById(OrderId orderId) {
        return orderEntityRepository.findById(orderId.value()).map(this::toDomain);
    }

    private OrderEntity toEntity(Order order) {
        OrderEntity entity = new OrderEntity();
        entity.setId(order.getId().value());
        entity.setOrderNumber(order.getOrderNumber().value());
        entity.setUserId(order.getUserId().value());
        entity.setMerchantId(order.getMerchantId().value());

        // Convert order items
        List<OrderItemEntity> itemEntities = order.getItems().stream()
                .map(item -> {
                    OrderItemEntity itemEntity = new OrderItemEntity();
                    itemEntity.setOrderId(order.getId().value());
                    itemEntity.setDishId(item.dishId().value());
                    itemEntity.setDishName(item.dishName());
                    itemEntity.setQuantity(item.quantity());
                    itemEntity.setPrice(item.price());
                    return itemEntity;
                })
                .collect(Collectors.toList());
        entity.setItems(itemEntities);

        // Convert delivery info
        DeliveryInfoEmbeddable deliveryInfoEmbeddable = new DeliveryInfoEmbeddable(
                order.getDeliveryInfo().recipientName(),
                order.getDeliveryInfo().recipientPhone(),
                order.getDeliveryInfo().address());
        entity.setDeliveryInfo(deliveryInfoEmbeddable);

        entity.setRemark(order.getRemark());
        entity.setStatus(order.getStatus());

        // Convert pricing
        PricingEmbeddable pricingEmbeddable = new PricingEmbeddable(
                order.getPricing().itemsTotal(),
                order.getPricing().packagingFee(),
                order.getPricing().deliveryFee(),
                order.getPricing().finalAmount());
        entity.setPricing(pricingEmbeddable);

        entity.setCreatedAt(order.getCreatedAt());
        entity.setUpdatedAt(order.getUpdatedAt());

        return entity;
    }

    private Order toDomain(OrderEntity entity) {
        // Convert order items
        List<OrderItem> items = entity.getItems().stream()
                .map(itemEntity -> new OrderItem(
                        new DishId(itemEntity.getDishId()),
                        itemEntity.getDishName(),
                        itemEntity.getQuantity(),
                        itemEntity.getPrice()))
                .collect(Collectors.toList());

        // Convert delivery info
        DeliveryInfo deliveryInfo = new DeliveryInfo(
                entity.getDeliveryInfo().getRecipientName(),
                entity.getDeliveryInfo().getRecipientPhone(),
                entity.getDeliveryInfo().getAddress());

        // Convert pricing
        Pricing pricing = new Pricing(
                entity.getPricing().getItemsTotal(),
                entity.getPricing().getPackagingFee(),
                entity.getPricing().getDeliveryFee(),
                entity.getPricing().getFinalAmount());

        // Use reconstitution constructor
        return new Order(
                new OrderId(entity.getId()),
                new OrderNumber(entity.getOrderNumber()),
                new UserId(entity.getUserId()),
                new MerchantId(entity.getMerchantId()),
                items,
                deliveryInfo,
                entity.getRemark(),
                entity.getStatus(),
                pricing,
                entity.getCreatedAt(),
                entity.getUpdatedAt());
    }
}
