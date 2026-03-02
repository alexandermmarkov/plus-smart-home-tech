package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.mapper.HubEventAvroMapper;
import ru.yandex.practicum.mapper.SensorEventAvroMapper;
import ru.yandex.practicum.model.hub.HubEvent;
import ru.yandex.practicum.model.sensor.SensorEvent;

@Slf4j
@Service
@RequiredArgsConstructor
public class EventsServiceImpl implements EventsService {

    private final HubEventAvroMapper hubEventAvroMapper;
    private final SensorEventAvroMapper sensorEventAvroMapper;
    private final Producer<String, SpecificRecordBase> kafkaProducer;

    @Value("${kafka.collector.sensor-topic}")
    private String sensorTopic;

    @Value("${kafka.collector.hub-topic}")
    private String hubTopic;

    @Override
    public void sendSensorEvent(SensorEvent event) {
        if (log.isDebugEnabled()) {
            log.debug("Sending sensor event: id={}, type={}, hubId={}",
                    event.getId(), event.getType(), event.getHubId());
        }

        SensorEventAvro message = sensorEventAvroMapper.toSensorEventAvro(event);
        sendToKafka(sensorTopic, event.getHubId(), message);
    }

    @Override
    public void sendHubEvent(HubEvent event) {
        if (log.isDebugEnabled()) {
            log.debug("Sending hub event: type={}, hubId={}",
                    event.getType(), event.getHubId());
        }

        HubEventAvro message = hubEventAvroMapper.toHubEventAvro(event);
        sendToKafka(hubTopic, event.getHubId(), message);
    }

    private void sendToKafka(String topic, String key, SpecificRecordBase message) {
        ProducerRecord<String, SpecificRecordBase> record =
                new ProducerRecord<>(topic, key, message);

        kafkaProducer.send(record, (metadata, exception) -> {
            if (exception != null) {
                log.error("Failed to send to {}: key={}", topic, key, exception);
            } else if (log.isDebugEnabled()) {
                log.debug("Sent to {}: partition={}, offset={}",
                        topic, metadata.partition(), metadata.offset());
            }
        });
    }
}
