package ru.yandex.practicum.service.handler.sensor;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.model.sensor.MotionSensorEvent;
import ru.yandex.practicum.service.EventsService;

@Component
public class MotionSensorEventHandler extends AbstractSensorEventHandler implements SensorEventHandler {
    public MotionSensorEventHandler(EventsService eventsService) {
        super(eventsService);
    }

    @Override
    public SensorEventProto.PayloadCase getMessageType() {
        return SensorEventProto.PayloadCase.MOTION_SENSOR;
    }

    @Override
    public void handle(SensorEventProto eventProto) {
        MotionSensorEvent sensorEvent = new MotionSensorEvent();
        sensorEvent.setMotion(eventProto.getMotionSensor().getMotion());
        sensorEvent.setVoltage(eventProto.getMotionSensor().getVoltage());
        sensorEvent.setLinkQuality(eventProto.getMotionSensor().getLinkQuality());
        setSensorEventFields(eventProto, sensorEvent);
    }
}
