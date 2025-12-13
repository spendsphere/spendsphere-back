package ru.nsu.spendsphere.models.messaging;

import com.fasterxml.jackson.annotation.JsonProperty;
import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * Элемент результата OCR - распознанная транзакция.
 *
 * @param name название товара/услуги
 * @param price стоимость
 * @param description описание
 * @param transactionDate дата транзакции
 * @param category название категории
 * @param transactionType тип транзакции (EXPENSE/INCOME)
 */
public record OcrResultItem(
    @JsonProperty("Name") String name,
    @JsonProperty("Price") BigDecimal price,
    @JsonProperty("Description") String description,
    @JsonProperty("TransactionDate") LocalDate transactionDate,
    @JsonProperty("Category") String category,
    @JsonProperty("TransactionType") String transactionType) {}

