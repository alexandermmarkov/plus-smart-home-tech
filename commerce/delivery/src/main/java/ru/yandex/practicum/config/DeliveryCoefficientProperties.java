package ru.yandex.practicum.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;

@Getter
@Setter
@ConfigurationProperties(prefix = "delivery.coefficient")
@Component
public class DeliveryCoefficientProperties {
    private BigDecimal baseCost = BigDecimal.ZERO;
    private BigDecimal address1 = BigDecimal.ZERO;
    private BigDecimal address2 = BigDecimal.ZERO;
    private BigDecimal fragile = BigDecimal.ZERO;
    private BigDecimal weight = BigDecimal.ZERO;
    private BigDecimal volume = BigDecimal.ZERO;
    private BigDecimal deliveryAddress = BigDecimal.ZERO;

}
