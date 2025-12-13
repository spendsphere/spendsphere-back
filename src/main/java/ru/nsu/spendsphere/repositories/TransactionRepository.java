package ru.nsu.spendsphere.repositories;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.nsu.spendsphere.models.entities.Transaction;
import ru.nsu.spendsphere.models.entities.TransactionType;

/** Репозиторий для работы с транзакциями пользователей. */
@Repository
public interface TransactionRepository extends JpaRepository<Transaction, Long> {

  /**
   * Находит все транзакции пользователя.
   *
   * @param userId идентификатор пользователя
   * @return список транзакций пользователя
   */
  List<Transaction> findByUserIdOrderByDateDescCreatedAtDesc(Long userId);

  /**
   * Находит транзакцию по идентификатору и идентификатору пользователя.
   *
   * @param id идентификатор транзакции
   * @param userId идентификатор пользователя
   * @return Optional с транзакцией, если найдена
   */
  Optional<Transaction> findByIdAndUserId(Long id, Long userId);

  /**
   * Находит транзакции пользователя с фильтрами.
   *
   * @param userId идентификатор пользователя
   * @param type тип транзакции (опционально)
   * @param accountId идентификатор счета (опционально)
   * @param categoryId идентификатор категории (опционально)
   * @param dateFrom дата начала периода (опционально)
   * @param dateTo дата окончания периода (опционально)
   * @return список транзакций
   */
  @Query(
      "SELECT t FROM Transaction t WHERE t.user.id = :userId "
          + "AND (:type IS NULL OR t.type = :type) "
          + "AND (:accountId IS NULL OR t.account.id = :accountId OR t.transferAccount.id ="
          + " :accountId) "
          + "AND (:categoryId IS NULL OR t.category.id = :categoryId) "
          + "AND (CAST(:dateFrom AS date) IS NULL OR t.date >= :dateFrom) "
          + "AND (CAST(:dateTo AS date) IS NULL OR t.date <= :dateTo) "
          + "ORDER BY t.date DESC, t.createdAt DESC")
  List<Transaction> findByUserIdWithFilters(
      @Param("userId") Long userId,
      @Param("type") TransactionType type,
      @Param("accountId") Long accountId,
      @Param("categoryId") Long categoryId,
      @Param("dateFrom") LocalDate dateFrom,
      @Param("dateTo") LocalDate dateTo);
}
