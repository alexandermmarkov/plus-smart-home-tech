package ru.yandex.practicum.dto;

import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class DimensionDto {
    @NotNull
    @DecimalMin("1.0")
    private Double width;

    @NotNull
    @DecimalMin("1.0")
    private Double height;

    @NotNull
    @DecimalMin("1.0")
    private Double depth;
}
