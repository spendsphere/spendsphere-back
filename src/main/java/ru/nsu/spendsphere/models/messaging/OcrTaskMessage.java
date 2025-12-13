package ru.nsu.spendsphere.models.messaging;

import java.util.List;

/**
 * Сообщение для отправки задачи OCR в очередь.
 *
 * @param taskId идентификатор задачи
 * @param imageB64 изображение в формате base64
 * @param categories список доступных категорий
 */
public record OcrTaskMessage(String taskId, String imageB64, List<String> categories) {}
