package ru.nsu.spendsphere.models.entities;

/** Тип периодичности напоминаний. */
public enum RecurrenceType {
  /** Каждый день. */
  DAILY,
  /** Каждую неделю (с указанием дня недели). */
  WEEKLY,
  /** Каждый месяц (с указанием дня месяца или флагом последнего дня). */
  MONTHLY
}
