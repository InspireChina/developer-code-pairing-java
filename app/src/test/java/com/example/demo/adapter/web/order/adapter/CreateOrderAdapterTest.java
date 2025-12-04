package com.example.demo.adapter.web.order.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.adapter.web.order.CreateOrderController;
import com.example.demo.application.service.CreateOrderService;
import com.example.demo.application.service.CreateOrderService.CreateOrderResult;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;

@ExtendWith(MockitoExtension.class)
class CreateOrderAdapterTest {
    @Mock
    private CreateOrderService createOrderService;

    @InjectMocks
    private CreateOrderAdapter createOrderAdapter;

    @Test
    void create_order_should_call_service_with_correct_command() {
        CreateOrderController.CreateOrderRequest.OrderItemRequest item1 =
                new CreateOrderController.CreateOrderRequest.OrderItemRequest(
                        "dish-001", "宫保鸡丁", 2, new BigDecimal("25.00"));
        CreateOrderController.CreateOrderRequest.OrderItemRequest item2 =
                new CreateOrderController.CreateOrderRequest.OrderItemRequest(
                        "dish-002", "鱼香肉丝", 1, new BigDecimal("30.00"));
        CreateOrderController.CreateOrderRequest.DeliveryInfoRequest deliveryInfo =
                new CreateOrderController.CreateOrderRequest.DeliveryInfoRequest("张三", "13800138000", "北京市朝阳区某某街道123号");

        CreateOrderController.CreateOrderRequest request =
                new CreateOrderController.CreateOrderRequest("merchant-001", List.of(item1, item2), deliveryInfo, "少辣");

        when(createOrderService.createOrder(any()))
                .thenReturn(new CreateOrderResult(
                        "order-id-1",
                        "20251105102730996280",
                        "PENDING_PAYMENT",
                        new CreateOrderResult.PricingDto(
                                new BigDecimal("80.00"),
                                new BigDecimal("1.00"),
                                new BigDecimal("3.00"),
                                new BigDecimal("84.00")),
                        Instant.parse("2025-11-05T02:27:30.745152Z")));

        UserDetails user = User.withUsername("user-001")
                .password("")
                .authorities("ROLE_USER")
                .build();
        createOrderAdapter.createOrder(request, (User) user);

        verify(createOrderService).createOrder(assertArg(command -> {
            assertThat(command)
                    .returns("user-001", from(CreateOrderService.CreateOrderCommand::userId))
                    .returns("merchant-001", from(CreateOrderService.CreateOrderCommand::merchantId))
                    .returns("少辣", from(CreateOrderService.CreateOrderCommand::remark));

            assertThat(command.items()).hasSize(2);
            assertThat(command.items().get(0))
                    .returns("dish-001", from(CreateOrderService.CreateOrderCommand.OrderItemDto::dishId))
                    .returns("宫保鸡丁", from(CreateOrderService.CreateOrderCommand.OrderItemDto::dishName))
                    .returns(2, from(CreateOrderService.CreateOrderCommand.OrderItemDto::quantity))
                    .returns(new BigDecimal("25.00"), from(CreateOrderService.CreateOrderCommand.OrderItemDto::price));
            assertThat(command.items().get(1))
                    .returns("dish-002", from(CreateOrderService.CreateOrderCommand.OrderItemDto::dishId))
                    .returns("鱼香肉丝", from(CreateOrderService.CreateOrderCommand.OrderItemDto::dishName))
                    .returns(1, from(CreateOrderService.CreateOrderCommand.OrderItemDto::quantity))
                    .returns(new BigDecimal("30.00"), from(CreateOrderService.CreateOrderCommand.OrderItemDto::price));

            assertThat(command.deliveryInfo())
                    .returns("张三", from(CreateOrderService.CreateOrderCommand.DeliveryInfoDto::recipientName))
                    .returns("13800138000", from(CreateOrderService.CreateOrderCommand.DeliveryInfoDto::recipientPhone))
                    .returns("北京市朝阳区某某街道123号", from(CreateOrderService.CreateOrderCommand.DeliveryInfoDto::address));
        }));
    }

    @Test
    void create_order_should_return_correct_response() {
        CreateOrderController.CreateOrderRequest.OrderItemRequest item =
                new CreateOrderController.CreateOrderRequest.OrderItemRequest(
                        "dish-001", "宫保鸡丁", 2, new BigDecimal("25.00"));
        CreateOrderController.CreateOrderRequest.DeliveryInfoRequest deliveryInfo =
                new CreateOrderController.CreateOrderRequest.DeliveryInfoRequest("张三", "13800138000", "北京市朝阳区某某街道123号");

        CreateOrderController.CreateOrderRequest request =
                new CreateOrderController.CreateOrderRequest("merchant-001", List.of(item), deliveryInfo, null);

        when(createOrderService.createOrder(any()))
                .thenReturn(new CreateOrderResult(
                        "order-id-1",
                        "20251105102730996280",
                        "PENDING_PAYMENT",
                        new CreateOrderResult.PricingDto(
                                new BigDecimal("50.00"),
                                new BigDecimal("1.00"),
                                new BigDecimal("3.00"),
                                new BigDecimal("54.00")),
                        Instant.parse("2025-11-05T02:27:30.745152Z")));

        UserDetails user = User.withUsername("user-001")
                .password("")
                .authorities("ROLE_USER")
                .build();
        CreateOrderAdapter.CreateOrderResponse response = createOrderAdapter.createOrder(request, (User) user);

        assertThat(response.code()).isEqualTo(0);
        assertThat(response.message()).isEqualTo("订单创建成功");
        assertThat(response.data().orderId()).isEqualTo("order-id-1");
        assertThat(response.data().orderNumber()).isEqualTo("20251105102730996280");
        assertThat(response.data().status()).isEqualTo("PENDING_PAYMENT");
        assertThat(response.data().pricing().itemsTotal()).isEqualByComparingTo(new BigDecimal("50.00"));
        assertThat(response.data().pricing().packagingFee()).isEqualByComparingTo(new BigDecimal("1.00"));
        assertThat(response.data().pricing().deliveryFee()).isEqualByComparingTo(new BigDecimal("3.00"));
        assertThat(response.data().pricing().finalAmount()).isEqualByComparingTo(new BigDecimal("54.00"));
        assertThat(response.data().createdAt()).isEqualTo("2025-11-05T02:27:30.745152Z");
    }
}
