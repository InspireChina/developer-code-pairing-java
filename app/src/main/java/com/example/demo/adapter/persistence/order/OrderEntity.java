package com.example.demo.adapter.persistence.order;

import com.example.demo.domain.order.OrderStatus;
import jakarta.persistence.AttributeOverride;
import jakarta.persistence.AttributeOverrides;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "orders")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class OrderEntity {
    @Id
    private String id;

    private String orderNumber;
    private String userId;
    private String merchantId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "orderId")
    private List<OrderItemEntity> items = new ArrayList<>();

    @Embedded
    private DeliveryInfoEmbeddable deliveryInfo;

    private String remark;

    @Enumerated(EnumType.STRING)
    private OrderStatus status;

    @Embedded
    @AttributeOverrides({
        @AttributeOverride(name = "itemsTotal", column = @Column(name = "items_total")),
        @AttributeOverride(name = "packagingFee", column = @Column(name = "packaging_fee")),
        @AttributeOverride(name = "deliveryFee", column = @Column(name = "delivery_fee")),
        @AttributeOverride(name = "finalAmount", column = @Column(name = "final_amount"))
    })
    private PricingEmbeddable pricing;

    private Instant createdAt;
    private Instant updatedAt;
}
