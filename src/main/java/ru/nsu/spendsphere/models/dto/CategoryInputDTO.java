package ru.nsu.spendsphere.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;

@Schema(description = "Данные для создания или обновления категории")
public record CategoryInputDTO(
    @NotBlank(message = "Название категории обязательно")
        @Schema(description = "Название категории", example = "Транспорт", required = true)
        String name,
    @Schema(
            description = "URL иконки категории",
            example = "https://example.com/transport-icon.png")
        String iconUrl) {}
