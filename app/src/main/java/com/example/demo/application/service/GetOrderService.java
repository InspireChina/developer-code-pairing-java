package com.example.demo.application.service;

import com.example.demo.application.port.LoadOrderPort;
import com.example.demo.domain.order.Order;
import com.example.demo.domain.order.OrderId;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for querying order details.
 */
@Service
@RequiredArgsConstructor
public class GetOrderService {

    private final LoadOrderPort loadOrderPort;

    public record GetOrderQuery(@NotNull String orderId, @NotNull String userId) {}

    public record GetOrderResult(
            String orderId,
            String orderNumber,
            String userId,
            String merchantId,
            List<OrderItemDto> items,
            DeliveryInfoDto deliveryInfo,
            String remark,
            String status,
            PricingDto pricing,
            Instant createdAt) {
        public record OrderItemDto(String dishId, String dishName, Integer quantity, BigDecimal price) {}

        public record DeliveryInfoDto(String recipientName, String recipientPhone, String address) {}

        public record PricingDto(
                BigDecimal itemsTotal, BigDecimal packagingFee, BigDecimal deliveryFee, BigDecimal finalAmount) {}
    }

    @Transactional(readOnly = true)
    public GetOrderResult getOrder(GetOrderQuery query) {
        // Query order by orderId
        Order order = loadOrderPort
                .findById(new OrderId(query.orderId()))
                .orElseThrow(() -> new OrderNotFoundException("订单不存在: " + query.orderId()));

        // Verify order ownership
        if (!order.getUserId().value().equals(query.userId())) {
            throw new OrderNotFoundException("订单不存在: " + query.orderId());
        }

        // Convert Order domain object to GetOrderResult
        return convertToResult(order);
    }

    private GetOrderResult convertToResult(Order order) {
        List<GetOrderResult.OrderItemDto> itemDtos = order.getItems().stream()
                .map(item -> new GetOrderResult.OrderItemDto(
                        item.dishId().value(), item.dishName(), item.quantity(), item.price()))
                .toList();

        GetOrderResult.DeliveryInfoDto deliveryInfoDto = new GetOrderResult.DeliveryInfoDto(
                order.getDeliveryInfo().recipientName(),
                order.getDeliveryInfo().recipientPhone(),
                order.getDeliveryInfo().address());

        GetOrderResult.PricingDto pricingDto = new GetOrderResult.PricingDto(
                order.getPricing().itemsTotal(),
                order.getPricing().packagingFee(),
                order.getPricing().deliveryFee(),
                order.getPricing().finalAmount());

        return new GetOrderResult(
                order.getId().value(),
                order.getOrderNumber().value(),
                order.getUserId().value(),
                order.getMerchantId().value(),
                itemDtos,
                deliveryInfoDto,
                order.getRemark(),
                order.getStatus().name(),
                pricingDto,
                order.getCreatedAt());
    }
}
