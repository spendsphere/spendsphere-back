package ru.nsu.spendsphere.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import ru.nsu.spendsphere.models.entities.TransactionType;

@Schema(description = "Данные для создания новой транзакции")
public record TransactionCreateDTO(
    @NotNull(message = "Тип транзакции обязателен")
        @Schema(description = "Тип транзакции", example = "EXPENSE", required = true)
        TransactionType type,
    @Schema(description = "Идентификатор категории", example = "5") Long categoryId,
    @NotNull(message = "Счет обязателен")
        @Schema(description = "Идентификатор счета", example = "2", required = true)
        Long accountId,
    @Schema(
            description = "Идентификатор счета для перевода (обязателен для TRANSFER)",
            example = "3")
        Long transferAccountId,
    @NotNull(message = "Сумма обязательна")
        @Positive(message = "Сумма должна быть положительной")
        @Schema(description = "Сумма транзакции", example = "500.00", required = true)
        BigDecimal amount,
    @Schema(description = "Описание транзакции", example = "Покупка продуктов в магазине")
        String description,
    @NotNull(message = "Дата транзакции обязательна")
        @Schema(description = "Дата транзакции", example = "2025-10-12", required = true)
        LocalDate date) {}
