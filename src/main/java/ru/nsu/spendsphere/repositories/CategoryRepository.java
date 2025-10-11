package ru.nsu.spendsphere.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.nsu.spendsphere.models.entities.Category;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
  // Вернуть все категории юзера (кастомные + дефолтные)
  @Query("SELECT c FROM Category c WHERE c.user.id = :userId OR c.isDefault = true")
  List<Category> findAllByUserIdOrDefault(@Param("userId") Long userId);

  // Только кастомные категории
  List<Category> findByUser_IdAndIsDefaultFalse(Long userId);

  // Только дефолтные
  List<Category> findByIsDefaultTrue();

  // Одна кастомная категория юзера
  Optional<Category> findByIdAndUser_IdAndIsDefaultFalse(Long categoryId, Long userId);

  // Проверка принадлежности
  boolean existsByIdAndUser_IdAndIsDefaultFalse(Long categoryId, Long userId);
}
