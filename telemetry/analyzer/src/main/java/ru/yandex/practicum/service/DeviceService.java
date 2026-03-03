package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.yandex.practicum.kafka.telemetry.event.DeviceAddedEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.DeviceRemovedEventAvro;
import ru.yandex.practicum.model.Sensor;
import ru.yandex.practicum.repository.SensorRepository;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeviceService {

    private final SensorRepository sensorRepository;

    @Transactional
    public void addDevice(DeviceAddedEventAvro eventAvro, String hubId) {
        String id = eventAvro.getId();
        if (sensorRepository.findByIdAndHubId(id, hubId).isPresent()) {
            log.info("Сенсор {} hub {} уже существует", id, hubId);
            return;
        }
        try {
            Sensor sensor = Sensor.builder()
                    .id(eventAvro.getId())
                    .hubId(hubId)
                    .build();
            sensorRepository.save(sensor);
            log.info("Сенсор {} {} добавлен", id, eventAvro.getType().toString());
        } catch (Exception e) {
            log.error("Ошибка при добавлении нового сенсора {}: {}", id, e.getMessage());
        }
    }

    @Transactional
    public void removeDevice(DeviceRemovedEventAvro eventAvro, String hubId) {
        String id = eventAvro.getId();
        if (sensorRepository.findByIdAndHubId(id, hubId).isPresent()) {
            log.info("Сенсор {} hub {} не существует", id, hubId);
            return;
        }
        try {
            sensorRepository.deleteById(id);
            log.info("Сенсор {} из хаба {} удален", id, hubId);
        } catch (Exception e) {
            log.error("Ошибка при удалении сенсора {}: {}", id, e.getMessage());
        }
    }
}
