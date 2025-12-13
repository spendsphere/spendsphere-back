package ru.nsu.spendsphere.models.messaging;

/**
 * Сообщение с результатом OCR из очереди.
 *
 * @param taskId идентификатор задачи
 * @param status статус выполнения (SUCCESS/FAILURE)
 * @param data данные результата
 * @param error сообщение об ошибке (если status != SUCCESS)
 */
public record OcrResultMessage(String taskId, String status, OcrResultData data, String error) {}
