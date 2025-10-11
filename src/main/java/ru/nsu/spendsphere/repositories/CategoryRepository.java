package ru.nsu.spendsphere.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.nsu.spendsphere.models.entities.Category;
import ru.nsu.spendsphere.models.entities.User;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {

  // Категории конкретного пользователя (только кастомные)
  @Query("SELECT c FROM Category c WHERE c.user.id = :userId")
  List<Category> findByUserId(@Param("userId") Long userId);

  // Дефолтные категории (без пользователя)
  List<Category> findByUserIsNull();

  // Все категории для пользователя (дефолтные + его кастомные)
  @Query("SELECT c FROM Category c WHERE c.user IS NULL OR c.user.id = :userId")
  List<Category> findAllCategoriesForUser(@Param("userId") Long userId);

  // Поиск по ID и пользователю (только для кастомных категорий)
  Optional<Category> findByIdAndUserId(Long id, Long userId);

  // Проверка существования категории по имени для пользователя
  @Query(
      "SELECT CASE WHEN COUNT(c) > 0 THEN true ELSE false END FROM Category c WHERE "
          + "(c.user IS NULL AND :userId IS NULL) OR "
          + "(c.user.id = :userId AND c.name = :name)")
  boolean existsByUserIdAndName(@Param("userId") Long userId, @Param("name") String name);

  // Для дефолтных категорий
  boolean existsByUserIsNullAndName(String name);

  // Для кастомных категорий
  boolean existsByUserAndName(User user, String name);
}
