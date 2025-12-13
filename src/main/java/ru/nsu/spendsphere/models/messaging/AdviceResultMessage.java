package ru.nsu.spendsphere.models.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.util.List;

/**
 * Сообщение с результатом генерации финансового совета.
 *
 * @param taskId идентификатор задачи
 * @param status статус выполнения (SUCCESS/FAILURE)
 * @param goal цель
 * @param advice список советов
 */
public record AdviceResultMessage(
    @JsonProperty("task_id") String taskId,
    String status,
    String goal,
    List<AdviceResultItem> advice) {}
