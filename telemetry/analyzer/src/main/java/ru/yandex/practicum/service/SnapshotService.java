package ru.yandex.practicum.service;

import com.google.protobuf.Timestamp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.grpc.telemetry.event.ActionTypeProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionProto;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.kafka.telemetry.event.*;
import ru.yandex.practicum.model.Action;
import ru.yandex.practicum.model.Condition;
import ru.yandex.practicum.model.Scenario;
import ru.yandex.practicum.repository.ScenarioRepository;

import java.time.Instant;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class SnapshotService {
    private final ScenarioRepository scenarioRepository;
    private final HubRouterGrpcClient grpcClient;

    public void handleRecord(ConsumerRecord<String, SensorsSnapshotAvro> record) {
        log.info("Snapshot handleRecord key: {}", record.key());
        SensorsSnapshotAvro sensorsSnapshotAvro = record.value();
        String hubId = sensorsSnapshotAvro.getHubId();

        List<Scenario> scenarios = scenarioRepository.findByHubId(hubId);
        if (scenarios.isEmpty()) {
            log.info("Не найдены сценарии для хаба {}", hubId);
            return;
        }

        for (Scenario scenario : scenarios) {
            if (analyzeScenario(scenario, sensorsSnapshotAvro)) {
                log.info("Условия сценария '{}' для хаба {} выполняются", scenario.getName(), sensorsSnapshotAvro.getHubId());
                executeActions(scenario, sensorsSnapshotAvro);
            } else {
                log.info("Условия сценария '{}' для хаба {} не выполняются", scenario.getName(), sensorsSnapshotAvro.getHubId());
            }
        }

    }

    private Boolean analyzeScenario(Scenario scenario, SensorsSnapshotAvro snapshot) {
        // все условия сценария scenario
        Map<String, Condition> conditions = scenario.getConditions();
        // набор состояний снапшота , где ключ - id устройства
        Map<String, SensorStateAvro> sensorsState = snapshot.getSensorsState();

        boolean allConditionsTrue = true;
        // перебираем все условия сценария scenario
        for (Map.Entry<String, Condition> entry : conditions.entrySet()) {
            String sensorId = entry.getKey();
            Condition condition = entry.getValue();
            // состояние датчика из снапшота
            SensorStateAvro sensorState = sensorsState.get(sensorId);
            if (sensorState == null) {
                log.error("Состояние сенсора {} не найдено в снапшоте для сценария {}", sensorId, scenario.getName());
                allConditionsTrue = false;
                break;
            }

            Integer sensorValue = getValueFromSensorState(sensorState, condition.getType());
            if (sensorValue == null) {
                log.error("Не удалось получить значение для типа {} из сенсора {}", condition.getType(), sensorId);
                allConditionsTrue = false;
                break;
            }

            if (!condition.check(sensorValue)) {
                allConditionsTrue = false;
                break;
            }
        }
        return allConditionsTrue;

    }

    private Integer getValueFromSensorState(SensorStateAvro sensorState, ConditionTypeAvro conditionType) {
        Object data = sensorState.getData();

        return switch (conditionType) {
            case TEMPERATURE -> switch (data) {
                case TemperatureSensorAvro temp -> temp.getTemperatureC();
                case ClimateSensorAvro climate -> climate.getTemperatureC();
                default -> null;
            };
            case HUMIDITY -> (data instanceof ClimateSensorAvro climate) ? climate.getHumidity() : null;
            case CO2LEVEL -> (data instanceof ClimateSensorAvro climate) ? climate.getCo2Level() : null;
            case LUMINOSITY -> (data instanceof LightSensorAvro light) ? light.getLuminosity() : null;
            case MOTION -> (data instanceof MotionSensorAvro motion) ? (motion.getMotion() ? 1 : 0) : null;
            case SWITCH -> (data instanceof SwitchSensorAvro sw) ? (sw.getState() ? 1 : 0) : null;
        };

    }

    private void executeActions(Scenario scenario, SensorsSnapshotAvro snapshot) {
        Map<String, Action> actions = scenario.getActions();

        for (Map.Entry<String, Action> entry : actions.entrySet()) {
            String sensorId = entry.getKey();
            Action action = entry.getValue();

            // Преобразуем Action в DeviceActionProto
            DeviceActionProto.Builder actionBuilder = DeviceActionProto.newBuilder()
                    .setSensorId(sensorId)
                    .setType(ActionTypeProto.valueOf(action.getType().toString()));

            if (action.getValue() != null) {
                actionBuilder.setValue(action.getValue());
            }

            DeviceActionRequest request = DeviceActionRequest.newBuilder()
                    .setHubId(snapshot.getHubId())
                    .setScenarioName(scenario.getName())
                    .setAction(actionBuilder.build())
                    .setTimestamp(Timestamp.newBuilder()
                            .setSeconds(Instant.now().getEpochSecond())
                            .setNanos(Instant.now().getNano())
                            .build())
                    .build();

            try {
                // Отправляем gRPC-запрос
                grpcClient.sendDeviceAction(request);
                log.info("Отправлено действие для сценария '{}', сенсор {}", scenario.getName(), sensorId);
            } catch (Exception e) {
                log.error("Ошибка при отправке действия через gRPC", e);
            }
        }

    }

}
