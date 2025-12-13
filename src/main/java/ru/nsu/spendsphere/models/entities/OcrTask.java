package ru.nsu.spendsphere.models.entities;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

/**
 * Сущность для хранения задач OCR-распознавания транзакций.
 */
@Entity
@Table(name = "ocr_tasks")
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class OcrTask {

  @Id
  @Column(name = "task_id", nullable = false)
  private UUID taskId;

  @Column(name = "user_id", nullable = false)
  private Long userId;

  @Column(name = "account_id", nullable = false)
  private Long accountId;

  @CreationTimestamp
  @Column(name = "created_at", nullable = false, updatable = false)
  private LocalDateTime createdAt;
}

