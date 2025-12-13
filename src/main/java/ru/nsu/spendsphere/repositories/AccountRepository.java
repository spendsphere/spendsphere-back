package ru.nsu.spendsphere.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import ru.nsu.spendsphere.models.entities.Account;

/** Репозиторий для работы со счетами пользователей. */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

  /**
   * Находит все счета пользователя по его идентификатору с загрузкой связанных сущностей.
   *
   * @param userId идентификатор пользователя
   * @return список счетов пользователя
   */
  @Query("SELECT a FROM Account a JOIN FETCH a.user WHERE a.user.id = :userId")
  List<Account> findByUserId(@Param("userId") Long userId);

  /**
   * Находит счет по идентификатору и идентификатору пользователя с загрузкой связанных сущностей.
   *
   * @param id идентификатор счета
   * @param userId идентификатор пользователя
   * @return Optional с счетом, если найден
   */
  @Query("SELECT a FROM Account a JOIN FETCH a.user WHERE a.id = :id AND a.user.id = :userId")
  Optional<Account> findByIdAndUserId(@Param("id") Long id, @Param("userId") Long userId);

  /**
   * Проверяет существование счета по идентификатору и идентификатору пользователя.
   *
   * @param id идентификатор счета
   * @param userId идентификатор пользователя
   * @return true, если счет существует
   */
  boolean existsByIdAndUserId(Long id, Long userId);
}
