package ru.nsu.spendsphere.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import ru.nsu.spendsphere.models.entities.AccountType;
import ru.nsu.spendsphere.models.entities.Currency;

@Schema(description = "Данные для создания нового счета")
public record AccountCreateDTO(
    @NotNull(message = "Тип счета обязателен")
        @Schema(description = "Тип счета", example = "CARD", required = true)
        AccountType accountType,
    @Schema(description = "Начальный баланс счета", example = "1000.00") BigDecimal balance,
    @Schema(description = "Валюта счета", example = "RUB") Currency currency,
    @NotBlank(message = "Название счета обязательно")
        @Schema(description = "Название счета", example = "Основная карта", required = true)
        String name,
    @Schema(description = "URL иконки счета", example = "https://example.com/icon.png")
        String iconUrl,
    @Schema(description = "Кредитный лимит (для кредитных карт)", example = "50000.00")
        BigDecimal creditLimit,
    @Schema(description = "Активен ли счет", example = "true") Boolean isActive,
    @Schema(description = "Включать ли в общий баланс", example = "true") Boolean includeInTotal) {}
