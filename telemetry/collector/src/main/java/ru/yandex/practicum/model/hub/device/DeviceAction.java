package ru.yandex.practicum.model.hub.device;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;

@Getter
@Setter
@ToString
public class DeviceAction {
    @NotBlank
    private String sensorId;
    @NotNull
    private ActionType type;
    private int value;
}
