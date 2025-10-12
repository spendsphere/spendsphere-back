package ru.nsu.spendsphere.repositories;

import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.nsu.spendsphere.models.entities.Account;

/** Репозиторий для работы со счетами пользователей. */
@Repository
public interface AccountRepository extends JpaRepository<Account, Long> {

  /**
   * Находит все счета пользователя по его идентификатору.
   *
   * @param userId идентификатор пользователя
   * @return список счетов пользователя
   */
  List<Account> findByUserId(Long userId);

  /**
   * Находит счет по идентификатору и идентификатору пользователя.
   *
   * @param id идентификатор счета
   * @param userId идентификатор пользователя
   * @return Optional с счетом, если найден
   */
  Optional<Account> findByIdAndUserId(Long id, Long userId);

  /**
   * Проверяет существование счета по идентификатору и идентификатору пользователя.
   *
   * @param id идентификатор счета
   * @param userId идентификатор пользователя
   * @return true, если счет существует
   */
  boolean existsByIdAndUserId(Long id, Long userId);
}
