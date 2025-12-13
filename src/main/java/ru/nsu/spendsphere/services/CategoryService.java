package ru.nsu.spendsphere.services;

import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import ru.nsu.spendsphere.exceptions.ResourceNotFoundException;
import ru.nsu.spendsphere.models.dto.CategoryDTO;
import ru.nsu.spendsphere.models.dto.CategoryInputDTO;
import ru.nsu.spendsphere.models.entities.Category;
import ru.nsu.spendsphere.models.entities.User;
import ru.nsu.spendsphere.models.mappers.CategoryMapper;
import ru.nsu.spendsphere.repositories.CategoryRepository;
import ru.nsu.spendsphere.repositories.UserRepository;

/**
 * Сервис для управления категориями расходов и доходов. Предоставляет бизнес-логику для работы с
 * дефолтными и пользовательскими категориями.
 */
@Service
@RequiredArgsConstructor
public class CategoryService {
  private final CategoryRepository categoryRepository;
  private final UserRepository userRepository;

  /**
   * Получение всех категорий для пользователя (дефолтные и пользовательские).
   *
   * @param userId идентификатор пользователя
   * @return список всех доступных категорий для пользователя
   * @throws ResourceNotFoundException если пользователь с указанным ID не найден
   */
  public List<CategoryDTO> getAllByUserIdOrDefault(Long userId) {
    if (!userRepository.existsById(userId)) {
      throw new ResourceNotFoundException("User with id " + userId + " not found");
    }
    return categoryRepository.findAllByUserIdOrDefault(userId).stream()
        .map(CategoryMapper::toDto)
        .collect(Collectors.toList());
  }

  /**
   * Получение всех пользовательских категорий.
   *
   * @param userId идентификатор пользователя
   * @return список пользовательских категорий
   * @throws ResourceNotFoundException если пользователь с указанным ID не найден
   */
  public List<CategoryDTO> getCustomByUserId(Long userId) {
    if (!userRepository.existsById(userId)) {
      throw new ResourceNotFoundException("User with id " + userId + " not found");
    }
    return categoryRepository.findByUserIdAndIsDefaultFalse(userId).stream()
        .map(CategoryMapper::toDto)
        .collect(Collectors.toList());
  }

  /**
   * Получение всех дефолтных категорий.
   *
   * @return список всех дефолтных категорий
   */
  public List<CategoryDTO> getAllDefault() {
    return categoryRepository.findByIsDefaultTrue().stream()
        .map(CategoryMapper::toDto)
        .collect(Collectors.toList());
  }

  /**
   * Создание новой пользовательской категории.
   *
   * @param userId идентификатор пользователя
   * @param body DTO с данными новой категории
   * @return DTO созданной категории
   * @throws ResourceNotFoundException если пользователь с указанным ID не найден
   */
  public CategoryDTO createCustomCategory(Long userId, CategoryInputDTO body) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> new ResourceNotFoundException("User with id " + userId + " not found"));
    Category category =
        Category.builder()
            .name(body.name())
            .icon(body.icon())
            .color(body.color())
            .isDefault(false)
            .categoryType(
                body.categoryType() != null
                    ? body.categoryType()
                    : ru.nsu.spendsphere.models.entities.CategoryType.BOTH)
            .user(user)
            .build();
    return CategoryMapper.toDto(categoryRepository.save(category));
  }

  /**
   * Обновление пользовательской категории.
   *
   * @param userId идентификатор пользователя
   * @param categoryId идентификатор категории
   * @param body DTO с обновленными данными категории
   * @return DTO обновленной категории
   * @throws ResourceNotFoundException если пользователь или категория не найдены
   */
  public CategoryDTO updateCustomCategory(Long userId, Long categoryId, CategoryInputDTO body) {
    if (!userRepository.existsById(userId)) {
      throw new ResourceNotFoundException("User with id " + userId + " not found");
    }
    Category category =
        categoryRepository
            .findByIdAndUserIdAndIsDefaultFalse(categoryId, userId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException("Category with id " + categoryId + " not found"));
    if (body.name() != null) {
      category.setName(body.name());
    }
    if (body.icon() != null) {
      category.setIcon(body.icon());
    }
    if (body.color() != null) {
      category.setColor(body.color());
    }
    if (body.categoryType() != null) {
      category.setCategoryType(body.categoryType());
    }
    return CategoryMapper.toDto(categoryRepository.save(category));
  }

  /**
   * Удаление пользовательской категории.
   *
   * @param userId идентификатор пользователя
   * @param categoryId идентификатор категории
   * @throws ResourceNotFoundException если пользователь или категория не найдены
   */
  public void deleteCustomCategory(Long userId, Long categoryId) {
    if (!userRepository.existsById(userId)) {
      throw new ResourceNotFoundException("User with id " + userId + " not found");
    }
    if (!categoryRepository.existsByIdAndUserIdAndIsDefaultFalse(categoryId, userId)) {
      throw new ResourceNotFoundException("Category with id " + categoryId + " not found");
    }
    categoryRepository.deleteById(categoryId);
  }
}
