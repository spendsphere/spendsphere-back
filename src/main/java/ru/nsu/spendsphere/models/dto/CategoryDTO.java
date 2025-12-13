package ru.nsu.spendsphere.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import ru.nsu.spendsphere.models.entities.CategoryType;

@Schema(description = "Информация о категории")
public record CategoryDTO(
    @Schema(description = "Идентификатор категории", example = "1") Long id,
    @Schema(description = "Название категории", example = "Продукты") String name,
    @Schema(description = "Иконка категории (эмодзи)", example = "🍔") String icon,
    @Schema(description = "Цвет категории (hex)", example = "#10b981") String color,
    @Schema(description = "Флаг дефолтной категории", example = "false") Boolean isDefault,
    @Schema(description = "Тип категории", example = "EXPENSE") CategoryType categoryType) {}
