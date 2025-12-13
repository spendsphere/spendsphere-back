package ru.nsu.spendsphere.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import ru.nsu.spendsphere.models.entities.CategoryType;

@Schema(description = "Данные для создания или обновления категории")
public record CategoryInputDTO(
    @NotBlank(message = "Название категории обязательно")
        @Schema(description = "Название категории", example = "Транспорт", required = true)
        String name,
    @Schema(description = "Иконка категории (эмодзи)", example = "🚗") String icon,
    @Schema(description = "Цвет категории (hex)", example = "#8b5cf6") String color,
    @Schema(description = "Тип категории", example = "EXPENSE")
        CategoryType categoryType) {}
