package ru.nsu.spendsphere.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.nsu.spendsphere.models.entities.Advice;

/** Репозиторий для работы с финансовыми советами. */
@Repository
public interface AdviceRepository extends JpaRepository<Advice, Long> {

  /**
   * Находит все советы пользователя с загрузкой связанных сущностей.
   *
   * @param userId идентификатор пользователя
   * @return список советов пользователя
   */
  @Query(
      "SELECT DISTINCT a FROM Advice a "
          + "JOIN FETCH a.user "
          + "LEFT JOIN FETCH a.items "
          + "WHERE a.user.id = :userId "
          + "ORDER BY a.createdAt DESC")
  List<Advice> findByUserIdWithItems(@Param("userId") Long userId);

  /**
   * Находит совет по task_id с загрузкой связанных сущностей.
   *
   * @param taskId идентификатор задачи
   * @return Optional с советом
   */
  @Query(
      "SELECT a FROM Advice a "
          + "JOIN FETCH a.user "
          + "LEFT JOIN FETCH a.items "
          + "WHERE a.taskId = :taskId")
  Optional<Advice> findByTaskIdWithItems(@Param("taskId") String taskId);
}
