package com.example.demo.adapter.web;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.Mockito.*;

import com.example.demo.application.service.CreateOrderService;
import com.example.demo.application.service.GetOrderService;
import com.example.demo.application.service.OrderNotFoundException;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.TestInfo;

public abstract class OrdersBase extends ContractTestBase {

    @BeforeEach
    public void setup() {
        super.setup();

        // Mock CreateOrderService
        when(createOrderService.createOrder(any())).thenAnswer(invocation -> {
            CreateOrderService.CreateOrderCommand command = invocation.getArgument(0);

            BigDecimal itemsTotal = command.items().stream()
                    .map(item -> item.price().multiply(BigDecimal.valueOf(item.quantity())))
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            BigDecimal packagingFee = new BigDecimal("1.00");
            BigDecimal deliveryFee = new BigDecimal("3.00");
            BigDecimal finalAmount = itemsTotal.add(packagingFee).add(deliveryFee);

            return new CreateOrderService.CreateOrderResult(
                    "order-id-1",
                    "20251105102730996280",
                    "PENDING_PAYMENT",
                    new CreateOrderService.CreateOrderResult.PricingDto(
                            itemsTotal, packagingFee, deliveryFee, finalAmount),
                    Instant.parse("2025-11-05T02:27:30.745152Z"));
        });

        // Mock GetOrderService
        when(getOrderService.getOrder(any())).thenAnswer(invocation -> {
            GetOrderService.GetOrderQuery query = invocation.getArgument(0);

            if ("order-id-1".equals(query.orderId())) {
                return new GetOrderService.GetOrderResult(
                        "order-id-1",
                        "20251105102730996280",
                        "user-token",
                        "merchant-001",
                        List.of(new GetOrderService.GetOrderResult.OrderItemDto(
                                "dish-001", "宫保鸡丁", 2, new BigDecimal("25.00"))),
                        new GetOrderService.GetOrderResult.DeliveryInfoDto("张三", "13800138000", "北京市朝阳区xxx街道xxx号"),
                        "少辣",
                        "PENDING_PAYMENT",
                        new GetOrderService.GetOrderResult.PricingDto(
                                new BigDecimal("50.00"),
                                new BigDecimal("1.00"),
                                new BigDecimal("3.00"),
                                new BigDecimal("54.00")),
                        Instant.parse("2025-11-05T02:27:30.745152Z"));
            } else {
                throw new OrderNotFoundException("订单不存在");
            }
        });
    }

    @AfterEach
    void tearDown(TestInfo testInfo) {
        if (testInfo.getTestMethod()
                .filter(method -> method.getName().equals("validate_create_a_new_order"))
                .isPresent()) {
            verify(createOrderService).createOrder(assertArg(command -> {
                assertThat(command.userId()).isEqualTo("user-token");
                assertThat(command.merchantId()).isEqualTo("merchant-001");
                assertThat(command.items()).hasSize(1);
                assertThat(command.items().get(0).dishId()).isEqualTo("dish-001");
                assertThat(command.items().get(0).dishName()).isEqualTo("宫保鸡丁");
                assertThat(command.items().get(0).quantity()).isEqualTo(2);
                assertThat(command.items().get(0).price()).isEqualTo(new BigDecimal("25.00"));
                assertThat(command.deliveryInfo().recipientName()).isEqualTo("张三");
                assertThat(command.deliveryInfo().recipientPhone()).isEqualTo("13800138000");
                assertThat(command.deliveryInfo().address()).isEqualTo("北京市朝阳区xxx街道xxx号");
            }));
        }
    }
}
