package com.example.demo.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

import com.example.demo.application.port.LoadOrderPort;
import com.example.demo.application.service.GetOrderService.GetOrderQuery;
import com.example.demo.application.service.GetOrderService.GetOrderResult;
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
class GetOrderServiceTest {

    @Mock
    LoadOrderPort loadOrderPort;

    @InjectMocks
    GetOrderService getOrderService;

    @Test
    void should_return_order_details_when_order_exists_and_user_matches() {
        // Given
        String orderId = "order-001";
        String userId = "user-001";
        String merchantId = "merchant-001";

        OrderItem item = new OrderItem(new DishId("dish-001"), "宫保鸡丁", 2, new BigDecimal("25.00"));
        DeliveryInfo deliveryInfo = new DeliveryInfo("张三", "13800138000", "北京市朝阳区某某街道123号");
        Pricing pricing = Pricing.calculate(List.of(item));
        Instant createdAt = Instant.parse("2025-01-04T12:00:00Z");

        Order order = new Order(
                new OrderId(orderId),
                new OrderNumber("20250104120000123456"),
                new UserId(userId),
                new MerchantId(merchantId),
                List.of(item),
                deliveryInfo,
                "少辣",
                OrderStatus.PENDING_PAYMENT,
                pricing,
                createdAt,
                createdAt);

        when(loadOrderPort.findById(new OrderId(orderId))).thenReturn(Optional.of(order));

        GetOrderQuery query = new GetOrderQuery(orderId, userId);

        // When
        GetOrderResult result = getOrderService.getOrder(query);

        // Then
        assertThat(result).isNotNull();
        assertThat(result.orderId()).isEqualTo(orderId);
        assertThat(result.orderNumber()).isEqualTo("20250104120000123456");
        assertThat(result.userId()).isEqualTo(userId);
        assertThat(result.merchantId()).isEqualTo(merchantId);
        assertThat(result.remark()).isEqualTo("少辣");
        assertThat(result.status()).isEqualTo("PENDING_PAYMENT");
        assertThat(result.createdAt()).isEqualTo(createdAt);

        // Verify items
        assertThat(result.items()).hasSize(1);
        assertThat(result.items().get(0).dishId()).isEqualTo("dish-001");
        assertThat(result.items().get(0).dishName()).isEqualTo("宫保鸡丁");
        assertThat(result.items().get(0).quantity()).isEqualTo(2);
        assertThat(result.items().get(0).price()).isEqualByComparingTo(new BigDecimal("25.00"));

        // Verify delivery info
        assertThat(result.deliveryInfo()).isNotNull();
        assertThat(result.deliveryInfo().recipientName()).isEqualTo("张三");
        assertThat(result.deliveryInfo().recipientPhone()).isEqualTo("13800138000");
        assertThat(result.deliveryInfo().address()).isEqualTo("北京市朝阳区某某街道123号");

        // Verify pricing
        assertThat(result.pricing()).isNotNull();
        assertThat(result.pricing().itemsTotal()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(result.pricing().packagingFee()).isEqualByComparingTo(new BigDecimal("1.00"));
        assertThat(result.pricing().deliveryFee()).isEqualByComparingTo(new BigDecimal("3.00"));
        assertThat(result.pricing().finalAmount()).isEqualByComparingTo(new BigDecimal("54.00"));
    }

    @Test
    void should_throw_exception_when_order_does_not_exist() {
        // Given
        String orderId = "non-existent-order";
        String userId = "user-001";

        when(loadOrderPort.findById(new OrderId(orderId))).thenReturn(Optional.empty());

        GetOrderQuery query = new GetOrderQuery(orderId, userId);

        // When & Then
        assertThatThrownBy(() -> getOrderService.getOrder(query))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("订单不存在: " + orderId);
    }

    @Test
    void should_throw_exception_when_user_does_not_own_order() {
        // Given
        String orderId = "order-001";
        String requestingUserId = "user-002";
        String orderOwnerUserId = "user-001";

        OrderItem item = new OrderItem(new DishId("dish-001"), "宫保鸡丁", 1, new BigDecimal("25.00"));
        DeliveryInfo deliveryInfo = new DeliveryInfo("张三", "13800138000", "北京市朝阳区某某街道123号");
        Pricing pricing = Pricing.calculate(List.of(item));
        Instant createdAt = Instant.parse("2025-01-04T12:00:00Z");

        Order order = new Order(
                new OrderId(orderId),
                new OrderNumber("20250104120000123456"),
                new UserId(orderOwnerUserId),
                new MerchantId("merchant-001"),
                List.of(item),
                deliveryInfo,
                null,
                OrderStatus.PENDING_PAYMENT,
                pricing,
                createdAt,
                createdAt);

        when(loadOrderPort.findById(new OrderId(orderId))).thenReturn(Optional.of(order));

        GetOrderQuery query = new GetOrderQuery(orderId, requestingUserId);

        // When & Then
        assertThatThrownBy(() -> getOrderService.getOrder(query))
                .isInstanceOf(OrderNotFoundException.class)
                .hasMessageContaining("订单不存在: " + orderId);
    }
}
