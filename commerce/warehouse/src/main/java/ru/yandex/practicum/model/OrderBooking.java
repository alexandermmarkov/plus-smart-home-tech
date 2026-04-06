package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "bookings", schema = "warehouse")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class OrderBooking {
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "booking_id")
    private UUID bookingId;

    @Column(name = "order_id")
    private UUID orderId;

    @Column(name = "delivery_id")
    private UUID deliveryId;

    @OneToMany(mappedBy = "booking", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<BookingProduct> bookingProducts = new ArrayList<>();

    @Column(name = "delivery_weight")
    private BigDecimal deliveryWeight;

    @Column(name = "delivery_volume")
    private BigDecimal deliveryVolume;

    @Column(name = "fragile")
    private Boolean fragile;

    public void addBookingProduct(WarehouseProduct product, Integer quantity) {
        BookingProduct bp = new BookingProduct();
        bp.setProduct(product);
        bp.setQuantity(quantity);
        bp.setBooking(this);

        this.bookingProducts.add(bp);
    }
}
