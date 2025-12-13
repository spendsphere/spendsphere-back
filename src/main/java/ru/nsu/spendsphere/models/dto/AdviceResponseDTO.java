package ru.nsu.spendsphere.models.dto;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * DTO для ответа с финансовым советом.
 *
 * @param id идентификатор совета
 * @param userId идентификатор пользователя
 * @param goal цель
 * @param targetDate желаемая дата достижения цели
 * @param items список советов
 * @param createdAt дата создания
 */
public record AdviceResponseDTO(
    Long id,
    Long userId,
    String goal,
    LocalDate targetDate,
    List<AdviceItemDTO> items,
    LocalDateTime createdAt) {}
