package ru.nsu.spendsphere.models.messaging;

import java.util.List;

/**
 * Данные результата OCR.
 *
 * @param items список распознанных элементов транзакций
 */
public record OcrResultData(List<OcrResultItem> items) {}

