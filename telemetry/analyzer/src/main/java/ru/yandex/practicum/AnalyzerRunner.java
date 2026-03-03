package ru.yandex.practicum;

import jakarta.annotation.PreDestroy;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.ApplicationRunner;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.service.processor.HubEventProcessor;
import ru.yandex.practicum.service.processor.SnapshotProcessor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Slf4j
@Component
@RequiredArgsConstructor
public class AnalyzerRunner implements ApplicationRunner {
    private final HubEventProcessor hubEventProcessor;
    private final SnapshotProcessor snapshotProcessor;
    private final ExecutorService executor = Executors.newSingleThreadExecutor(runnable ->
            new Thread(runnable, "HubEventHandlerThread")
    );
    @Override
    public void run(org.springframework.boot.ApplicationArguments args) throws Exception {
        executor.execute(hubEventProcessor);
        snapshotProcessor.run();
    }
    @PreDestroy
    public void shutdown() {
        log.info("shutdownNow HubEventProcessor executor...");
        executor.shutdownNow();
    }
}
