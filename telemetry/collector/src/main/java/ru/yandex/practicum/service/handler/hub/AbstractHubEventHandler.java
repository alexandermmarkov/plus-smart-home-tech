package ru.yandex.practicum.service.handler.hub;

import lombok.RequiredArgsConstructor;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.model.hub.HubEvent;
import ru.yandex.practicum.service.EventsService;

import java.time.Instant;

@RequiredArgsConstructor
public abstract class AbstractHubEventHandler {
    private final EventsService eventsService;

    protected void setHubEventFields(HubEventProto proto, HubEvent event) {
        event.setHubId(proto.getHubId());
        event.setTimestamp(Instant.ofEpochSecond(
                proto.getTimestamp().getSeconds(),
                proto.getTimestamp().getNanos()
        ));
        eventsService.sendHubEvent(event);
    }
}
