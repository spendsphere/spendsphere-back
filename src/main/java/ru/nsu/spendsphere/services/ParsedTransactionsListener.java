package ru.nsu.spendsphere.services;

import lombok.RequiredArgsConstructor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.amqp.rabbit.annotation.RabbitListener;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import ru.nsu.spendsphere.models.dto.TransactionCreateDTO;
import ru.nsu.spendsphere.models.entities.Account;
import ru.nsu.spendsphere.models.entities.TransactionType;
import ru.nsu.spendsphere.models.messaging.ParsedTransactionItem;
import ru.nsu.spendsphere.models.messaging.ParsedTransactionsMessage;
import ru.nsu.spendsphere.repositories.AccountRepository;

@Service
@ConditionalOnProperty(value = "app.rabbit.enabled", havingValue = "true")
@RequiredArgsConstructor
public class ParsedTransactionsListener {

  private static final Logger log = LoggerFactory.getLogger(ParsedTransactionsListener.class);

  private final TransactionService transactionService;
  private final AccountRepository accountRepository;

  @Value("${app.rabbit.queues.parsed}")
  private String parsedResultsQueueName;

  @RabbitListener(queues = "${app.rabbit.queues.parsed}")
  public void handleParsedTransactions(ParsedTransactionsMessage message) {
    if (message.transactions() == null || message.transactions().isEmpty()) {
      return;
    }

    Account account = accountRepository.findById(message.accountId()).orElse(null);
    if (account == null) {
      log.error("Parsed transactions message skipped: account {} not found", message.accountId());
      return;
    }

    Long userId = account.getUser().getId();

    log.info(
        "Received parsed transactions: queue={}, accountId={}, items={}",
        parsedResultsQueueName,
        message.accountId(),
        message.transactions().size());

    int processed = 0;
    int skipped = 0;
    for (ParsedTransactionItem item : message.transactions()) {
      TransactionType type;
      try {
        type = TransactionType.valueOf(item.type().toUpperCase());
      } catch (Exception e) {
        skipped++;
        log.warn("Skip transaction item due to invalid type: {}", item.type());
        continue;
      }

      TransactionCreateDTO dto =
          new TransactionCreateDTO(
              type,
              item.categoryId(),
              message.accountId(),
              null,
              item.amount(),
              item.description(),
              item.date() != null ? item.date() : java.time.LocalDate.now());

      try {
        transactionService.createTransaction(userId, dto);
        processed++;
      } catch (Exception e) {
        skipped++;
        log.error(
            "Failed to persist parsed transaction for account {}: {}",
            message.accountId(),
            e.toString());
      }
    }

    log.info(
        "Parsed transactions handled: accountId={}, processed={}, skipped={}",
        message.accountId(),
        processed,
        skipped);
  }
}
