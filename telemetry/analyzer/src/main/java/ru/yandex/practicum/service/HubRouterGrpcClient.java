package ru.yandex.practicum.service;

import lombok.RequiredArgsConstructor;
import net.devh.boot.grpc.client.inject.GrpcClient;
import org.springframework.stereotype.Component;
import ru.yandex.practicum.grpc.telemetry.event.DeviceActionRequest;
import ru.yandex.practicum.grpc.telemetry.hubrouter.HubRouterControllerGrpc;

@Component
@RequiredArgsConstructor
public class HubRouterGrpcClient {

    @GrpcClient("hub-router")
    private HubRouterControllerGrpc.HubRouterControllerBlockingStub stub;

    public void sendDeviceAction(DeviceActionRequest request) {
        stub.handleDeviceAction(request);
    }
}
