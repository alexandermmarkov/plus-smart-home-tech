package ru.yandex.practicum.service.handler.hub;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.DeviceTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.model.hub.device.DeviceAddedEvent;
import ru.yandex.practicum.model.hub.device.DeviceType;
import ru.yandex.practicum.service.EventsService;

@Component
public class DeviceAddedEventHandler extends AbstractHubEventHandler implements HubEventHandler {
    public DeviceAddedEventHandler(EventsService eventsService) {
        super(eventsService);
    }

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.DEVICE_ADDED;
    }

    @Override
    public void handle(HubEventProto eventProto) {
        DeviceAddedEvent hubEvent = new DeviceAddedEvent();
        hubEvent.setId(eventProto.getDeviceAdded().getId());

        DeviceTypeProto protoType = eventProto.getDeviceAdded().getType();
        DeviceType javaType = DeviceType.valueOf(protoType.name());
        hubEvent.setDeviceType(javaType);
        setHubEventFields(eventProto, hubEvent);
    }
}
