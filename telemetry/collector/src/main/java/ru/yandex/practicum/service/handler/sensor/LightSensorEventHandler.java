package ru.yandex.practicum.service.handler.sensor;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.model.sensor.LightSensorEvent;
import ru.yandex.practicum.service.EventsService;

@Component
public class LightSensorEventHandler extends AbstractSensorEventHandler implements SensorEventHandler {
    public LightSensorEventHandler(EventsService eventsService) {
        super(eventsService);
    }

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.LIGHT_SENSOR;
    }

    @Override
    public void handle(SensorEventProto eventProto) {
        LightSensorEvent sensorEvent = new LightSensorEvent();
        sensorEvent.setLinkQuality(eventProto.getLightSensor().getLinkQuality());
        sensorEvent.setLuminosity(eventProto.getLightSensor().getLuminosity());
        setSensorEventFields(eventProto, sensorEvent);
    }
}
