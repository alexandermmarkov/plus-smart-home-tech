package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.model.Action;
import ru.yandex.practicum.model.Condition;
import ru.yandex.practicum.model.Scenario;
import ru.yandex.practicum.repository.ActionRepository;
import ru.yandex.practicum.repository.ConditionRepository;
import ru.yandex.practicum.repository.ScenarioRepository;
import ru.yandex.practicum.repository.SensorRepository;

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

        // Проверить, что сценарий с таким именем в хабе уже существует
        if (scenarioRepository.findByHubIdAndName(hubId, name).isPresent()) {
            log.warn("Сценарий {} уже существует в хабе {}", name, hubId);
            return;
        }

        Scenario scenario = Scenario.builder()
                .hubId(hubId)
                .name(name)
                .build();

        // Обработка условий
        addConditions(scenarioAddedEventAvro, hubId, scenario);
        // Обработка действий
        addActions(scenarioAddedEventAvro, hubId, scenario);

        scenarioRepository.save(scenario);
        log.info("Сценарий {} для хаба {} добавлен", name, hubId);

    }

    private void addConditions(ScenarioAddedEventAvro scenarioAddedEventAvro, String hubId, Scenario scenario) {
        for (ScenarioConditionAvro scenarioConditionAvro : scenarioAddedEventAvro.getConditions()) {
            String sensorId = scenarioConditionAvro.getSensorId();
            if (sensorRepository.findByIdAndHubId(sensorId, hubId).isEmpty()) {
                log.warn("Несуществующий сенсор: {}", sensorId);
                continue;
            }

            if (!isValidEnum(scenarioConditionAvro.getType(), ConditionTypeAvro.class)) {
                log.warn("Недопустимый тип условия: {}", scenarioConditionAvro.getType());
                continue;
            }

            if (!isValidEnum(scenarioConditionAvro.getOperation(), ConditionOperationAvro.class)) {
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

    private void addActions(ScenarioAddedEventAvro scenarioAddedEventAvro, String hubId, Scenario scenario) {
        for (DeviceActionAvro deviceActionAvro : scenarioAddedEventAvro.getActions()) {
            String sensorId = deviceActionAvro.getSensorId();
            if (sensorRepository.findByIdAndHubId(sensorId, hubId).isEmpty()) {
                log.warn("Несуществующий сенсор: {}", sensorId);
                continue;
            }

            if (!isValidEnum(deviceActionAvro.getType(), ActionTypeAvro.class)) {
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

    private <T extends Enum<T>> boolean isValidEnum(Object value, Class<T> enumClass) {
        if (value == null) return false;
        try {
            Enum.valueOf(enumClass, value.toString());
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private Integer mapValue(Object value) {
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Boolean) {
            return (Boolean) value ? 1 : 0;
        } else if (value == null) {
            return null;
        } else {
            log.warn("Неподдерживаемый тип value в условии: {}", value.getClass());
            return null;
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
