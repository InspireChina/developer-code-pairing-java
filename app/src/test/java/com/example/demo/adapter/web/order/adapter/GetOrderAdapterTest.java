package com.example.demo.adapter.web.order.adapter;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.from;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.assertArg;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.demo.application.service.GetOrderService;
import com.example.demo.application.service.GetOrderService.GetOrderQuery;
import com.example.demo.application.service.GetOrderService.GetOrderResult;
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
class GetOrderAdapterTest {
    @Mock
    private GetOrderService getOrderService;

    @InjectMocks
    private GetOrderAdapter getOrderAdapter;

    @Test
    void get_order_should_call_service_with_correct_query() {
        // Arrange
        String orderId = "order-id-1";
        UserDetails user = User.withUsername("user-001")
                .password("")
                .authorities("ROLE_USER")
                .build();

        GetOrderResult mockResult = createMockGetOrderResult();
        when(getOrderService.getOrder(any())).thenReturn(mockResult);

        // Act
        getOrderAdapter.getOrder(orderId, (User) user);

        // Assert
        verify(getOrderService).getOrder(assertArg(query -> {
            assertThat(query)
                    .returns("order-id-1", from(GetOrderQuery::orderId))
                    .returns("user-001", from(GetOrderQuery::userId));
        }));
    }

    @Test
    void get_order_should_return_correct_response_format() {
        // Arrange
        String orderId = "order-id-1";
        UserDetails user = User.withUsername("user-001")
                .password("")
                .authorities("ROLE_USER")
                .build();

        GetOrderResult mockResult = createMockGetOrderResult();
        when(getOrderService.getOrder(any())).thenReturn(mockResult);

        // Act
        GetOrderAdapter.GetOrderResponse response = getOrderAdapter.getOrder(orderId, (User) user);

        // Assert - Verify top-level response structure
        assertThat(response.code()).isEqualTo(0);
        assertThat(response.message()).isEqualTo("查询成功");
        assertThat(response.data()).isNotNull();
    }

    @Test
    void get_order_should_convert_result_to_response_correctly() {
        // Arrange
        String orderId = "order-id-1";
        UserDetails user = User.withUsername("user-001")
                .password("")
                .authorities("ROLE_USER")
                .build();

        GetOrderResult mockResult = createMockGetOrderResult();
        when(getOrderService.getOrder(any())).thenReturn(mockResult);

        // Act
        GetOrderAdapter.GetOrderResponse response = getOrderAdapter.getOrder(orderId, (User) user);

        // Assert - Verify order data
        assertThat(response.data().orderId()).isEqualTo("order-id-1");
        assertThat(response.data().orderNumber()).isEqualTo("20251105102730996280");
        assertThat(response.data().userId()).isEqualTo("user-001");
        assertThat(response.data().merchantId()).isEqualTo("merchant-001");
        assertThat(response.data().status()).isEqualTo("PENDING_PAYMENT");
        assertThat(response.data().remark()).isEqualTo("少辣");

        // Assert - Verify items
        assertThat(response.data().items()).hasSize(2);
        assertThat(response.data().items().get(0))
                .returns("dish-001", from(GetOrderAdapter.GetOrderResponse.OrderItemData::dishId))
                .returns("宫保鸡丁", from(GetOrderAdapter.GetOrderResponse.OrderItemData::dishName))
                .returns(2, from(GetOrderAdapter.GetOrderResponse.OrderItemData::quantity))
                .returns(new BigDecimal("25.00"), from(GetOrderAdapter.GetOrderResponse.OrderItemData::price));
        assertThat(response.data().items().get(1))
                .returns("dish-002", from(GetOrderAdapter.GetOrderResponse.OrderItemData::dishId))
                .returns("鱼香肉丝", from(GetOrderAdapter.GetOrderResponse.OrderItemData::dishName))
                .returns(1, from(GetOrderAdapter.GetOrderResponse.OrderItemData::quantity))
                .returns(new BigDecimal("30.00"), from(GetOrderAdapter.GetOrderResponse.OrderItemData::price));

        // Assert - Verify delivery info
        assertThat(response.data().deliveryInfo())
                .returns("张三", from(GetOrderAdapter.GetOrderResponse.DeliveryInfoData::recipientName))
                .returns("13800138000", from(GetOrderAdapter.GetOrderResponse.DeliveryInfoData::recipientPhone))
                .returns("北京市朝阳区某某街道123号", from(GetOrderAdapter.GetOrderResponse.DeliveryInfoData::address));

        // Assert - Verify pricing
        assertThat(response.data().pricing().itemsTotal()).isEqualByComparingTo(new BigDecimal("80.00"));
        assertThat(response.data().pricing().packagingFee()).isEqualByComparingTo(new BigDecimal("1.00"));
        assertThat(response.data().pricing().deliveryFee()).isEqualByComparingTo(new BigDecimal("3.00"));
        assertThat(response.data().pricing().finalAmount()).isEqualByComparingTo(new BigDecimal("84.00"));
    }

    @Test
    void get_order_should_convert_timestamp_to_iso8601_format() {
        // Arrange
        String orderId = "order-id-1";
        UserDetails user = User.withUsername("user-001")
                .password("")
                .authorities("ROLE_USER")
                .build();

        GetOrderResult mockResult = createMockGetOrderResult();
        when(getOrderService.getOrder(any())).thenReturn(mockResult);

        // Act
        GetOrderAdapter.GetOrderResponse response = getOrderAdapter.getOrder(orderId, (User) user);

        // Assert - Verify ISO 8601 format
        assertThat(response.data().createdAt()).isEqualTo("2025-11-05T02:27:30.745152Z");
    }

    private GetOrderResult createMockGetOrderResult() {
        GetOrderResult.OrderItemDto item1 =
                new GetOrderResult.OrderItemDto("dish-001", "宫保鸡丁", 2, new BigDecimal("25.00"));
        GetOrderResult.OrderItemDto item2 =
                new GetOrderResult.OrderItemDto("dish-002", "鱼香肉丝", 1, new BigDecimal("30.00"));

        GetOrderResult.DeliveryInfoDto deliveryInfo =
                new GetOrderResult.DeliveryInfoDto("张三", "13800138000", "北京市朝阳区某某街道123号");

        GetOrderResult.PricingDto pricing = new GetOrderResult.PricingDto(
                new BigDecimal("80.00"), new BigDecimal("1.00"), new BigDecimal("3.00"), new BigDecimal("84.00"));

        return new GetOrderResult(
                "order-id-1",
                "20251105102730996280",
                "user-001",
                "merchant-001",
                List.of(item1, item2),
                deliveryInfo,
                "少辣",
                "PENDING_PAYMENT",
                pricing,
                Instant.parse("2025-11-05T02:27:30.745152Z"));
    }
}
