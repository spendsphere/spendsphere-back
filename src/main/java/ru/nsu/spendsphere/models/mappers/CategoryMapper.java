package ru.nsu.spendsphere.models.mappers;

import ru.nsu.spendsphere.models.dto.CategoryDTO;
import ru.nsu.spendsphere.models.entities.Category;

public class CategoryMapper {
  public static CategoryDTO toDto(Category c) {
    if (c == null) {
      return null;
    }
    return new CategoryDTO(
        c.getId(), c.getName(), c.getIcon(), c.getColor(), c.getIsDefault(), c.getCategoryType());
  }
}
