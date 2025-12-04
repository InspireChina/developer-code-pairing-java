package com.example.demo.application.port;

import com.example.demo.domain.order.Order;
import com.example.demo.domain.order.OrderId;
import java.util.Optional;

/**
 * Port for loading orders.
 * Outbound port - called by application layer, implemented by adapters.
 */
public interface LoadOrderPort {
    /**
     * Find an order by its ID.
     * @param orderId the order ID
     * @return Optional containing the order if found
     */
    Optional<Order> findById(OrderId orderId);
}
