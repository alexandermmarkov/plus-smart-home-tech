package ru.yandex.practicum.service.processor;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.apache.kafka.clients.consumer.ConsumerRecords;
import org.apache.kafka.clients.consumer.KafkaConsumer;
import org.apache.kafka.common.errors.WakeupException;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.configuration.KafkaConsumerConfigService;
import ru.yandex.practicum.kafka.telemetry.event.HubEventAvro;
import ru.yandex.practicum.service.HubEventService;

import java.time.Duration;
import java.util.Collections;
import java.util.Properties;

@Slf4j
@Component
@RequiredArgsConstructor
public class HubEventProcessor implements Runnable {
    private final KafkaConsumerConfigService configService;
    private final HubEventService hubEventService;

    @Override
    public void run() {
        Properties props = configService.getConsumerProperties("consumer-hub");
        String topic = configService.getTopic("consumer-hub");
        KafkaConsumer<String, HubEventAvro> consumer = new KafkaConsumer<>(props);
        // Добавить shutdown hook
        Runtime.getRuntime().addShutdownHook(new Thread(consumer::wakeup));

        try {
            consumer.subscribe(Collections.singletonList(topic));
            log.info("Подписка на топик сценариев: {}", topic);
            while (true) {
                ConsumerRecords<String, HubEventAvro> records = consumer.poll(Duration.ofMillis(2000));
                if (records.isEmpty()) {
                    continue;
                }
                for (ConsumerRecord<String, HubEventAvro> record : records) {
                    hubEventService.handleRecord(record);
                }
                consumer.commitAsync();
            }
        } catch (WakeupException ignored) {
            log.info("Получен сигнал завершения (исключение WakeupException)");
        } finally {
            try {
                consumer.commitSync(); // избыточно?
            } finally {
                log.info("Закрываем консьюмер сценариев");
                consumer.close();
            }
        }
    }
}
