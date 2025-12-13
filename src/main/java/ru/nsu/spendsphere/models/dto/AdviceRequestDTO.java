package ru.nsu.spendsphere.models.dto;

import jakarta.validation.constraints.NotBlank;
import java.time.LocalDate;

/**
 * DTO для запроса создания финансового совета.
 *
 * @param goal цель пользователя
 * @param targetDate желаемая дата достижения цели (опционально)
 */
public record AdviceRequestDTO(
    @NotBlank(message = "Goal is required") String goal, LocalDate targetDate) {}

