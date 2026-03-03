package ru.yandex.practicum.service.handler.hub;

import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.HubEventProto;
import ru.yandex.practicum.model.hub.scenario.ScenarioRemovedEvent;
import ru.yandex.practicum.service.EventsService;

@Component
public class ScenarioRemovedEventHandler extends AbstractHubEventHandler implements HubEventHandler {
    public ScenarioRemovedEventHandler(EventsService eventsService) {
        super(eventsService);
    }

    @Override
    public HubEventProto.PayloadCase getMessageType() {
        return HubEventProto.PayloadCase.SCENARIO_REMOVED;
    }

    @Override
    public void handle(HubEventProto eventProto) {
        ScenarioRemovedEvent hubEvent = new ScenarioRemovedEvent();
        hubEvent.setName(eventProto.getScenarioRemoved().getName());
        setHubEventFields(eventProto, hubEvent);
    }
}
