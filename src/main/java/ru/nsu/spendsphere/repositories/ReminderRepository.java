package ru.nsu.spendsphere.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.nsu.spendsphere.models.entities.Reminder;

@Repository
public interface ReminderRepository extends JpaRepository<Reminder, Long> {

  /**
   * Находит все напоминания пользователя с загрузкой связанных сущностей.
   *
   * @param userId идентификатор пользователя
   * @return список напоминаний пользователя
   */
  @Query(
      "SELECT r FROM Reminder r "
          + "JOIN FETCH r.user "
          + "LEFT JOIN FETCH r.account "
          + "WHERE r.user.id = :userId")
  List<Reminder> findByUserId(@Param("userId") Long userId);

  /**
   * Находит активные напоминания пользователя с загрузкой связанных сущностей.
   *
   * @param userId идентификатор пользователя
   * @return список активных напоминаний пользователя
   */
  @Query(
      "SELECT r FROM Reminder r "
          + "JOIN FETCH r.user "
          + "LEFT JOIN FETCH r.account "
          + "WHERE r.user.id = :userId AND r.isActive = true")
  List<Reminder> findActiveByUserId(@Param("userId") Long userId);

  /**
   * Находит напоминание по ID с загрузкой связанных сущностей.
   *
   * @param id идентификатор напоминания
   * @return Optional с напоминанием
   */
  @Query(
      "SELECT r FROM Reminder r "
          + "JOIN FETCH r.user "
          + "LEFT JOIN FETCH r.account "
          + "WHERE r.id = :id")
  Optional<Reminder> findByIdWithRelations(@Param("id") Long id);
}
