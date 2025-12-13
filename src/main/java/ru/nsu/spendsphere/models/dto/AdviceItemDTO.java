package ru.nsu.spendsphere.models.dto;

/**
 * DTO для отдельного совета.
 *
 * @param id порядковый номер совета
 * @param title заголовок
 * @param priority приоритет (High/Medium/Low)
 * @param description описание
 */
public record AdviceItemDTO(Integer id, String title, String priority, String description) {}
