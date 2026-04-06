package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.enums.OrderState;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "orders", schema = "ordering")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Order {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "cart_id", nullable = false)
    private UUID shoppingCartId;

    @Column(name = "user_name")
    private String userName;

    @OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<OrderItem> products = new ArrayList<>();

    @Column(name = "payment_id")
    private UUID paymentId;
    @Column(name = "delivery_id")
    private UUID deliveryId;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_state", nullable = false)
    private OrderState orderState;

    @Column(name = "delivery_weight", nullable = false)
    private Double deliveryWeight;

    @Column(name = "delivery_volume", nullable = false)
    private Double deliveryVolume;

    @Column(name = "fragile", nullable = false)
    private Boolean fragile;

    @Column(name = "total_price", nullable = false)
    private BigDecimal totalPrice;

    @Column(name = "delivery_price", nullable = false)
    private BigDecimal deliveryPrice;

    @Column(name = "product_price", nullable = false)
    private BigDecimal productPrice;
}
