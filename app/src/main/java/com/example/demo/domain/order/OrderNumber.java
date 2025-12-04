package com.example.demo.domain.order;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.concurrent.ThreadLocalRandom;

public record OrderNumber(String value) {
    public OrderNumber() {
        this(generateOrderNumber());
    }

    private static String generateOrderNumber() {
        String timestamp = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyyMMddHHmmss"));
        String random = String.format("%06d", ThreadLocalRandom.current().nextInt(1000000));
        return timestamp + random;
    }
}
