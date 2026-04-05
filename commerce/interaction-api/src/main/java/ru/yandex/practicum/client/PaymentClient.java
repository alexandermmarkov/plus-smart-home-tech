package ru.yandex.practicum.client;

import org.springframework.cloud.openfeign.FeignClient;
import ru.yandex.practicum.contract.PaymentContract;

@FeignClient(name = "payment", path = "/api/v1/payment")
public interface PaymentClient extends PaymentContract {
}
