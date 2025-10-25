package ru.nsu.spendsphere.models.entities;

/**
 * Типы транзакций.
 *
 * <ul>
 *   <li>INCOME - Доход (зачисление на счет)
 *   <li>EXPENSE - Расход (списание со счета)
 *   <li>TRANSFER - Перевод между счетами
 * </ul>
 */
public enum TransactionType {
  INCOME,
  EXPENSE,
  TRANSFER
}
