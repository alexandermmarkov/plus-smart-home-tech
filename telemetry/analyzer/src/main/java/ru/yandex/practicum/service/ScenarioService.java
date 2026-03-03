package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.model.Action;
import ru.yandex.practicum.model.Condition;
import ru.yandex.practicum.model.Scenario;
import ru.yandex.practicum.model.Sensor;
import ru.yandex.practicum.repository.ActionRepository;
import ru.yandex.practicum.repository.ConditionRepository;
import ru.yandex.practicum.repository.ScenarioRepository;
import ru.yandex.practicum.repository.SensorRepository;

import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScenarioService {

    private final ScenarioRepository scenarioRepository;
    private final ActionRepository actionRepository;
    private final ConditionRepository conditionRepository;
    private final SensorRepository sensorRepository;

    @Transactional
    public void addScenario(ScenarioAddedEventAvro scenarioAddedEventAvro, String hubId) {
        String name = scenarioAddedEventAvro.getName();
        log.info("Добавление сценария {}, хаб {}", name, hubId);

        if (scenarioRepository.findByHubIdAndName(hubId, name).isPresent()) {
            log.warn("Сценарий {} уже существует в хабе {}", name, hubId);
            return;
        }

        Set<String> hubSensorIds = sensorRepository.findAllByHubId(hubId).stream()
                .map(Sensor::getId)
                .collect(Collectors.toSet());

        Scenario scenario = Scenario.builder()
                .hubId(hubId)
                .name(name)
                .build();

        addConditions(scenarioAddedEventAvro, hubSensorIds, scenario);
        addActions(scenarioAddedEventAvro, hubSensorIds, scenario);

        scenarioRepository.save(scenario);
        log.info("Сценарий {} для хаба {} добавлен", name, hubId);
    }

    private void addConditions(ScenarioAddedEventAvro scenarioAddedEventAvro,
                               Set<String> hubSensorIds,
                               Scenario scenario) {
        for (ScenarioConditionAvro scenarioConditionAvro : scenarioAddedEventAvro.getConditions()) {
            String sensorId = scenarioConditionAvro.getSensorId();

            if (!hubSensorIds.contains(sensorId)) {
                log.warn("Несуществующий сенсор: {}", sensorId);
                continue;
            }

            if (isInvalidEnum(scenarioConditionAvro.getType(), ConditionTypeAvro.class)) {
                log.warn("Недопустимый тип условия: {}", scenarioConditionAvro.getType());
                continue;
            }

            if (isInvalidEnum(scenarioConditionAvro.getOperation(), ConditionOperationAvro.class)) {
                log.warn("Недопустимая операция условия: {}", scenarioConditionAvro.getOperation());
                continue;
            }

            Integer value = mapValue(scenarioConditionAvro.getValue());

            Condition condition = Condition.builder()
                    .type(scenarioConditionAvro.getType())
                    .operation(scenarioConditionAvro.getOperation())
                    .value(value)
                    .build();

            scenario.addCondition(sensorId, condition);
        }
    }

    private void addActions(ScenarioAddedEventAvro scenarioAddedEventAvro,
                            Set<String> hubSensorIds,
                            Scenario scenario) {
        for (DeviceActionAvro deviceActionAvro : scenarioAddedEventAvro.getActions()) {
            String sensorId = deviceActionAvro.getSensorId();

            if (!hubSensorIds.contains(sensorId)) {
                log.warn("Несуществующий сенсор: {}", sensorId);
                continue;
            }

            if (isInvalidEnum(deviceActionAvro.getType(), ActionTypeAvro.class)) {
                log.warn("Недопустимый тип действия: {}", deviceActionAvro.getType());
                continue;
            }

            Action action = Action.builder()
                    .type(deviceActionAvro.getType())
                    .value(deviceActionAvro.getValue())
                    .build();

            scenario.addAction(sensorId, action);
        }
    }

    private <T extends Enum<T>> boolean isInvalidEnum(Object value, Class<T> enumClass) {
        if (value == null) return true;
        try {
            Enum.valueOf(enumClass, value.toString());
            return false;
        } catch (IllegalArgumentException e) {
            return true;
        }
    }

    private Integer mapValue(Object value) {
        switch (value) {
            case Integer i -> {
                return i;
            }
            case Boolean b -> {
                return b ? 1 : 0;
            }
            case null -> {
                return null;
            }
            default -> {
                log.warn("Неподдерживаемый тип value в условии: {}", value.getClass());
                return null;
            }
        }
    }

    @Transactional
    public void removeScenario(ScenarioRemovedEventAvro eventAvro, String hubId) {
        String name = eventAvro.getName();
        log.info("Удаление сценария {}, хаб {}", name, hubId);
        try {
            scenarioRepository.findByHubIdAndName(hubId, name).ifPresent(scenario -> {
                conditionRepository.deleteAll(scenario.getConditions().values()); // избыточно?
                actionRepository.deleteAll(scenario.getActions().values()); // избыточно?
                scenarioRepository.delete(scenario);
            });
            log.info("Сценарий {} хаб {} удален", name, hubId);
        } catch (Exception e) {
            log.error("Ошибка при удалении сценария: {} хаб {}: {}", name, hubId, e.getMessage());
        }
    }
}
