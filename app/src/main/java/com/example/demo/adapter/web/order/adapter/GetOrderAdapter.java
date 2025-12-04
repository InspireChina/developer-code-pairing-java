package com.example.demo.adapter.web.order.adapter;

import com.example.demo.application.service.GetOrderService;
import com.example.demo.application.service.GetOrderService.GetOrderQuery;
import com.example.demo.application.service.GetOrderService.GetOrderResult;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

/**
 * Adapter for converting web requests to application queries and results to responses.
 */
@Component
@RequiredArgsConstructor
public class GetOrderAdapter {

    private final GetOrderService getOrderService;

    public record GetOrderResponse(int code, String message, OrderData data) {
        public record OrderData(
                String orderId,
                String orderNumber,
                String userId,
                String merchantId,
                List<OrderItemData> items,
                DeliveryInfoData deliveryInfo,
                String remark,
                String status,
                PricingData pricing,
                String createdAt) {}

        public record OrderItemData(String dishId, String dishName, Integer quantity, BigDecimal price) {}

        public record DeliveryInfoData(String recipientName, String recipientPhone, String address) {}

        public record PricingData(
                BigDecimal itemsTotal, BigDecimal packagingFee, BigDecimal deliveryFee, BigDecimal finalAmount) {}
    }

    public GetOrderResponse getOrder(String orderId, User user) {
        // Extract userId from User
        String userId = user.getUsername();

        // Create GetOrderQuery and call GetOrderService
        GetOrderQuery query = new GetOrderQuery(orderId, userId);
        GetOrderResult result = getOrderService.getOrder(query);

        // Convert GetOrderResult to GetOrderResponse
        return toResponse(result);
    }

    /**
     * Converts a GetOrderResult to a GetOrderResponse.
     *
     * @param result the application result
     * @return the web response
     */
    private GetOrderResponse toResponse(GetOrderResult result) {
        List<GetOrderResponse.OrderItemData> itemDataList = result.items().stream()
                .map(item -> new GetOrderResponse.OrderItemData(
                        item.dishId(), item.dishName(), item.quantity(), item.price()))
                .toList();

        GetOrderResponse.DeliveryInfoData deliveryInfoData = new GetOrderResponse.DeliveryInfoData(
                result.deliveryInfo().recipientName(),
                result.deliveryInfo().recipientPhone(),
                result.deliveryInfo().address());

        GetOrderResponse.PricingData pricingData = new GetOrderResponse.PricingData(
                result.pricing().itemsTotal(),
                result.pricing().packagingFee(),
                result.pricing().deliveryFee(),
                result.pricing().finalAmount());

        GetOrderResponse.OrderData orderData = new GetOrderResponse.OrderData(
                result.orderId(),
                result.orderNumber(),
                result.userId(),
                result.merchantId(),
                itemDataList,
                deliveryInfoData,
                result.remark(),
                result.status(),
                pricingData,
                result.createdAt().toString());

        return new GetOrderResponse(0, "查询成功", orderData);
    }
}
