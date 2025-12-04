package com.example.demo.application.port;

import com.example.demo.domain.order.Order;

/**
 * Port for saving orders.
 * Outbound port - called by application layer, implemented by adapters.
 */
public interface SaveOrderPort {
    /**
     * Save an order.
     * @param order the order to save
     */
    void save(Order order);
}
