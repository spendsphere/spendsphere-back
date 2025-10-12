package ru.nsu.spendsphere.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import ru.nsu.spendsphere.models.entities.AccountType;
import ru.nsu.spendsphere.models.entities.Currency;

@Schema(description = "Данные для обновления счета")
public record AccountUpdateDTO(
    @Schema(description = "Тип счета", example = "CARD") AccountType accountType,
    @Schema(description = "Баланс счета", example = "1000.00") BigDecimal balance,
    @Schema(description = "Валюта счета", example = "RUB") Currency currency,
    @Schema(description = "Название счета", example = "Основная карта") String name,
    @Schema(description = "URL иконки счета", example = "https://example.com/icon.png")
        String iconUrl,
    @Schema(description = "Кредитный лимит", example = "50000.00") BigDecimal creditLimit,
    @Schema(description = "Активен ли счет", example = "true") Boolean isActive,
    @Schema(description = "Включать ли в общий баланс", example = "true") Boolean includeInTotal) {}
