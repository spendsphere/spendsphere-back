package ru.nsu.spendsphere.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDateTime;
import ru.nsu.spendsphere.models.entities.RecurrenceType;

@Schema(description = "Данные напоминания о платеже")
public record ReminderDTO(
    @Schema(description = "Идентификатор напоминания", example = "1001") Long id,
    @Schema(description = "Идентификатор пользователя", example = "1") Long userId,
    @Schema(description = "Заголовок напоминания", example = "Оплата интернета") String title,
    @Schema(description = "Описание", example = "Тариф Домашний 500 Мбит/с") String description,
    @Schema(description = "Сумма платежа", example = "699.00") BigDecimal amount,
    @Schema(description = "Тип периодичности", example = "MONTHLY") RecurrenceType recurrenceType,
    @Schema(description = "День недели для WEEKLY", example = "MONDAY") DayOfWeek weeklyDayOfWeek,
    @Schema(description = "День месяца для MONTHLY (1-30)", example = "15")
        Integer monthlyDayOfMonth,
    @Schema(description = "Последний день месяца для MONTHLY", example = "false")
        Boolean monthlyUseLastDay,
    @Schema(description = "Активно ли напоминание", example = "true") Boolean isActive,
    @Schema(description = "ID счета для оплаты", example = "2") Long accountId,
    @Schema(description = "Имя счета", example = "Основная карта") String accountName,
    @Schema(description = "Создано", example = "2025-10-12T10:15:30") LocalDateTime createdAt,
    @Schema(description = "Обновлено", example = "2025-10-12T10:15:30") LocalDateTime updatedAt) {}
