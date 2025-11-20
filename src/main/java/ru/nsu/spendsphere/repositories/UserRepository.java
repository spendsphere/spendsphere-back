package ru.nsu.spendsphere.repositories;

import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.nsu.spendsphere.models.entities.User;

/** Репозиторий для работы с сущностью пользователя. */
@Repository
public interface UserRepository extends JpaRepository<User, Long> {
  /**
   * Поиск пользователя по email.
   *
   * @param email email пользователя
   * @return Optional с пользователем, если найден
   */
  Optional<User> findByEmail(String email);

  /**
   * Проверка существования пользователя с указанным email.
   *
   * @param email email для проверки
   * @return true, если пользователь с таким email существует
   */
  boolean existsByEmail(String email);

  Optional<User> findByProviderAndProviderId(String provider, String providerId);
}
