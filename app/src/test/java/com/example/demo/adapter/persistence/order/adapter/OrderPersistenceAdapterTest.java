package com.example.demo.adapter.persistence.order.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;
import static org.mockito.Mockito.*;

import com.example.demo.adapter.persistence.order.DeliveryInfoEmbeddable;
import com.example.demo.adapter.persistence.order.OrderEntity;
import com.example.demo.adapter.persistence.order.OrderEntityRepository;
import com.example.demo.adapter.persistence.order.OrderItemEntity;
import com.example.demo.adapter.persistence.order.PricingEmbeddable;
import com.example.demo.domain.dish.DishId;
import com.example.demo.domain.merchant.MerchantId;
import com.example.demo.domain.order.DeliveryInfo;
import com.example.demo.domain.order.Order;
import com.example.demo.domain.order.OrderId;
import com.example.demo.domain.order.OrderItem;
import com.example.demo.domain.order.OrderNumber;
import com.example.demo.domain.order.OrderStatus;
import com.example.demo.domain.order.Pricing;
import com.example.demo.domain.user.UserId;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class OrderPersistenceAdapterTest {

    @Mock
    private OrderEntityRepository orderEntityRepository;

    @InjectMocks
    private OrderPersistenceAdapter orderPersistenceAdapter;

    @Test
    void save_should_persist_order_with_all_fields() {
        Instant now = Instant.now();

        // Create order items
        OrderItem item = new OrderItem(new DishId("dish-001"), "宫保鸡丁", 2, new BigDecimal("25.00"));

        // Create delivery info
        DeliveryInfo deliveryInfo = new DeliveryInfo("张三", "13800138000", "北京市朝阳区某某街道123号");

        // Create pricing
        Pricing pricing = Pricing.calculate(List.of(item));

        // Create order using reconstitution constructor
        Order order = new Order(
                new OrderId("order-001"),
                new OrderNumber("20251105102730996280"),
                new UserId("user-001"),
                new MerchantId("merchant-001"),
                List.of(item),
                deliveryInfo,
                "少辣",
                OrderStatus.PENDING_PAYMENT,
                pricing,
                now,
                now);

        orderPersistenceAdapter.save(order);

        verify(orderEntityRepository).save(assertArg(orderEntity -> {
            assertThat(orderEntity)
                    .returns("order-001", from(OrderEntity::getId))
                    .returns("20251105102730996280", from(OrderEntity::getOrderNumber))
                    .returns("user-001", from(OrderEntity::getUserId))
                    .returns("merchant-001", from(OrderEntity::getMerchantId))
                    .returns("少辣", from(OrderEntity::getRemark))
                    .returns(OrderStatus.PENDING_PAYMENT, from(OrderEntity::getStatus))
                    .returns(now, from(OrderEntity::getCreatedAt))
                    .returns(now, from(OrderEntity::getUpdatedAt));

            // Verify items
            assertThat(orderEntity.getItems()).hasSize(1);
            assertThat(orderEntity.getItems().get(0))
                    .returns("order-001", from(OrderItemEntity::getOrderId))
                    .returns("dish-001", from(OrderItemEntity::getDishId))
                    .returns("宫保鸡丁", from(OrderItemEntity::getDishName))
                    .returns(2, from(OrderItemEntity::getQuantity))
                    .returns(new BigDecimal("25.00"), from(OrderItemEntity::getPrice));

            // Verify delivery info
            assertThat(orderEntity.getDeliveryInfo())
                    .returns("张三", from(DeliveryInfoEmbeddable::getRecipientName))
                    .returns("13800138000", from(DeliveryInfoEmbeddable::getRecipientPhone))
                    .returns("北京市朝阳区某某街道123号", from(DeliveryInfoEmbeddable::getAddress));

            // Verify pricing
            assertThat(orderEntity.getPricing())
                    .returns(new BigDecimal("50.00"), from(PricingEmbeddable::getItemsTotal))
                    .returns(new BigDecimal("1.00"), from(PricingEmbeddable::getPackagingFee))
                    .returns(new BigDecimal("3.00"), from(PricingEmbeddable::getDeliveryFee))
                    .returns(new BigDecimal("54.00"), from(PricingEmbeddable::getFinalAmount));
        }));
    }

    @Test
    void find_by_id_should_return_order_with_all_fields() {
        Instant now = Instant.now();

        // Create order item entities
        OrderItemEntity itemEntity = new OrderItemEntity();
        itemEntity.setOrderId("order-001");
        itemEntity.setDishId("dish-001");
        itemEntity.setDishName("宫保鸡丁");
        itemEntity.setQuantity(2);
        itemEntity.setPrice(new BigDecimal("25.00"));

        // Create delivery info embeddable
        DeliveryInfoEmbeddable deliveryInfoEmbeddable =
                new DeliveryInfoEmbeddable("张三", "13800138000", "北京市朝阳区某某街道123号");

        // Create pricing embeddable
        PricingEmbeddable pricingEmbeddable = new PricingEmbeddable(
                new BigDecimal("50.00"), new BigDecimal("1.00"), new BigDecimal("3.00"), new BigDecimal("54.00"));

        // Create order entity
        OrderEntity orderEntity = new OrderEntity();
        orderEntity.setId("order-001");
        orderEntity.setOrderNumber("20251105102730996280");
        orderEntity.setUserId("user-001");
        orderEntity.setMerchantId("merchant-001");
        orderEntity.setItems(List.of(itemEntity));
        orderEntity.setDeliveryInfo(deliveryInfoEmbeddable);
        orderEntity.setRemark("少辣");
        orderEntity.setStatus(OrderStatus.PENDING_PAYMENT);
        orderEntity.setPricing(pricingEmbeddable);
        orderEntity.setCreatedAt(now);
        orderEntity.setUpdatedAt(now);

        when(orderEntityRepository.findById("order-001")).thenReturn(Optional.of(orderEntity));

        Optional<Order> result = orderPersistenceAdapter.findById(new OrderId("order-001"));

        assertThat(result).isPresent();
        Order order = result.get();

        // Verify basic fields
        assertThat(order.getId().value()).isEqualTo("order-001");
        assertThat(order.getOrderNumber().value()).isEqualTo("20251105102730996280");
        assertThat(order.getUserId().value()).isEqualTo("user-001");
        assertThat(order.getMerchantId().value()).isEqualTo("merchant-001");
        assertThat(order.getRemark()).isEqualTo("少辣");
        assertThat(order.getStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
        assertThat(order.getCreatedAt()).isEqualTo(now);
        assertThat(order.getUpdatedAt()).isEqualTo(now);

        // Verify items
        assertThat(order.getItems()).hasSize(1);
        assertThat(order.getItems().get(0))
                .returns(new DishId("dish-001"), from(OrderItem::dishId))
                .returns("宫保鸡丁", from(OrderItem::dishName))
                .returns(2, from(OrderItem::quantity))
                .returns(new BigDecimal("25.00"), from(OrderItem::price));

        // Verify delivery info
        assertThat(order.getDeliveryInfo())
                .returns("张三", from(DeliveryInfo::recipientName))
                .returns("13800138000", from(DeliveryInfo::recipientPhone))
                .returns("北京市朝阳区某某街道123号", from(DeliveryInfo::address));

        // Verify pricing
        assertThat(order.getPricing())
                .returns(new BigDecimal("50.00"), from(Pricing::itemsTotal))
                .returns(new BigDecimal("1.00"), from(Pricing::packagingFee))
                .returns(new BigDecimal("3.00"), from(Pricing::deliveryFee))
                .returns(new BigDecimal("54.00"), from(Pricing::finalAmount));
    }
}
