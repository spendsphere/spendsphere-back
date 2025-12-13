package ru.nsu.spendsphere.models.messaging;

import java.util.Map;

/**
 * Сообщение для отправки задачи на получение финансовых советов.
 *
 * @param taskId идентификатор задачи
 * @param goal цель пользователя
 * @param monthlyStats статистика по месяцам
 */
public record AdviceTaskMessage(
    String taskId, AdviceGoal goal, Map<String, MonthlyStats> monthlyStats) {}

