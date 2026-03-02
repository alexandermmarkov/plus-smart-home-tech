package ru.yandex.practicum.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.yandex.practicum.model.hub.HubEvent;
import ru.yandex.practicum.model.sensor.SensorEvent;
import ru.yandex.practicum.service.EventsService;

@RestController
@RequiredArgsConstructor
@RequestMapping(path = "/events")
@Validated
@Slf4j
public class EventsController {
    private final EventsService eventsService;

    @PostMapping("/hubs")
    public void collectHubEvent(@Valid @RequestBody HubEvent event) {
        if (log.isDebugEnabled()) {
            log.debug("Received hub event: type={}, hubId={}",
                    event.getType(), event.getHubId());
        }
        eventsService.sendHubEvent(event);

    }

    @PostMapping("/sensors")
    public void collectSensorEvent(@Valid @RequestBody SensorEvent event) {
        if (log.isDebugEnabled()) {
            log.debug("Received sensor event: type={}, hubId={}, timestamp={}",
                    event.getType(), event.getHubId(), event.getTimestamp());
        }
        eventsService.sendSensorEvent(event);
    }
}
