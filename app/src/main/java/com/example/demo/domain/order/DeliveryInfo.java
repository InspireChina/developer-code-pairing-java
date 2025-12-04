package com.example.demo.domain.order;

import java.util.regex.Pattern;

public record DeliveryInfo(String recipientName, String recipientPhone, String address) {
    private static final Pattern PHONE_PATTERN = Pattern.compile("^1[3-9]\\d{9}$");
    private static final int MAX_ADDRESS_LENGTH = 500;

    public DeliveryInfo {
        if (recipientName == null || recipientName.isBlank()) {
            throw new IllegalArgumentException("收货人姓名不能为空");
        }
        if (recipientPhone == null || !PHONE_PATTERN.matcher(recipientPhone).matches()) {
            throw new IllegalArgumentException("手机号格式不正确");
        }
        if (address == null || address.isBlank()) {
            throw new IllegalArgumentException("收货地址不能为空");
        }
        if (address.length() > MAX_ADDRESS_LENGTH) {
            throw new IllegalArgumentException("收货地址长度不能超过" + MAX_ADDRESS_LENGTH + "字符");
        }
    }
}
