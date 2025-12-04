package com.example.demo.adapter.web.order.adapter;

import com.example.demo.adapter.web.order.CreateOrderController.CreateOrderRequest;
import com.example.demo.application.service.CreateOrderService;
import com.example.demo.application.service.CreateOrderService.CreateOrderCommand;
import com.example.demo.application.service.CreateOrderService.CreateOrderResult;
import java.math.BigDecimal;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.stereotype.Component;

/**
 * Adapter for converting web requests to application commands and results to responses.
 */
@Component
@RequiredArgsConstructor
public class CreateOrderAdapter {

    private final CreateOrderService createOrderService;

    public record CreateOrderResponse(int code, String message, OrderData data) {
        public record OrderData(
                String orderId, String orderNumber, String status, PricingData pricing, String createdAt) {}

        public record PricingData(
                BigDecimal itemsTotal, BigDecimal packagingFee, BigDecimal deliveryFee, BigDecimal finalAmount) {}
    }

    public CreateOrderResponse createOrder(CreateOrderRequest request, User user) {
        // Convert request to command
        CreateOrderCommand command = toCommand(request, user);

        // Execute the command
        CreateOrderResult result = createOrderService.createOrder(command);

        // Convert result to response
        return toResponse(result);
    }

    /**
     * Converts a CreateOrderRequest to a CreateOrderCommand.
     *
     * @param request the web request
     * @param user the authenticated user
     * @return the application command
     */
    private CreateOrderCommand toCommand(CreateOrderRequest request, User user) {
        List<CreateOrderCommand.OrderItemDto> itemDtos = request.items().stream()
                .map(item -> new CreateOrderCommand.OrderItemDto(
                        item.dishId(), item.dishName(), item.quantity(), item.price()))
                .toList();

        CreateOrderCommand.DeliveryInfoDto deliveryInfoDto = new CreateOrderCommand.DeliveryInfoDto(
                request.deliveryInfo().recipientName(),
                request.deliveryInfo().recipientPhone(),
                request.deliveryInfo().address());

        return new CreateOrderCommand(
                user.getUsername(), request.merchantId(), itemDtos, deliveryInfoDto, request.remark());
    }

    /**
     * Converts a CreateOrderResult to a CreateOrderResponse.
     *
     * @param result the application result
     * @return the web response
     */
    private CreateOrderResponse toResponse(CreateOrderResult result) {
        CreateOrderResponse.PricingData pricingData = new CreateOrderResponse.PricingData(
                result.pricing().itemsTotal(),
                result.pricing().packagingFee(),
                result.pricing().deliveryFee(),
                result.pricing().finalAmount());

        CreateOrderResponse.OrderData orderData = new CreateOrderResponse.OrderData(
                result.orderId(),
                result.orderNumber(),
                result.status(),
                pricingData,
                result.createdAt().toString());

        return new CreateOrderResponse(0, "订单创建成功", orderData);
    }
}
