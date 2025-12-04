package com.example.demo.adapter.persistence.order;

import jakarta.persistence.Embeddable;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Embeddable
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class DeliveryInfoEmbeddable {
    private String recipientName;
    private String recipientPhone;
    private String address;
}
