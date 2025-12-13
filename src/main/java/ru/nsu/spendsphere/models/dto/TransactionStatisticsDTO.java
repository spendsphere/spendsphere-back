package ru.nsu.spendsphere.models.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Map;

/**
 * DTO для статистики транзакций за период.
 *
 * @param expensesByCategory расходы по категориям (для круговой диаграммы)
 * @param incomeByCategory доходы по категориям (для круговой диаграммы)
 * @param monthlyExpenses суммы расходов по месяцам (для столбчатой диаграммы)
 * @param monthlyIncome суммы доходов по месяцам (для столбчатой диаграммы)
 * @param avgExpensesByCategory средние расходы по категориям во времени (для линейной диаграммы)
 * @param avgIncomeByCategory средние доходы по категориям во времени (для линейной диаграммы)
 * @param maxExpensePerDay максимальный расход за день
 * @param maxExpensePerCategory максимальный расход за категорию
 * @param averageExpense среднее значение расхода за период
 * @param averageIncome среднее значение дохода за период
 * @param startDate дата начала периода
 * @param endDate дата окончания периода
 */
@Schema(description = "Статистика транзакций за период")
public record TransactionStatisticsDTO(
    @Schema(description = "Расходы по категориям (название категории -> сумма)")
        Map<String, BigDecimal> expensesByCategory,
    @Schema(description = "Доходы по категориям (название категории -> сумма)")
        Map<String, BigDecimal> incomeByCategory,
    @Schema(description = "Суммы расходов по месяцам (год-месяц -> сумма)")
        Map<String, BigDecimal> monthlyExpenses,
    @Schema(description = "Суммы доходов по месяцам (год-месяц -> сумма)")
        Map<String, BigDecimal> monthlyIncome,
    @Schema(description = "Средние расходы по категориям во времени") List<CategoryTimeSeriesDTO>
        avgExpensesByCategory,
    @Schema(description = "Средние доходы по категориям во времени") List<CategoryTimeSeriesDTO>
        avgIncomeByCategory,
    @Schema(description = "Максимальный расход за день") MaxExpensePerDayDTO maxExpensePerDay,
    @Schema(description = "Максимальный расход по категории") MaxExpensePerCategoryDTO
        maxExpensePerCategory,
    @Schema(description = "Среднее значение расхода за период") BigDecimal averageExpense,
    @Schema(description = "Среднее значение дохода за период") BigDecimal averageIncome,
    @Schema(description = "Дата начала периода") LocalDate startDate,
    @Schema(description = "Дата окончания периода") LocalDate endDate) {

  /**
   * DTO для временного ряда по категориям.
   *
   * @param categoryName название категории
   * @param timeSeries временной ряд (год-месяц -> среднее значение)
   */
  @Schema(description = "Временной ряд средних значений по категории")
  public record CategoryTimeSeriesDTO(
      @Schema(description = "Название категории") String categoryName,
      @Schema(description = "Временной ряд (год-месяц -> среднее значение)") Map<String, BigDecimal>
          timeSeries) {}

  /**
   * DTO для максимального расхода за день.
   *
   * @param date дата максимального расхода
   * @param amount сумма максимального расхода
   */
  @Schema(description = "Информация о максимальном расходе за день")
  public record MaxExpensePerDayDTO(
      @Schema(description = "Дата максимального расхода") LocalDate date,
      @Schema(description = "Сумма максимального расхода") BigDecimal amount) {}

  /**
   * DTO для максимального расхода по категории.
   *
   * @param categoryName название категории
   * @param amount сумма максимального расхода
   */
  @Schema(description = "Информация о максимальном расходе по категории")
  public record MaxExpensePerCategoryDTO(
      @Schema(description = "Название категории") String categoryName,
      @Schema(description = "Сумма максимального расхода") BigDecimal amount) {}
}

