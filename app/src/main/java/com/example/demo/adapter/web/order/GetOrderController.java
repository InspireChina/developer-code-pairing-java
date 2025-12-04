package com.example.demo.adapter.web.order;

import com.example.demo.adapter.web.order.adapter.GetOrderAdapter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.User;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST controller for order retrieval.
 */
@RestController
@RequestMapping("/api/v1")
@RequiredArgsConstructor
public class GetOrderController {

    private final GetOrderAdapter getOrderAdapter;

    @GetMapping("/orders/{orderId}")
    @ResponseStatus(HttpStatus.OK)
    public GetOrderAdapter.GetOrderResponse getOrder(@PathVariable String orderId, @AuthenticationPrincipal User user) {
        return getOrderAdapter.getOrder(orderId, user);
    }
}
