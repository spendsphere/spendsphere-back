package ru.nsu.spendsphere.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import ru.nsu.spendsphere.models.entities.TransactionType;

@Schema(description = "Информация о транзакции")
public record TransactionDTO(
    @Schema(description = "Идентификатор транзакции", example = "1") Long id,
    @Schema(description = "Идентификатор пользователя", example = "1") Long userId,
    @Schema(description = "Тип транзакции", example = "EXPENSE") TransactionType type,
    @Schema(description = "Идентификатор категории", example = "5") Long categoryId,
    @Schema(description = "Название категории", example = "Продукты") String categoryName,
    @Schema(description = "Иконка категории (эмодзи)", example = "🍔") String categoryIcon,
    @Schema(description = "Цвет категории (hex)", example = "#10b981") String categoryColor,
    @Schema(description = "Идентификатор счета", example = "2") Long accountId,
    @Schema(description = "Название счета", example = "Основная карта") String accountName,
    @Schema(description = "Идентификатор счета для перевода (только для TRANSFER)", example = "3")
        Long transferAccountId,
    @Schema(description = "Название счета для перевода (только для TRANSFER)", example = "Наличные")
        String transferAccountName,
    @Schema(description = "Сумма транзакции", example = "500.00") BigDecimal amount,
    @Schema(description = "Описание транзакции", example = "Покупка продуктов в магазине")
        String description,
    @Schema(description = "Дата транзакции", example = "2025-10-12") LocalDate date,
    @Schema(description = "Дата и время создания") LocalDateTime createdAt,
    @Schema(description = "Дата и время последнего обновления") LocalDateTime updatedAt) {}
