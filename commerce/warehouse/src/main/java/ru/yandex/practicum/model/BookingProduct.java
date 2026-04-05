package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(
        name = "booking_products",
        schema = "warehouse",
        uniqueConstraints = @UniqueConstraint(columnNames = {"booking_id", "product_id"})
)
@Getter
@Setter
@NoArgsConstructor
public class BookingProduct {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "booking_id", nullable = false)
    private OrderBooking booking;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "product_id", nullable = false)
    private WarehouseProduct product;

    @Column(name = "quantity", nullable = false)
    private Integer quantity;

}
