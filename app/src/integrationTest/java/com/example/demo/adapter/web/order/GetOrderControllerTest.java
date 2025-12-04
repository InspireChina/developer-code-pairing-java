package com.example.demo.adapter.web.order;

import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.user;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class GetOrderControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void get_order_should_succeed_with_valid_order_id() throws Exception {
        // Given - Create an order first
        String orderId = createTestOrder("test-user-001");

        // When & Then - Get the order
        mockMvc.perform(get("/api/v1/orders/{orderId}", orderId).with(user("test-user-001")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.message").value("查询成功"))
                .andExpect(jsonPath("$.data.orderId").value(orderId))
                .andExpect(jsonPath("$.data.orderNumber").exists())
                .andExpect(jsonPath("$.data.userId").value("test-user-001"))
                .andExpect(jsonPath("$.data.merchantId").value("merchant-001"))
                .andExpect(jsonPath("$.data.status").value("PENDING_PAYMENT"))
                .andExpect(jsonPath("$.data.items").isArray())
                .andExpect(jsonPath("$.data.items[0].dishId").value("dish-001"))
                .andExpect(jsonPath("$.data.items[0].dishName").value("宫保鸡丁"))
                .andExpect(jsonPath("$.data.items[0].quantity").value(2))
                .andExpect(jsonPath("$.data.items[0].price").value(25.00))
                .andExpect(jsonPath("$.data.deliveryInfo.recipientName").value("张三"))
                .andExpect(jsonPath("$.data.deliveryInfo.recipientPhone").value("13800138000"))
                .andExpect(jsonPath("$.data.deliveryInfo.address").value("北京市朝阳区xxx街道xxx号"))
                .andExpect(jsonPath("$.data.remark").isEmpty())
                .andExpect(jsonPath("$.data.pricing.itemsTotal").value(50.00))
                .andExpect(jsonPath("$.data.pricing.packagingFee").value(1.00))
                .andExpect(jsonPath("$.data.pricing.deliveryFee").value(3.00))
                .andExpect(jsonPath("$.data.pricing.finalAmount").value(54.00))
                .andExpect(jsonPath("$.data.createdAt").exists());
    }

    @Test
    void get_order_should_return_404_when_order_does_not_exist() throws Exception {
        // Given - Non-existent order ID
        String nonExistentOrderId = "550e8400-e29b-41d4-a716-446655440000";

        // When & Then
        mockMvc.perform(get("/api/v1/orders/{orderId}", nonExistentOrderId).with(user("test-user-001")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("OrderNotFoundException"))
                .andExpect(jsonPath("$.detail").value("订单不存在: " + nonExistentOrderId));
    }

    @Test
    void get_order_should_return_404_when_accessing_other_users_order() throws Exception {
        // Given - Create an order for user-001
        String orderId = createTestOrder("test-user-001");

        // When & Then - Try to access with user-002
        mockMvc.perform(get("/api/v1/orders/{orderId}", orderId).with(user("test-user-002")))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.title").value("OrderNotFoundException"))
                .andExpect(jsonPath("$.detail").value("订单不存在: " + orderId));
    }

    @Test
    void get_order_should_return_403_when_not_authenticated() throws Exception {
        // Given - Create an order
        String orderId = createTestOrder("test-user-001");

        // When & Then - Try to access without authentication
        // Spring Security returns 403 Forbidden when authentication is missing
        mockMvc.perform(get("/api/v1/orders/{orderId}", orderId)).andExpect(status().isForbidden());
    }

    @Test
    void get_order_should_return_order_with_remark() throws Exception {
        // Given - Create an order with remark
        String orderId = createTestOrderWithRemark("test-user-004", "少辣");

        // When & Then
        mockMvc.perform(get("/api/v1/orders/{orderId}", orderId).with(user("test-user-004")))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(0))
                .andExpect(jsonPath("$.data.remark").value("少辣"));
    }

    /**
     * Helper method to create a test order and return its ID.
     */
    private String createTestOrder(String userId) throws Exception {
        CreateOrderController.CreateOrderRequest request = new CreateOrderController.CreateOrderRequest(
                "merchant-001",
                List.of(new CreateOrderController.CreateOrderRequest.OrderItemRequest(
                        "dish-001", "宫保鸡丁", 2, new BigDecimal("25.00"))),
                new CreateOrderController.CreateOrderRequest.DeliveryInfoRequest(
                        "张三", "13800138000", "北京市朝阳区xxx街道xxx号"),
                null);

        MvcResult result = mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(userId)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        return objectMapper.readTree(responseBody).path("data").path("orderId").asText();
    }

    /**
     * Helper method to create a test order with remark.
     */
    private String createTestOrderWithRemark(String userId, String remark) throws Exception {
        CreateOrderController.CreateOrderRequest request = new CreateOrderController.CreateOrderRequest(
                "merchant-001",
                List.of(new CreateOrderController.CreateOrderRequest.OrderItemRequest(
                        "dish-001", "宫保鸡丁", 2, new BigDecimal("25.00"))),
                new CreateOrderController.CreateOrderRequest.DeliveryInfoRequest(
                        "张三", "13800138000", "北京市朝阳区xxx街道xxx号"),
                remark);

        MvcResult result = mockMvc.perform(post("/api/v1/orders")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request))
                        .with(user(userId)))
                .andExpect(status().isCreated())
                .andReturn();

        String responseBody = result.getResponse().getContentAsString();
        return objectMapper.readTree(responseBody).path("data").path("orderId").asText();
    }
}
