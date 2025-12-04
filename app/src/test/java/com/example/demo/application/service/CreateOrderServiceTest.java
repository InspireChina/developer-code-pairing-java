package com.example.demo.application.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.verify;

import com.example.demo.application.port.SaveOrderPort;
import com.example.demo.application.service.CreateOrderService.CreateOrderCommand;
import com.example.demo.application.service.CreateOrderService.CreateOrderCommand.DeliveryInfoDto;
import com.example.demo.application.service.CreateOrderService.CreateOrderCommand.OrderItemDto;
import com.example.demo.domain.order.Order;
import com.example.demo.domain.order.Pricing;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class CreateOrderServiceTest {

    @Mock
    SaveOrderPort saveOrderPort;

    @InjectMocks
    CreateOrderService createOrderService;

    @Captor
    ArgumentCaptor<Order> orderCaptor;

    @Test
    void create_order_should_create_and_save_order() {
        OrderItemDto item = new OrderItemDto("dish-001", "宫保鸡丁", 2, new BigDecimal("25.00"));
        DeliveryInfoDto deliveryInfo = new DeliveryInfoDto("张三", "13800138000", "北京市朝阳区某某街道123号");

        CreateOrderCommand command =
                new CreateOrderCommand("user-001", "merchant-001", List.of(item), deliveryInfo, "少辣");

        createOrderService.createOrder(command);

        verify(saveOrderPort).save(orderCaptor.capture());
        Order capturedOrder = orderCaptor.getValue();

        assertThat(capturedOrder.getUserId().value()).isEqualTo("user-001");
        assertThat(capturedOrder.getMerchantId().value()).isEqualTo("merchant-001");
        assertThat(capturedOrder.getItems()).hasSize(1);
        assertThat(capturedOrder.getItems().get(0).dishId().value()).isEqualTo("dish-001");
        assertThat(capturedOrder.getItems().get(0).dishName()).isEqualTo("宫保鸡丁");
        assertThat(capturedOrder.getItems().get(0).quantity()).isEqualTo(2);
        assertThat(capturedOrder.getItems().get(0).price()).isEqualByComparingTo(new BigDecimal("25.00"));
        assertThat(capturedOrder.getDeliveryInfo().recipientName()).isEqualTo("张三");
        assertThat(capturedOrder.getDeliveryInfo().recipientPhone()).isEqualTo("13800138000");
        assertThat(capturedOrder.getDeliveryInfo().address()).isEqualTo("北京市朝阳区某某街道123号");
        assertThat(capturedOrder.getPricing().itemsTotal()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(capturedOrder.getPricing().packagingFee()).isEqualByComparingTo(Pricing.PACKAGING_FEE);
        assertThat(capturedOrder.getPricing().deliveryFee()).isEqualByComparingTo(Pricing.DELIVERY_FEE);
        assertThat(capturedOrder.getPricing().finalAmount()).isEqualByComparingTo(new BigDecimal("54.00"));
        assertThat(capturedOrder.getRemark()).isEqualTo("少辣");
    }

    @Test
    void create_order_should_set_status_to_pending_payment() {
        OrderItemDto item = new OrderItemDto("dish-001", "宫保鸡丁", 1, new BigDecimal("25.00"));
        DeliveryInfoDto deliveryInfo = new DeliveryInfoDto("张三", "13800138000", "北京市朝阳区某某街道123号");

        CreateOrderCommand command =
                new CreateOrderCommand("user-001", "merchant-001", List.of(item), deliveryInfo, null);

        createOrderService.createOrder(command);

        verify(saveOrderPort).save(orderCaptor.capture());
        Order capturedOrder = orderCaptor.getValue();

        assertThat(capturedOrder.getStatus().name()).isEqualTo("PENDING_PAYMENT");
    }
}
