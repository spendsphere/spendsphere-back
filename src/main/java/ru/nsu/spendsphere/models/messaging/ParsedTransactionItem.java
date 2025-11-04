package ru.nsu.spendsphere.models.messaging;

import java.math.BigDecimal;
import java.time.LocalDate;

public record ParsedTransactionItem(
    String type, BigDecimal amount, LocalDate date, String description, Long categoryId) {}
