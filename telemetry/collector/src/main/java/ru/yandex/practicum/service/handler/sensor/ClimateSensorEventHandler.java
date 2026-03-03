package ru.yandex.practicum.service.handler.sensor;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.model.sensor.ClimateSensorEvent;
import ru.yandex.practicum.service.EventsService;

@Component
public class ClimateSensorEventHandler extends AbstractSensorEventHandler implements SensorEventHandler {
    public ClimateSensorEventHandler(EventsService eventsService) {
        super(eventsService);
    }

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.CLIMATE_SENSOR;
    }

    @Override
    public void handle(SensorEventProto eventProto) {
        ClimateSensorEvent sensorEvent = new ClimateSensorEvent();
        sensorEvent.setHumidity(eventProto.getClimateSensor().getHumidity());
        sensorEvent.setTemperatureC(eventProto.getClimateSensor().getTemperatureC());
        sensorEvent.setCo2Level(eventProto.getClimateSensor().getCo2Level());
        setSensorEventFields(eventProto, sensorEvent);
    }
}
