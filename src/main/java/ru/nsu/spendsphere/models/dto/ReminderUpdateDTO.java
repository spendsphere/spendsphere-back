package ru.nsu.spendsphere.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import ru.nsu.spendsphere.models.entities.RecurrenceType;

@Schema(description = "Данные для обновления напоминания")
public record ReminderUpdateDTO(
    @Schema(description = "Заголовок", example = "Оплата интернета") String title,
    @Schema(description = "Описание", example = "Тариф Домашний 500 Мбит/с") String description,
    @Positive @Schema(description = "Сумма платежа", example = "749.00") BigDecimal amount,
    @Schema(description = "Периодичность", example = "MONTHLY") RecurrenceType recurrenceType,
    @Schema(description = "День недели (для WEEKLY)", example = "MONDAY") DayOfWeek weeklyDayOfWeek,
    @Schema(description = "День месяца (1-30) для MONTHLY", example = "20")
        Integer monthlyDayOfMonth,
    @Schema(description = "Последний день месяца (MONTHLY)", example = "false")
        Boolean monthlyUseLastDay,
    @Schema(description = "Активно ли напоминание", example = "true") Boolean isActive,
    @Schema(description = "ID счета для оплаты", example = "2") Long accountId) {}
