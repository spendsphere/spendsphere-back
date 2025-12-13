package ru.nsu.spendsphere.models.messaging;

import java.time.LocalDate;

/**
 * Цель для финансового совета.
 *
 * @param name название цели
 * @param targetDate желаемая дата достижения
 */
public record AdviceGoal(String name, LocalDate targetDate) {}
