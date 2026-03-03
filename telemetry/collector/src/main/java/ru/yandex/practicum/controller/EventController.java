package ru.yandex.practicum.controller;

import com.google.protobuf.Empty;
import io.grpc.Status;
import io.grpc.StatusRuntimeException;
import io.grpc.stub.StreamObserver;
import lombok.extern.slf4j.Slf4j;
import net.devh.boot.grpc.server.service.GrpcService;
import ru.yandex.practicum.grpc.telemetry.collector.CollectorControllerGrpc;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.grpc.telemetry.event.SensorEventProto;
import ru.yandex.practicum.service.handler.hub.HubEventHandler;
import ru.yandex.practicum.service.handler.sensor.SensorEventHandler;

import java.util.Map;
import java.util.Set;
import java.util.function.Function;
import java.util.stream.Collectors;

@Slf4j
@GrpcService
public class EventController extends CollectorControllerGrpc.CollectorControllerImplBase {
    private final Map<SensorEventProto.PayloadCase, SensorEventHandler> sensorEventHandlers;
    private final Map<HubEventProto.PayloadCase, HubEventHandler> hubEventHandlers;

    public EventController(Set<SensorEventHandler> sensorEventHandlers, Set<HubEventHandler> hubEventHandlers) {
        this.sensorEventHandlers = sensorEventHandlers.stream()
                .collect(Collectors.toMap(SensorEventHandler::getMessageType, Function.identity()));
        this.hubEventHandlers = hubEventHandlers.stream().collect(Collectors.toMap(HubEventHandler::getMessageType,
                Function.identity()));
    }

    @Override
    public void collectSensorEvent(SensorEventProto request, StreamObserver<Empty> responseObserver) {
        log.info("Получено сообщение collectSensorEvent \n{}", request);
        try {
            SensorEventHandler handler = sensorEventHandlers.get(request.getPayloadCase());
            if (handler == null) {
                throw new IllegalArgumentException("Не найден обработчик датчика " + request.getPayloadCase());
            }
            handler.handle(request);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Ошибка {}", e.getLocalizedMessage());
            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }

    @Override
    public void collectHubEvent(HubEventProto request, StreamObserver<Empty> responseObserver) {
        log.info("Получено сообщение collectHubEvent \n{}", request);
        try {
            HubEventHandler handler = hubEventHandlers.get(request.getPayloadCase());
            if (handler == null) {
                throw new IllegalArgumentException("Не найден обработчик датчика " + request.getPayloadCase());
            }
            handler.handle(request);

            responseObserver.onNext(Empty.getDefaultInstance());
            responseObserver.onCompleted();
        } catch (Exception e) {
            log.error("Ошибка {}", e.getLocalizedMessage());

            responseObserver.onError(new StatusRuntimeException(
                    Status.INTERNAL
                            .withDescription(e.getLocalizedMessage())
                            .withCause(e)
            ));
        }
    }
}
