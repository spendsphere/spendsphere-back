package ru.nsu.spendsphere.repositories;

import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import ru.nsu.spendsphere.models.entities.OcrTask;

/**
 * Репозиторий для работы с OCR-задачами.
 */
@Repository
public interface OcrTaskRepository extends JpaRepository<OcrTask, UUID> {

  /**
   * Поиск OCR-задачи по ID задачи.
   *
   * @param taskId идентификатор задачи
   * @return Optional с найденной задачей или пустой Optional
   */
  Optional<OcrTask> findByTaskId(UUID taskId);
}

