package ru.yandex.practicum.service.handler.sensor;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.model.sensor.SwitchSensorEvent;
import ru.yandex.practicum.service.EventsService;

@Component
public class SwitchSensorEventHandler extends AbstractSensorEventHandler implements SensorEventHandler {
    public SwitchSensorEventHandler(EventsService eventsService) {
        super(eventsService);
    }

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.SWITCH_SENSOR;
    }

    @Override
    public void handle(SensorEventProto eventProto) {
        SwitchSensorEvent sensorEvent = new SwitchSensorEvent();
        sensorEvent.setState(eventProto.getSwitchSensor().getState());
        setSensorEventFields(eventProto, sensorEvent);
    }
}
