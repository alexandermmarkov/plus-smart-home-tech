package ru.yandex.practicum.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "warehouse_products", schema = "warehouse")
@Getter
@Setter
@Builder(toBuilder = true)
@NoArgsConstructor
@AllArgsConstructor
public class WarehouseProduct {
    @Id
    @Column(name = "product_id", nullable = false)
    private UUID productId;

    @Column(name = "quantity", nullable = false)
    private Integer quantity = 0;

    @Column(name = "weight", nullable = false, precision = 10, scale = 3)
    private BigDecimal weight;

    @Column(name = "width", nullable = false, precision = 10, scale = 3)
    private BigDecimal width;

    @Column(name = "height", nullable = false, precision = 10, scale = 3)
    private BigDecimal height;

    @Column(name = "depth", nullable = false, precision = 10, scale = 3)
    private BigDecimal depth;

    @Column(name = "fragile", nullable = false)
    private Boolean fragile = false;
}
