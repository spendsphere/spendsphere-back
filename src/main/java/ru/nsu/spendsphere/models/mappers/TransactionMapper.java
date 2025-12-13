package ru.nsu.spendsphere.models.mappers;

import org.springframework.stereotype.Component;
import ru.nsu.spendsphere.models.dto.TransactionDTO;
import ru.nsu.spendsphere.models.entities.Transaction;

/** Маппер для преобразования между Entity Transaction и DTO. */
@Component
public class TransactionMapper {

  /**
   * Преобразует Entity Transaction в DTO.
   *
   * @param transaction entity транзакции
   * @return DTO транзакции
   */
  public TransactionDTO toTransactionDTO(Transaction transaction) {
    if (transaction == null) return null;
    return new TransactionDTO(
        transaction.getId(),
        transaction.getUser().getId(),
        transaction.getType(),
        transaction.getCategory() != null ? transaction.getCategory().getId() : null,
        transaction.getCategory() != null ? transaction.getCategory().getName() : null,
        transaction.getCategory() != null ? transaction.getCategory().getIcon() : null,
        transaction.getCategory() != null ? transaction.getCategory().getColor() : null,
        transaction.getAccount().getId(),
        transaction.getAccount().getName(),
        transaction.getTransferAccount() != null ? transaction.getTransferAccount().getId() : null,
        transaction.getTransferAccount() != null
            ? transaction.getTransferAccount().getName()
            : null,
        transaction.getAmount(),
        transaction.getDescription(),
        transaction.getDate(),
        transaction.getCreatedAt(),
        transaction.getUpdatedAt());
  }
}
