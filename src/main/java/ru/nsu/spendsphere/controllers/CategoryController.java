package ru.nsu.spendsphere.controllers;

import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import ru.nsu.spendsphere.models.dto.CategoryDTO;
import ru.nsu.spendsphere.models.dto.CategoryInputDTO;
import ru.nsu.spendsphere.services.CategoryService;

@RestController
@RequestMapping("/api/v1/categories")
@RequiredArgsConstructor
public class CategoryController {
  private final CategoryService categoryService;

  // Все категории юзера (и дефолтные и его)
  @GetMapping("/user/{userId}/all")
  public List<CategoryDTO> getAllCategoriesForUser(@PathVariable Long userId) {
    return categoryService.getAllByUserIdOrDefault(userId);
  }

  // Кастомные категории юзера
  @GetMapping("/user/{userId}/custom")
  public List<CategoryDTO> getCustomCategoriesForUser(@PathVariable Long userId) {
    return categoryService.getCustomByUserId(userId);
  }

  // Все дефолтные категории
  @GetMapping("/default")
  public List<CategoryDTO> getAllDefaultCategories() {
    return categoryService.getAllDefault();
  }

  // Создать кастомную категорию для юзера
  @PostMapping("/user/{userId}/category")
  public CategoryDTO createCustomCategory(
      @PathVariable Long userId, @RequestBody CategoryInputDTO body) {
    return categoryService.createCustomCategory(userId, body);
  }

  // Изменить кастомную категорию
  @PutMapping("/user/{userId}/category/{categoryId}")
  public CategoryDTO updateCustomCategory(
      @PathVariable Long userId,
      @PathVariable Long categoryId,
      @RequestBody CategoryInputDTO body) {
    return categoryService.updateCustomCategory(userId, categoryId, body);
  }

  // Удалить кастомную категорию
  @DeleteMapping("/user/{userId}/category/{categoryId}")
  public void deleteCustomCategory(@PathVariable Long userId, @PathVariable Long categoryId) {
    categoryService.deleteCustomCategory(userId, categoryId);
  }
}
