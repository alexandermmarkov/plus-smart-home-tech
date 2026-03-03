package ru.yandex.practicum.mapper;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.model.hub.HubEvent;
import ru.yandex.practicum.model.hub.device.DeviceAction;
import ru.yandex.practicum.model.hub.device.DeviceAddedEvent;
import ru.yandex.practicum.model.hub.device.DeviceRemovedEvent;
import ru.yandex.practicum.model.hub.scenario.ScenarioAddedEvent;
import ru.yandex.practicum.model.hub.scenario.ScenarioCondition;
import ru.yandex.practicum.model.hub.scenario.ScenarioRemovedEvent;

import java.util.Collections;
import java.util.List;

@Component
@Slf4j
public class HubEventAvroMapper {
    public HubEventAvro toHubEventAvro(HubEvent event) {
        if (log.isDebugEnabled()) {
            log.debug("Mapping hub event: type={}, hubId={}, timestamp={}, details={}",
                    event.getType(), event.getHubId(), event.getTimestamp(), event);
        }

        HubEventAvro.Builder builder = HubEventAvro.newBuilder()
                .setHubId(event.getHubId())
                .setTimestamp(event.getTimestamp());

        Object payload = mapPayload(event);
        builder.setPayload(payload);

        return builder.build();
    }

    private Object mapPayload(HubEvent event) {
        return switch (event) {
            case DeviceAddedEvent e -> DeviceAddedEventAvro.newBuilder()
                    .setId(e.getId())
                    .setType(DeviceTypeAvro.valueOf(e.getDeviceType().name()))
                    .build();

            case DeviceRemovedEvent e -> DeviceRemovedEventAvro.newBuilder()
                    .setId(e.getId())
                    .build();

            case ScenarioAddedEvent e -> ScenarioAddedEventAvro.newBuilder()
                    .setName(e.getName())
                    .setConditions(mapConditions(e.getConditions()))
                    .setActions(mapActions(e.getActions()))
                    .build();

            case ScenarioRemovedEvent e -> ScenarioRemovedEventAvro.newBuilder()
                    .setName(e.getName())
                    .build();

            default -> throw new IllegalArgumentException(
                    "No handler for sensor event type: " + event.getClass().getSimpleName()
            );
        };
    }

    private List<ScenarioConditionAvro> mapConditions(List<ScenarioCondition> conditions) {
        if (conditions == null) {
            return Collections.emptyList();
        }
        return conditions.stream()
                .map(this::toScenarioConditionAvro)
                .toList();
    }

    private List<DeviceActionAvro> mapActions(List<DeviceAction> actions) {
        if (actions == null) {
            return Collections.emptyList();
        }
        return actions.stream()
                .map(this::toDeviceActionAvro)
                .toList();
    }

    private DeviceActionAvro toDeviceActionAvro(DeviceAction action) {
        return DeviceActionAvro.newBuilder()
                .setSensorId(action.getSensorId())
                .setValue(action.getValue())
                .setType(ActionTypeAvro.valueOf(action.getType().name()))
                .build();
    }

    private ScenarioConditionAvro toScenarioConditionAvro(ScenarioCondition condition) {
        return ScenarioConditionAvro.newBuilder()
                .setSensorId(condition.getSensorId())
                .setType(ConditionTypeAvro.valueOf(condition.getType().name()))
                .setOperation(ConditionOperationAvro.valueOf(condition.getOperation().name()))
                .setValue(condition.getValue())
                .build();
    }
}
