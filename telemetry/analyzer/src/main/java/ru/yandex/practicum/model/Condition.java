package ru.yandex.practicum.model;

import jakarta.persistence.*;
import lombok.*;
import ru.yandex.practicum.kafka.telemetry.event.ConditionOperationAvro;
import ru.yandex.practicum.kafka.telemetry.event.ConditionTypeAvro;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "conditions")
public class Condition {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    private ConditionTypeAvro type;

    @Enumerated(EnumType.STRING)
    private ConditionOperationAvro operation;

    private Integer value;

    @Transient
    public boolean check(Integer sensorValue) {
        if (sensorValue == null || value == null) return false;
        return switch (operation) {
            case EQUALS -> sensorValue.equals(value);
            case GREATER_THAN -> sensorValue > value;
            case LOWER_THAN -> sensorValue < value;
        };
    }
}
