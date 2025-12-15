package ru.nsu.spendsphere.controllers;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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

@Tag(
    name = "Управление категориями",
    description = "API для работы с дефолтными и пользовательскими категориями расходов и доходов")
@RestController
@RequestMapping({"/api/v1/categories", "/v1/categories"})
@RequiredArgsConstructor
public class CategoryController {
  private final CategoryService categoryService;

  @Operation(
      summary = "Получение всех категорий пользователя",
      description =
          "Возвращает все доступные категории для пользователя (дефолтные и пользовательские)")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Список категорий успешно получен",
            content =
                @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = CategoryDTO.class)))),
        @ApiResponse(
            responseCode = "404",
            description = "Пользователь не найден",
            content = @Content)
      })
  @GetMapping("/user/{userId}/all")
  public List<CategoryDTO> getAllCategoriesForUser(
      @Parameter(description = "Идентификатор пользователя", required = true) @PathVariable
          Long userId) {
    return categoryService.getAllByUserIdOrDefault(userId);
  }

  @Operation(
      summary = "Получение пользовательских категорий",
      description = "Возвращает только пользовательские (не дефолтные) категории")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Список пользовательских категорий успешно получен",
            content =
                @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = CategoryDTO.class)))),
        @ApiResponse(
            responseCode = "404",
            description = "Пользователь не найден",
            content = @Content)
      })
  @GetMapping("/user/{userId}/custom")
  public List<CategoryDTO> getCustomCategoriesForUser(
      @Parameter(description = "Идентификатор пользователя", required = true) @PathVariable
          Long userId) {
    return categoryService.getCustomByUserId(userId);
  }

  @Operation(
      summary = "Получение дефолтных категорий",
      description = "Возвращает список всех дефолтных категорий")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Список дефолтных категорий успешно получен",
            content =
                @Content(
                    mediaType = "application/json",
                    array = @ArraySchema(schema = @Schema(implementation = CategoryDTO.class))))
      })
  @GetMapping("/default")
  public List<CategoryDTO> getAllDefaultCategories() {
    return categoryService.getAllDefault();
  }

  @Operation(
      summary = "Создание пользовательской категории",
      description = "Создает новую пользовательскую категорию для указанного пользователя")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Категория успешно создана",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CategoryDTO.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Пользователь не найден",
            content = @Content)
      })
  @PostMapping("/user/{userId}/category")
  public CategoryDTO createCustomCategory(
      @Parameter(description = "Идентификатор пользователя", required = true) @PathVariable
          Long userId,
      @Parameter(description = "Данные новой категории", required = true) @RequestBody
          CategoryInputDTO body) {
    return categoryService.createCustomCategory(userId, body);
  }

  @Operation(
      summary = "Обновление пользовательской категории",
      description = "Обновляет данные существующей пользовательской категории")
  @ApiResponses(
      value = {
        @ApiResponse(
            responseCode = "200",
            description = "Категория успешно обновлена",
            content =
                @Content(
                    mediaType = "application/json",
                    schema = @Schema(implementation = CategoryDTO.class))),
        @ApiResponse(
            responseCode = "404",
            description = "Пользователь или категория не найдены",
            content = @Content)
      })
  @PutMapping("/user/{userId}/category/{categoryId}")
  public CategoryDTO updateCustomCategory(
      @Parameter(description = "Идентификатор пользователя", required = true) @PathVariable
          Long userId,
      @Parameter(description = "Идентификатор категории", required = true) @PathVariable
          Long categoryId,
      @Parameter(description = "Обновленные данные категории", required = true) @RequestBody
          CategoryInputDTO body) {
    return categoryService.updateCustomCategory(userId, categoryId, body);
  }

  @Operation(
      summary = "Удаление пользовательской категории",
      description = "Удаляет пользовательскую категорию")
  @ApiResponses(
      value = {
        @ApiResponse(responseCode = "200", description = "Категория успешно удалена"),
        @ApiResponse(
            responseCode = "404",
            description = "Пользователь или категория не найдены",
            content = @Content)
      })
  @DeleteMapping("/user/{userId}/category/{categoryId}")
  public void deleteCustomCategory(
      @Parameter(description = "Идентификатор пользователя", required = true) @PathVariable
          Long userId,
      @Parameter(description = "Идентификатор категории", required = true) @PathVariable
          Long categoryId) {
    categoryService.deleteCustomCategory(userId, categoryId);
  }
}
