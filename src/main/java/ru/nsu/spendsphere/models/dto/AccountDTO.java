package ru.nsu.spendsphere.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import ru.nsu.spendsphere.models.entities.AccountType;
import ru.nsu.spendsphere.models.entities.Currency;

@Schema(description = "Информация о счете пользователя")
public record AccountDTO(
    @Schema(description = "Идентификатор счета", example = "1") Long id,
    @Schema(description = "Идентификатор пользователя", example = "1") Long userId,
    @Schema(description = "Тип счета", example = "CARD") AccountType accountType,
    @Schema(description = "Баланс счета", example = "1000.50") BigDecimal balance,
    @Schema(description = "Валюта счета", example = "RUB") Currency currency,
    @Schema(description = "Название счета", example = "Основная карта") String name,
    @Schema(description = "URL иконки счета", example = "https://example.com/icon.png")
        String iconUrl,
    @Schema(description = "Кредитный лимит", example = "50000.00") BigDecimal creditLimit,
    @Schema(description = "Активен ли счет", example = "true") Boolean isActive,
    @Schema(description = "Включать ли в общий баланс", example = "true") Boolean includeInTotal,
    @Schema(description = "Дата и время создания") LocalDateTime createdAt,
    @Schema(description = "Дата и время последнего обновления") LocalDateTime updatedAt) {}
