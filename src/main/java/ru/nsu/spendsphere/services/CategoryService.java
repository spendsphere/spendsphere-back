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

@Service
@RequiredArgsConstructor
public class CategoryService {
  private final CategoryRepository categoryRepository;
  private final UserRepository userRepository;

  public List<CategoryDTO> getAllByUserIdOrDefault(Long userId) {
    if (!userRepository.existsById(userId)) {
      throw new ResourceNotFoundException("User with id " + userId + " not found");
    }
    return categoryRepository.findAllByUserIdOrDefault(userId).stream()
        .map(CategoryMapper::toDto)
        .collect(Collectors.toList());
  }

  public List<CategoryDTO> getCustomByUserId(Long userId) {
    if (!userRepository.existsById(userId)) {
      throw new ResourceNotFoundException("User with id " + userId + " not found");
    }
    return categoryRepository.findByUserIdAndIsDefaultFalse(userId).stream()
        .map(CategoryMapper::toDto)
        .collect(Collectors.toList());
  }

  public List<CategoryDTO> getAllDefault() {
    return categoryRepository.findByIsDefaultTrue().stream()
        .map(CategoryMapper::toDto)
        .collect(Collectors.toList());
  }

  public CategoryDTO createCustomCategory(Long userId, CategoryInputDTO body) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> new ResourceNotFoundException("User with id " + userId + " not found"));
    Category category =
        Category.builder()
            .name(body.name())
            .iconUrl(body.iconUrl())
            .isDefault(false)
            .user(user)
            .build();
    return CategoryMapper.toDto(categoryRepository.save(category));
  }

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
    category.setName(body.name());
    category.setIconUrl(body.iconUrl());
    return CategoryMapper.toDto(categoryRepository.save(category));
  }

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
