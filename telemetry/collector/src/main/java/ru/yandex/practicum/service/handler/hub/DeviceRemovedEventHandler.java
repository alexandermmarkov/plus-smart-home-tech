package ru.yandex.practicum.service.handler.hub;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.model.hub.device.DeviceRemovedEvent;
import ru.yandex.practicum.service.EventsService;

@Component
public class DeviceRemovedEventHandler extends AbstractHubEventHandler implements HubEventHandler {
    public DeviceRemovedEventHandler(EventsService eventsService) {
        super(eventsService);
    }

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.DEVICE_REMOVED;
    }

    @Override
    public void handle(HubEventProto eventProto) {
        DeviceRemovedEvent hubEvent = new DeviceRemovedEvent();
        hubEvent.setId(eventProto.getDeviceRemoved().getId());
        setHubEventFields(eventProto, hubEvent);
    }
}
