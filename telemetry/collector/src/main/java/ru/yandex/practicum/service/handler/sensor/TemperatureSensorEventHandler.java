package ru.yandex.practicum.service.handler.sensor;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.model.sensor.TemperatureSensorEvent;
import ru.yandex.practicum.service.EventsService;

@Component
public class TemperatureSensorEventHandler extends AbstractSensorEventHandler implements SensorEventHandler {
    public TemperatureSensorEventHandler(EventsService eventsService) {
        super(eventsService);
    }

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.TEMPERATURE_SENSOR;
    }

    @Override
    public void handle(SensorEventProto eventProto) {
        TemperatureSensorEvent sensorEvent = new TemperatureSensorEvent();
        sensorEvent.setTemperatureC(eventProto.getTemperatureSensor().getTemperatureC());
        sensorEvent.setTemperatureF(eventProto.getTemperatureSensor().getTemperatureF());
        setSensorEventFields(eventProto, sensorEvent);
    }
}
