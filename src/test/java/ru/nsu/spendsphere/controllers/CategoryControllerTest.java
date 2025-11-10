package ru.nsu.spendsphere.controllers;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import ru.nsu.spendsphere.configurations.security.SecurityConfig;
import ru.nsu.spendsphere.exceptions.ResourceNotFoundException;
import ru.nsu.spendsphere.models.dto.CategoryDTO;
import ru.nsu.spendsphere.models.dto.CategoryInputDTO;
import ru.nsu.spendsphere.services.CategoryService;

/** Юнит-тесты для {@link CategoryController}. */
@WebMvcTest(CategoryController.class)
@AutoConfigureMockMvc(addFilters = false)
class CategoryControllerTest {

  @Autowired private MockMvc mockMvc;

  @MockitoBean private CategoryService categoryService;

  @Autowired private ObjectMapper objectMapper;

  /**
   * Тест успешного получения всех категорий пользователя (дефолтных и пользовательских).
   *
   * @throws Exception если возникла ошибка при выполнении запроса
   */
  @Test
  void getAllCategoriesForUserSuccess() throws Exception {
    Long userId = 1L;
    List<CategoryDTO> categories =
        Arrays.asList(
            new CategoryDTO(1L, "Food", "food-icon.png", true),
            new CategoryDTO(2L, "Transport", "transport-icon.png", true),
            new CategoryDTO(3L, "Custom Category", "custom-icon.png", false));

    when(categoryService.getAllByUserIdOrDefault(userId)).thenReturn(categories);

    mockMvc
        .perform(get("/api/v1/categories/user/{userId}/all", userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(3))
        .andExpect(jsonPath("$[0].name").value("Food"))
        .andExpect(jsonPath("$[0].isDefault").value(true))
        .andExpect(jsonPath("$[2].name").value("Custom Category"))
        .andExpect(jsonPath("$[2].isDefault").value(false));
  }

  /**
   * Тест получения всех категорий для несуществующего пользователя.
   *
   * @throws Exception если возникла ошибка при выполнении запроса
   */
  @Test
  void getAllCategoriesForUserUserNotFound() throws Exception {
    Long userId = 999L;
    when(categoryService.getAllByUserIdOrDefault(userId))
        .thenThrow(new ResourceNotFoundException("User with id " + userId + " not found"));

    mockMvc
        .perform(get("/api/v1/categories/user/{userId}/all", userId))
        .andExpect(status().isNotFound());
  }

  /**
   * Тест успешного получения пользовательских категорий.
   *
   * @throws Exception если возникла ошибка при выполнении запроса
   */
  @Test
  void getCustomCategoriesForUserSuccess() throws Exception {
    Long userId = 1L;
    List<CategoryDTO> customCategories =
        Arrays.asList(
            new CategoryDTO(10L, "My Category 1", "icon1.png", false),
            new CategoryDTO(11L, "My Category 2", "icon2.png", false));

    when(categoryService.getCustomByUserId(userId)).thenReturn(customCategories);

    mockMvc
        .perform(get("/api/v1/categories/user/{userId}/custom", userId))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(2))
        .andExpect(jsonPath("$[0].name").value("My Category 1"))
        .andExpect(jsonPath("$[0].isDefault").value(false))
        .andExpect(jsonPath("$[1].name").value("My Category 2"));
  }

  /**
   * Тест получения пользовательских категорий для несуществующего пользователя.
   *
   * @throws Exception если возникла ошибка при выполнении запроса
   */
  @Test
  void getCustomCategoriesForUserUserNotFound() throws Exception {
    Long userId = 999L;
    when(categoryService.getCustomByUserId(userId))
        .thenThrow(new ResourceNotFoundException("User with id " + userId + " not found"));

    mockMvc
        .perform(get("/api/v1/categories/user/{userId}/custom", userId))
        .andExpect(status().isNotFound());
  }

  /**
   * Тест успешного получения всех дефолтных категорий.
   *
   * @throws Exception если возникла ошибка при выполнении запроса
   */
  @Test
  void getAllDefaultCategoriesSuccess() throws Exception {
    List<CategoryDTO> defaultCategories =
        Arrays.asList(
            new CategoryDTO(1L, "Food", "food-icon.png", true),
            new CategoryDTO(2L, "Transport", "transport-icon.png", true),
            new CategoryDTO(3L, "Entertainment", "entertainment-icon.png", true));

    when(categoryService.getAllDefault()).thenReturn(defaultCategories);

    mockMvc
        .perform(get("/api/v1/categories/default"))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$").isArray())
        .andExpect(jsonPath("$.length()").value(3))
        .andExpect(jsonPath("$[0].isDefault").value(true))
        .andExpect(jsonPath("$[1].isDefault").value(true))
        .andExpect(jsonPath("$[2].isDefault").value(true));
  }

  /**
   * Тест успешного создания пользовательской категории.
   *
   * @throws Exception если возникла ошибка при выполнении запроса
   */
  @Test
  void createCustomCategorySuccess() throws Exception {
    Long userId = 1L;
    CategoryInputDTO inputDto = new CategoryInputDTO("New Category", "new-icon.png");
    CategoryDTO expectedDto = new CategoryDTO(100L, "New Category", "new-icon.png", false);

    when(categoryService.createCustomCategory(eq(userId), any(CategoryInputDTO.class)))
        .thenReturn(expectedDto);

    mockMvc
        .perform(
            post("/api/v1/categories/user/{userId}/category", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(100L))
        .andExpect(jsonPath("$.name").value("New Category"))
        .andExpect(jsonPath("$.iconUrl").value("new-icon.png"))
        .andExpect(jsonPath("$.isDefault").value(false));
  }

  /**
   * Тест создания категории для несуществующего пользователя.
   *
   * @throws Exception если возникла ошибка при выполнении запроса
   */
  @Test
  void createCustomCategoryUserNotFound() throws Exception {
    Long userId = 999L;
    CategoryInputDTO inputDto = new CategoryInputDTO("New Category", "new-icon.png");

    when(categoryService.createCustomCategory(eq(userId), any(CategoryInputDTO.class)))
        .thenThrow(new ResourceNotFoundException("User with id " + userId + " not found"));

    mockMvc
        .perform(
            post("/api/v1/categories/user/{userId}/category", userId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto)))
        .andExpect(status().isNotFound());
  }

  /**
   * Тест успешного обновления пользовательской категории.
   *
   * @throws Exception если возникла ошибка при выполнении запроса
   */
  @Test
  void updateCustomCategorySuccess() throws Exception {
    Long userId = 1L;
    Long categoryId = 10L;
    CategoryInputDTO inputDto = new CategoryInputDTO("Updated Category", "updated-icon.png");
    CategoryDTO expectedDto =
        new CategoryDTO(categoryId, "Updated Category", "updated-icon.png", false);

    when(categoryService.updateCustomCategory(
            eq(userId), eq(categoryId), any(CategoryInputDTO.class)))
        .thenReturn(expectedDto);

    mockMvc
        .perform(
            put("/api/v1/categories/user/{userId}/category/{categoryId}", userId, categoryId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto)))
        .andExpect(status().isOk())
        .andExpect(jsonPath("$.id").value(categoryId))
        .andExpect(jsonPath("$.name").value("Updated Category"))
        .andExpect(jsonPath("$.iconUrl").value("updated-icon.png"))
        .andExpect(jsonPath("$.isDefault").value(false));
  }

  /**
   * Тест обновления категории для несуществующего пользователя.
   *
   * @throws Exception если возникла ошибка при выполнении запроса
   */
  @Test
  void updateCustomCategoryUserNotFound() throws Exception {
    Long userId = 999L;
    Long categoryId = 10L;
    CategoryInputDTO inputDto = new CategoryInputDTO("Updated Category", "updated-icon.png");

    when(categoryService.updateCustomCategory(
            eq(userId), eq(categoryId), any(CategoryInputDTO.class)))
        .thenThrow(new ResourceNotFoundException("User with id " + userId + " not found"));

    mockMvc
        .perform(
            put("/api/v1/categories/user/{userId}/category/{categoryId}", userId, categoryId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto)))
        .andExpect(status().isNotFound());
  }

  /**
   * Тест обновления несуществующей категории.
   *
   * @throws Exception если возникла ошибка при выполнении запроса
   */
  @Test
  void updateCustomCategoryCategoryNotFound() throws Exception {
    Long userId = 1L;
    Long categoryId = 999L;
    CategoryInputDTO inputDto = new CategoryInputDTO("Updated Category", "updated-icon.png");

    when(categoryService.updateCustomCategory(
            eq(userId), eq(categoryId), any(CategoryInputDTO.class)))
        .thenThrow(new ResourceNotFoundException("Category with id " + categoryId + " not found"));

    mockMvc
        .perform(
            put("/api/v1/categories/user/{userId}/category/{categoryId}", userId, categoryId)
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(inputDto)))
        .andExpect(status().isNotFound());
  }

  /**
   * Тест успешного удаления пользовательской категории.
   *
   * @throws Exception если возникла ошибка при выполнении запроса
   */
  @Test
  void deleteCustomCategorySuccess() throws Exception {
    Long userId = 1L;
    Long categoryId = 10L;

    doNothing().when(categoryService).deleteCustomCategory(userId, categoryId);

    mockMvc
        .perform(
            delete("/api/v1/categories/user/{userId}/category/{categoryId}", userId, categoryId))
        .andExpect(status().isOk());
  }

  /**
   * Тест удаления категории для несуществующего пользователя.
   *
   * @throws Exception если возникла ошибка при выполнении запроса
   */
  @Test
  void deleteCustomCategoryUserNotFound() throws Exception {
    Long userId = 999L;
    Long categoryId = 10L;

    doThrow(new ResourceNotFoundException("User with id " + userId + " not found"))
        .when(categoryService)
        .deleteCustomCategory(userId, categoryId);

    mockMvc
        .perform(
            delete("/api/v1/categories/user/{userId}/category/{categoryId}", userId, categoryId))
        .andExpect(status().isNotFound());
  }

  /**
   * Тест удаления несуществующей категории.
   *
   * @throws Exception если возникла ошибка при выполнении запроса
   */
  @Test
  void deleteCustomCategoryCategoryNotFound() throws Exception {
    Long userId = 1L;
    Long categoryId = 999L;

    doThrow(new ResourceNotFoundException("Category with id " + categoryId + " not found"))
        .when(categoryService)
        .deleteCustomCategory(userId, categoryId);

    mockMvc
        .perform(
            delete("/api/v1/categories/user/{userId}/category/{categoryId}", userId, categoryId))
        .andExpect(status().isNotFound());
  }
}
