package ru.practicum.yandex.service;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.avro.specific.SpecificRecordBase;
import org.apache.kafka.clients.consumer.*;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.Producer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.practicum.yandex.configuration.KafkaConfig;
import ru.yandex.practicum.kafka.telemetry.event.SensorEventAvro;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;

import java.time.Duration;
import java.util.*;

@Slf4j
@Component
@RequiredArgsConstructor
public class AggregationStarter {
    private final KafkaConfig kafkaConfig;
    private final SnapshotService snapshotService;
    private static final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

    public void start() {
        log.info("Старт");
        Properties consumerConfig = getConsumerProperties();
        String topic = kafkaConfig.getSensorTopic();
        KafkaConsumer<String, SensorEventAvro> consumer = new KafkaConsumer<>(consumerConfig);
        Properties producerConfig = getProducerProperties();
        Producer<String, SpecificRecordBase> producer = new KafkaProducer<>(producerConfig);
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

        try {
            consumer.subscribe(Collections.singletonList(topic));
            log.info("Подписка на топик: {}", topic);
            while (true) {
                ConsumerRecords<String, SensorEventAvro> records = consumer.poll(Duration.ofMillis(1000));
                if (records.isEmpty()) {
                    continue;
                }
                int count = 0;
                for (ConsumerRecord<String, SensorEventAvro> record : records) {
                    handleRecord(record, producer);
                    manageOffsets(record, count, consumer);
                    count++;
                }
                consumer.commitAsync();
            }
        } catch (WakeupException ignored) {
            log.info("Получено исключение WakeupException");
        } finally {
            try {
                producer.flush();
                consumer.commitSync(currentOffsets);
            } finally {
                log.info("Закрываем консьюмер");
                consumer.close();
                log.info("Закрываем продюсер");
                producer.close();
            }
        }
    }

    private static void manageOffsets(ConsumerRecord<String, SensorEventAvro> record,
                                      int count, KafkaConsumer<String, SensorEventAvro> consumer) {
        // обновляем текущий оффсет для топика-партиции
        currentOffsets.put(
                new TopicPartition(record.topic(), record.partition()),
                new OffsetAndMetadata(record.offset() + 1)
        );
        if (count % 10 == 0) {
            consumer.commitAsync(currentOffsets, (offsets, exception) -> {
                if (exception != null) {
                    log.warn("Ошибка во время фиксации оффсетов: {}", offsets, exception);
                }
            });
        }
    }

    private void handleRecord(ConsumerRecord<String, SensorEventAvro> record, Producer<String, SpecificRecordBase> producer) {
        SensorEventAvro eventAvro = record.value();
        log.info("метод handleRecord, eventAvro = {}", eventAvro);
        Optional<SensorsSnapshotAvro> snapshotAvro = snapshotService.updateState(eventAvro);
        if (snapshotAvro.isPresent()) {
            SensorsSnapshotAvro snapshot = snapshotAvro.get();
            String snapshotTopic = kafkaConfig.getSnapshotTopic();
            ProducerRecord<String, SpecificRecordBase> producerRecord = new ProducerRecord<>(
                    snapshotTopic,
                    null,
                    snapshot.getTimestamp().toEpochMilli(),
                    snapshot.getHubId(),
                    snapshot
            );
            producer.send(producerRecord, (metadata, exception) -> {
                if (exception != null) {
                    log.error("Ошибка при отправке снапшота в топик {}: {}", snapshotTopic, exception.getMessage(), exception);
                } else {
                    log.info("Снапшот успешно отправлен в топик {} на партицию {} со смещением {}",
                            metadata.topic(), metadata.partition(), metadata.offset());
                }
            });
        }
    }

    private Properties getConsumerProperties() {
        Properties config = new Properties();
        config.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getBootstrapServers());
        config.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, kafkaConfig.getConsumer().getKeyDeserializer());
        config.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, kafkaConfig.getConsumer().getValueDeserializer());
        config.put(ConsumerConfig.GROUP_ID_CONFIG, kafkaConfig.getConsumer().getGroupId());
        config.put(ConsumerConfig.CLIENT_ID_CONFIG, kafkaConfig.getConsumer().getClientId());
        KafkaConfig.Consumer.Properties props = kafkaConfig.getConsumer().getProperties();
        if (props.getFetchMinBytes() != null) {
            config.put(ConsumerConfig.FETCH_MIN_BYTES_CONFIG, props.getFetchMinBytes());
        }
        if (props.getMaxPollRecords() != null) {
            config.put(ConsumerConfig.MAX_POLL_RECORDS_CONFIG, props.getMaxPollRecords());
        }
        if (props.getEnableAutoCommit() != null) {
            config.put(ConsumerConfig.ENABLE_AUTO_COMMIT_CONFIG, props.getEnableAutoCommit());
        }
        if (props.getFetchMaxWaitMs() != null) {
            config.put(ConsumerConfig.FETCH_MAX_WAIT_MS_CONFIG, props.getFetchMaxWaitMs());
        }
        if (props.getMaxPartitionFetchBytes() != null) {
            config.put(ConsumerConfig.MAX_PARTITION_FETCH_BYTES_CONFIG, props.getMaxPartitionFetchBytes());
        }

        return config;
    }

    private Properties getProducerProperties() {
        Properties config = new Properties();
        config.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, kafkaConfig.getBootstrapServers());
        config.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, kafkaConfig.getProducer().getKeySerializer());
        config.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, kafkaConfig.getProducer().getValueSerializer());

        return config;
    }
}
