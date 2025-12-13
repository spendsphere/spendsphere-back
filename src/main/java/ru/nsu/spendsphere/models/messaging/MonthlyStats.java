package ru.nsu.spendsphere.models.messaging;

import java.math.BigDecimal;
import java.util.Map;

/**
 * Статистика за месяц.
 *
 * @param expensesByCategory расходы по категориям
 * @param incomeBySource доходы по источникам
 * @param averageByCategory средние значения по категориям
 */
public record MonthlyStats(
    Map<String, BigDecimal> expensesByCategory,
    Map<String, BigDecimal> incomeBySource,
    Map<String, BigDecimal> averageByCategory) {}

