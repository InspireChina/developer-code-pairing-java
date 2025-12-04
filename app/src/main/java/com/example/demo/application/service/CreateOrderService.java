package com.example.demo.application.service;

import com.example.demo.application.port.SaveOrderPort;
import com.example.demo.domain.dish.DishId;
import com.example.demo.domain.merchant.MerchantId;
import com.example.demo.domain.order.DeliveryInfo;
import com.example.demo.domain.order.Order;
import com.example.demo.domain.order.OrderItem;
import com.example.demo.domain.user.UserId;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Application service for creating orders.
 */
@Service
@RequiredArgsConstructor
public class CreateOrderService {

    private final SaveOrderPort saveOrderPort;

    public record CreateOrderCommand(
            @NotNull String userId,
            @NotNull String merchantId,
            @NotNull @Size(min = 1) List<@Valid OrderItemDto> items,
            @NotNull @Valid DeliveryInfoDto deliveryInfo,
            @Size(max = 200) String remark) {
        public record OrderItemDto(
                @NotNull String dishId,
                @NotNull String dishName,
                @NotNull @Min(1) Integer quantity,
                @NotNull BigDecimal price) {}

        public record DeliveryInfoDto(
                @NotNull String recipientName,
                @NotNull @Pattern(regexp = "^1[3-9]\\d{9}$") String recipientPhone,
                @NotNull @Size(max = 500) String address) {}
    }

    public record CreateOrderResult(
            String orderId, String orderNumber, String status, PricingDto pricing, Instant createdAt) {
        public record PricingDto(
                BigDecimal itemsTotal, BigDecimal packagingFee, BigDecimal deliveryFee, BigDecimal finalAmount) {}
    }

    @Transactional
    public CreateOrderResult createOrder(CreateOrderCommand command) {
        UserId userId = new UserId(command.userId());
        MerchantId merchantId = new MerchantId(command.merchantId());

        List<OrderItem> items = command.items().stream()
                .map(dto -> new OrderItem(new DishId(dto.dishId()), dto.dishName(), dto.quantity(), dto.price()))
                .toList();

        DeliveryInfo deliveryInfo = new DeliveryInfo(
                command.deliveryInfo().recipientName(),
                command.deliveryInfo().recipientPhone(),
                command.deliveryInfo().address());

        Order order = new Order(userId, merchantId, items, deliveryInfo, command.remark());

        saveOrderPort.save(order);

        return new CreateOrderResult(
                order.getId().value(),
                order.getOrderNumber().value(),
                order.getStatus().name(),
                new CreateOrderResult.PricingDto(
                        order.getPricing().itemsTotal(),
                        order.getPricing().packagingFee(),
                        order.getPricing().deliveryFee(),
                        order.getPricing().finalAmount()),
                order.getCreatedAt());
    }
}
