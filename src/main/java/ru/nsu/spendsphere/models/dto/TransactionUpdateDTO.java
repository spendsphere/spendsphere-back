package ru.nsu.spendsphere.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.LocalDate;
import ru.nsu.spendsphere.models.entities.TransactionType;

@Schema(description = "Данные для обновления транзакции")
public record TransactionUpdateDTO(
    @Schema(description = "Тип транзакции", example = "EXPENSE") TransactionType type,
    @Schema(description = "Идентификатор категории", example = "5") Long categoryId,
    @Schema(description = "Идентификатор счета", example = "2") Long accountId,
    @Schema(description = "Идентификатор счета для перевода (только для TRANSFER)", example = "3")
        Long transferAccountId,
    @Positive(message = "Сумма должна быть положительной")
        @Schema(description = "Сумма транзакции", example = "500.00")
        BigDecimal amount,
    @Schema(description = "Описание транзакции", example = "Покупка продуктов в магазине")
        String description,
    @Schema(description = "Дата транзакции", example = "2025-10-12") LocalDate date) {}
