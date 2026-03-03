package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class HubEventService {
    private final DeviceService deviceService;
    private final ScenarioService scenarioService;

    public void handleRecord(ConsumerRecord<String, HubEventAvro> record) {
        log.info("Hub handleRecord key: {}", record.key());
        HubEventAvro hubEventAvro = record.value();
        String hubId = hubEventAvro.getHubId();
        Object payload = hubEventAvro.getPayload();
        if (payload == null) {
            log.warn("Payload is null");
            return;
        }
        switch (payload) {
            case DeviceAddedEventAvro event -> deviceService.addDevice(event, hubId);
            case DeviceRemovedEventAvro event -> deviceService.removeDevice(event, hubId);
            case ScenarioAddedEventAvro event -> scenarioService.addScenario(event, hubId);
            case ScenarioRemovedEventAvro event -> scenarioService.removeScenario(event, hubId);
            default -> log.warn("Неизвестный тип payload: {}", payload.getClass().getName());
        }
    }

}
