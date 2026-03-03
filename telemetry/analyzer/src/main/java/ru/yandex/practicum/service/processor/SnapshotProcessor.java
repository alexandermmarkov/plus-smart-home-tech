package ru.yandex.practicum.service.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.clients.consumer.OffsetAndMetadata;
import org.apache.kafka.common.TopicPartition;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.configuration.KafkaConsumerConfigService;
import ru.yandex.practicum.kafka.telemetry.event.SensorsSnapshotAvro;
import ru.yandex.practicum.service.SnapshotService;

import java.time.Duration;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;

@Slf4j
@Component
@RequiredArgsConstructor
public class SnapshotProcessor implements Runnable {
    private final KafkaConsumerConfigService configService;
    private final SnapshotService snapshotService;
    private final Map<TopicPartition, OffsetAndMetadata> currentOffsets = new HashMap<>();

    @Override
    public void run() {
        Properties props = configService.getConsumerProperties("consumer-snapshot");
        String topic = configService.getTopic("consumer-snapshot");
        KafkaConsumer<String, SensorsSnapshotAvro> consumer = new KafkaConsumer<>(props);
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

        try {
            consumer.subscribe(Collections.singletonList(topic));
            log.info("Подписка на топик снапшотов: {}", topic);

            while (true) {
                ConsumerRecords<String, SensorsSnapshotAvro> records = consumer.poll(Duration.ofMillis(500));
                if (records.isEmpty()) continue;

                for (ConsumerRecord<String, SensorsSnapshotAvro> record : records) {
                    try {
                        // Обрабатываем снапшот
                        snapshotService.handleRecord(record);
                        // Обновляем смещение только после успешной обработки
                        currentOffsets.put(
                                new TopicPartition(record.topic(), record.partition()),
                                new OffsetAndMetadata(record.offset() + 1)
                        );
                    } catch (Exception e) {
                        log.error("Ошибка при обработке снапшота, offset: {}", record.offset(), e);
                    }
                }
                if (!currentOffsets.isEmpty()) {
                    consumer.commitSync(currentOffsets);
                    currentOffsets.clear();
                }
            }
        } catch (WakeupException ignored) {
            log.info("Получен сигнал завершения (исключение WakeupException)");
        } finally {
            try {
                // Финальный коммит перед закрытием
                if (!currentOffsets.isEmpty()) {
                    consumer.commitSync(currentOffsets);
                }
            } finally {
                log.info("Закрываем консьюмер снапшотов");
                consumer.close();
            }
        }
    }
}
