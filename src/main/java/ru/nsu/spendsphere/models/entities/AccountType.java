package ru.nsu.spendsphere.models.entities;

/**
 * Типы счетов для пользователей.
 *
 * <ul>
 *   <li>CASH - Наличные
 *   <li>CARD - Банковская карта
 *   <li>SAVINGS - Сберегательный счет
 *   <li>CREDIT - Кредитная карта
 *   <li>INVESTMENT - Инвестиционный счет
 *   <li>OTHER - Другое
 * </ul>
 */
public enum AccountType {
  CASH,
  CARD,
  SAVINGS,
  CREDIT,
  INVESTMENT,
  OTHER
}
