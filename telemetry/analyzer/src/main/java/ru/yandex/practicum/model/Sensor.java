package ru.yandex.practicum.model;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.*;

@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "sensors")
public class Sensor {
    @Id
    @Column(nullable = false)
    private String id;

    @Column(name = "hub_id", nullable = false)
    private String hubId;
}
