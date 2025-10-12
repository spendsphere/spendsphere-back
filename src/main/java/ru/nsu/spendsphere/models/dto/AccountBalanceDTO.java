package ru.nsu.spendsphere.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.util.Map;
import ru.nsu.spendsphere.models.entities.Currency;

@Schema(description = "Баланс по всем активным счетам пользователя")
public record AccountBalanceDTO(
    @Schema(description = "Общее количество активных счетов", example = "3") Integer totalAccounts,
    @Schema(description = "Балансы, сгруппированные по валютам")
        Map<Currency, BigDecimal> balancesByCurrency) {}
