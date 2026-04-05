package ru.yandex.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.contract.OrderContract;

@FeignClient(name = "order", path = "/api/v1/order")
public interface OrderClient extends OrderContract {
}
