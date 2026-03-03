package ru.yandex.practicum.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.model.sensor.*;

@Component
@Slf4j
public class SensorEventAvroMapper {
    public SensorEventAvro toSensorEventAvro(SensorEvent event) {
        if (log.isDebugEnabled()) {
            log.debug("Mapping sensor event: id={}, type={}, hubId={}, event={}",
                    event.getId(), event.getType(), event.getHubId(), event);
        }

        SensorEventAvro.Builder builder = SensorEventAvro.newBuilder()
                .setId(event.getId())
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp());

        Object payload = mapPayload(event);
        builder.setPayload(payload);

        return builder.build();
    }

    private Object mapPayload(SensorEvent event) {
        return switch (event) {
            case MotionSensorEvent e -> MotionSensorAvro.newBuilder()
                    .setLinkQuality(e.getLinkQuality())
                    .setMotion(e.getMotion())
                    .setVoltage(e.getVoltage())
                    .build();

            case LightSensorEvent e -> LightSensorAvro.newBuilder()
                    .setLinkQuality(e.getLinkQuality())
                    .setLuminosity(e.getLuminosity())
                    .build();

            case SwitchSensorEvent e -> SwitchSensorAvro.newBuilder()
                    .setState(e.getState())
                    .build();

            case ClimateSensorEvent e -> ClimateSensorAvro.newBuilder()
                    .setCo2Level(e.getCo2Level())
                    .setHumidity(e.getHumidity())
                    .setTemperatureC(e.getTemperatureC())
                    .build();

            case TemperatureSensorEvent e -> TemperatureSensorAvro.newBuilder()
                    .setTemperatureC(e.getTemperatureC())
                    .setTemperatureF(e.getTemperatureF())
                    .build();

            default -> throw new IllegalArgumentException(
                    "No handler for sensor event type: " + event.getClass().getSimpleName()
            );
        };
    }
}
