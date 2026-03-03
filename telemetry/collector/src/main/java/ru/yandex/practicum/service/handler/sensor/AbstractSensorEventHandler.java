package ru.yandex.practicum.service.handler.sensor;

import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.model.sensor.SensorEvent;
import ru.yandex.practicum.service.EventsService;

import java.time.Instant;

@RequiredArgsConstructor
public abstract class AbstractSensorEventHandler {
    private final EventsService eventsService;

    protected void setSensorEventFields(SensorEventProto proto, SensorEvent event) {
        event.setId(proto.getId());
        event.setHubId(proto.getHubId());
        event.setTimestamp(Instant.ofEpochSecond(
                proto.getTimestamp().getSeconds(),
                proto.getTimestamp().getNanos()
        ));
        eventsService.sendSensorEvent(event);
    }
}
