package ru.nsu.spendsphere.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.nsu.spendsphere.models.entities.Category;

/**
 * Репозиторий для работы с категориями расходов и доходов. Предоставляет методы для выполнения
 * операций с базой данных категорий.
 */
@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
  /**
   * Поиск всех категорий пользователя (пользовательские и дефолтные).
   *
   * @param userId идентификатор пользователя
   * @return список всех доступных категорий для пользователя
   */
  @Query("SELECT c FROM Category c WHERE c.user.id = :userId OR c.isDefault = true")
  List<Category> findAllByUserIdOrDefault(@Param("userId") Long userId);

  /**
   * Поиск всех пользовательских (не дефолтных) категорий пользователя.
   *
   * @param userId идентификатор пользователя
   * @return список пользовательских категорий
   */
  List<Category> findByUserIdAndIsDefaultFalse(Long userId);

  /**
   * Поиск всех дефолтных категорий.
   *
   * @return список дефолтных категорий
   */
  List<Category> findByIsDefaultTrue();

  /**
   * Поиск одной пользовательской категории по ID категории и ID пользователя.
   *
   * @param categoryId идентификатор категории
   * @param userId идентификатор пользователя
   * @return Optional с категорией, если найдена
   */
  Optional<Category> findByIdAndUserIdAndIsDefaultFalse(Long categoryId, Long userId);

  /**
   * Проверка существования пользовательской категории.
   *
   * @param categoryId идентификатор категории
   * @param userId идентификатор пользователя
   * @return true, если категория существует и принадлежит пользователю
   */
  boolean existsByIdAndUserIdAndIsDefaultFalse(Long categoryId, Long userId);
}
