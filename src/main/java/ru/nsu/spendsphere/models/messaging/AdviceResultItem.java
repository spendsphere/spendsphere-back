package ru.nsu.spendsphere.models.messaging;

/**
 * Отдельный совет из результата.
 *
 * @param id порядковый номер
 * @param title заголовок
 * @param priority приоритет (High/Medium/Low)
 * @param description описание
 */
public record AdviceResultItem(Integer id, String title, String priority, String description) {}
