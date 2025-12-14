package ru.nsu.spendsphere.services;

import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import ru.nsu.spendsphere.models.dto.TransactionCreateDTO;
import ru.nsu.spendsphere.models.entities.Category;
import ru.nsu.spendsphere.models.entities.OcrTask;
import ru.nsu.spendsphere.models.entities.TransactionType;
import ru.nsu.spendsphere.models.messaging.OcrResultItem;
import ru.nsu.spendsphere.models.messaging.OcrResultMessage;
import ru.nsu.spendsphere.repositories.CategoryRepository;
import ru.nsu.spendsphere.repositories.OcrTaskRepository;

@Service
@ConditionalOnProperty(value = "app.rabbit.enabled", havingValue = "true")
@RequiredArgsConstructor
public class ParsedTransactionsListener {

  private static final Logger log = LoggerFactory.getLogger(ParsedTransactionsListener.class);

  private final TransactionService transactionService;
  private final OcrTaskRepository ocrTaskRepository;
  private final CategoryRepository categoryRepository;

  @Value("${app.rabbit.queues.parsed}")
  private String parsedResultsQueueName;

  @RabbitListener(queues = "${app.rabbit.queues.parsed}")
  public void handleParsedTransactions(OcrResultMessage message) {
    log.info(
        "Received OCR result: queue={}, taskId={}, status={}",
        parsedResultsQueueName,
        message.taskId(),
        message.status());

    if (!validateOcrResult(message)) {
      return;
    }

    OcrTask ocrTask = findOcrTask(message.taskId());
    if (ocrTask == null) {
      return;
    }

    processOcrItems(message, ocrTask);
  }

  private boolean validateOcrResult(OcrResultMessage message) {
    if (!"SUCCESS".equalsIgnoreCase(message.status())) {
      log.error(
          "OCR task {} failed with status: {}, error: {}",
          message.taskId(),
          message.status(),
          message.error());
      return false;
    }

    if (message.data() == null
        || message.data().items() == null
        || message.data().items().isEmpty()) {
      log.warn("OCR result {} has no items", message.taskId());
      return false;
    }

    return true;
  }

  private OcrTask findOcrTask(String taskIdStr) {
    UUID taskId;
    try {
      taskId = UUID.fromString(taskIdStr);
    } catch (IllegalArgumentException e) {
      log.error("Invalid task_id format: {}", taskIdStr);
      return null;
    }

    OcrTask ocrTask = ocrTaskRepository.findByTaskId(taskId).orElse(null);
    if (ocrTask == null) {
      log.error("OCR task {} not found in database", taskId);
    }
    return ocrTask;
  }

  private void processOcrItems(OcrResultMessage message, OcrTask ocrTask) {
    Long userId = ocrTask.getUserId();
    Long accountId = ocrTask.getAccountId();

    log.info(
        "Processing OCR result: taskId={}, userId={}, accountId={}, items={}",
        ocrTask.getTaskId(),
        userId,
        accountId,
        message.data().items().size());

    var userCategories = loadUserCategories(userId);

    int processed = 0;
    int skipped = 0;

    for (OcrResultItem item : message.data().items()) {
      if (createTransactionFromItem(item, userId, accountId, userCategories)) {
        processed++;
      } else {
        skipped++;
      }
    }

    log.info(
        "OCR transactions handled: taskId={}, userId={}, accountId={}, processed={}, skipped={}",
        ocrTask.getTaskId(),
        userId,
        accountId,
        processed,
        skipped);
  }

  private java.util.Map<String, Long> loadUserCategories(Long userId) {
    return categoryRepository.findAllByUserIdOrDefault(userId).stream()
        .collect(
            java.util.stream.Collectors.toMap(
                c -> c.getName().toLowerCase(),
                Category::getId,
                (existing, replacement) -> existing));
  }

  private boolean createTransactionFromItem(
      OcrResultItem item, Long userId, Long accountId, java.util.Map<String, Long> userCategories) {
    Long categoryId = null;
    if (item.category() != null && !item.category().isEmpty()) {
      categoryId = userCategories.get(item.category().toLowerCase());
    }

    TransactionType type = parseTransactionType(item);

    TransactionCreateDTO dto =
        new TransactionCreateDTO(
            type,
            categoryId,
            accountId,
            null,
            item.price().abs(),
            item.description() != null ? item.description() : item.name(),
            item.transactionDate() != null ? item.transactionDate() : java.time.LocalDate.now());

    try {
      transactionService.createTransaction(userId, dto);
      log.debug(
          "Created transaction from OCR: name={}, price={}, category={}, type={}",
          item.name(),
          item.price().abs(),
          item.category(),
          type);
      return true;
    } catch (Exception e) {
      log.error("Failed to save OCR transaction: {}", e.toString());
      return false;
    }
  }

  private TransactionType parseTransactionType(OcrResultItem item) {
    if (item.transactionType() == null || item.transactionType().isEmpty()) {
      return TransactionType.EXPENSE;
    }

    try {
      return TransactionType.valueOf(item.transactionType().toUpperCase());
    } catch (IllegalArgumentException e) {
      log.warn(
          "Invalid transaction type '{}' for item '{}', using EXPENSE as default",
          item.transactionType(),
          item.name());
      return TransactionType.EXPENSE;
    }
  }
}
