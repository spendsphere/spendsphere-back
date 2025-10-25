package ru.nsu.spendsphere.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import java.math.BigDecimal;
import java.time.DayOfWeek;
import ru.nsu.spendsphere.models.entities.RecurrenceType;

@Schema(description = "Данные для создания напоминания")
public record ReminderCreateDTO(
    @NotBlank @Schema(description = "Заголовок", example = "Оплата интернета", required = true)
        String title,
    @Schema(description = "Описание", example = "Тариф Домашний 500 Мбит/с") String description,
    @NotNull @Positive @Schema(description = "Сумма платежа", example = "699.00", required = true)
        BigDecimal amount,
    @NotNull @Schema(description = "Периодичность", example = "MONTHLY", required = true)
        RecurrenceType recurrenceType,
    @Schema(description = "День недели (для WEEKLY)", example = "MONDAY") DayOfWeek weeklyDayOfWeek,
    @Schema(description = "День месяца (1-30) для MONTHLY", example = "15")
        Integer monthlyDayOfMonth,
    @Schema(description = "Последний день месяца (MONTHLY)", example = "false")
        Boolean monthlyUseLastDay,
    @Schema(description = "Активно ли напоминание", example = "true") Boolean isActive,
    @Schema(description = "ID счета для оплаты", example = "2") Long accountId) {}
