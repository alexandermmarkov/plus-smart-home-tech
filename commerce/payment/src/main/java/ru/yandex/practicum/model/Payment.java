package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.enums.PaymentState;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "payments", schema = "payment")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class Payment {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "payment_id")
    private UUID paymentId;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "total_payment", precision = 10, scale = 2)
    private BigDecimal totalPayment;

    @Column(name = "delivery_total", precision = 10, scale = 2)
    private BigDecimal deliveryTotal;

    @Column(name = "fee_total", precision = 10, scale = 2)
    private BigDecimal feeTotal;

    @Enumerated(EnumType.STRING)
    @Column(name = "state", nullable = false)
    private PaymentState state;
}
