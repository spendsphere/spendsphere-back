package ru.nsu.spendsphere.services;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.spendsphere.exceptions.BadRequestException;
import ru.nsu.spendsphere.exceptions.ResourceNotFoundException;
import ru.nsu.spendsphere.models.dto.TransactionCreateDTO;
import ru.nsu.spendsphere.models.dto.TransactionDTO;
import ru.nsu.spendsphere.models.dto.TransactionUpdateDTO;
import ru.nsu.spendsphere.models.entities.Account;
import ru.nsu.spendsphere.models.entities.Category;
import ru.nsu.spendsphere.models.entities.Transaction;
import ru.nsu.spendsphere.models.entities.TransactionType;
import ru.nsu.spendsphere.models.entities.User;
import ru.nsu.spendsphere.models.mappers.TransactionMapper;
import ru.nsu.spendsphere.repositories.AccountRepository;
import ru.nsu.spendsphere.repositories.CategoryRepository;
import ru.nsu.spendsphere.repositories.TransactionRepository;
import ru.nsu.spendsphere.repositories.UserRepository;

/**
 * Сервис для управления транзакциями пользователей. Предоставляет бизнес-логику для работы с
 * транзакциями, включая получение, создание, обновление и удаление.
 */
@Service
@RequiredArgsConstructor
public class TransactionService {

  private final TransactionRepository transactionRepository;
  private final UserRepository userRepository;
  private final AccountRepository accountRepository;
  private final CategoryRepository categoryRepository;
  private final TransactionMapper transactionMapper;

  /**
   * Получение всех транзакций пользователя.
   *
   * @param userId идентификатор пользователя
   * @return список DTO транзакций пользователя
   * @throws ResourceNotFoundException если пользователь не найден
   */
  public List<TransactionDTO> getAllTransactions(Long userId) {
    if (!userRepository.existsById(userId)) {
      throw new ResourceNotFoundException("User with id " + userId + " not found");
    }
    return transactionRepository.findByUserIdOrderByDateDescCreatedAtDesc(userId).stream()
        .map(transactionMapper::toTransactionDTO)
        .collect(Collectors.toList());
  }

  /**
   * Получение транзакции по идентификатору.
   *
   * @param transactionId идентификатор транзакции
   * @param userId идентификатор пользователя-владельца
   * @return DTO транзакции
   * @throws ResourceNotFoundException если транзакция не найдена или не принадлежит пользователю
   */
  public TransactionDTO getTransactionById(Long transactionId, Long userId) {
    if (!userRepository.existsById(userId)) {
      throw new ResourceNotFoundException("User with id " + userId + " not found");
    }
    Transaction transaction =
        transactionRepository
            .findByIdAndUserId(transactionId, userId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Transaction with id " + transactionId + " not found for user " + userId));
    return transactionMapper.toTransactionDTO(transaction);
  }

  /**
   * Получение транзакций пользователя с фильтрами.
   *
   * @param userId идентификатор пользователя
   * @param type тип транзакции (опционально)
   * @param accountId идентификатор счета (опционально)
   * @param categoryId идентификатор категории (опционально)
   * @param dateFrom дата начала периода (опционально)
   * @param dateTo дата окончания периода (опционально)
   * @return список DTO транзакций
   * @throws ResourceNotFoundException если пользователь не найден
   */
  public List<TransactionDTO> getTransactionsWithFilters(
      Long userId,
      TransactionType type,
      Long accountId,
      Long categoryId,
      LocalDate dateFrom,
      LocalDate dateTo) {
    if (!userRepository.existsById(userId)) {
      throw new ResourceNotFoundException("User with id " + userId + " not found");
    }
    return transactionRepository
        .findByUserIdWithFilters(userId, type, accountId, categoryId, dateFrom, dateTo)
        .stream()
        .map(transactionMapper::toTransactionDTO)
        .collect(Collectors.toList());
  }

  /**
   * Создание новой транзакции.
   *
   * @param userId идентификатор пользователя
   * @param createDTO DTO с данными для создания транзакции
   * @return DTO созданной транзакции
   * @throws ResourceNotFoundException если пользователь, счет или категория не найдены
   * @throws BadRequestException если данные некорректны
   */
  @Transactional
  public TransactionDTO createTransaction(Long userId, TransactionCreateDTO createDTO) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> new ResourceNotFoundException("User with id " + userId + " not found"));

    Account account =
        accountRepository
            .findByIdAndUserId(createDTO.accountId(), userId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Account with id "
                            + createDTO.accountId()
                            + " not found for user "
                            + userId));

    Account transferAccount = null;
    if (createDTO.type() == TransactionType.TRANSFER) {
      if (createDTO.transferAccountId() == null) {
        throw new BadRequestException("Transfer account is required for TRANSFER transaction");
      }
      if (createDTO.accountId().equals(createDTO.transferAccountId())) {
        throw new BadRequestException("Transfer account must be different from source account");
      }
      transferAccount =
          accountRepository
              .findByIdAndUserId(createDTO.transferAccountId(), userId)
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          "Transfer account with id "
                              + createDTO.transferAccountId()
                              + " not found for user "
                              + userId));
    }

    Category category = null;
    if (createDTO.categoryId() != null) {
      category =
          categoryRepository
              .findByIdAndIsDefaultTrueOrUserId(createDTO.categoryId(), userId)
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          "Category with id " + createDTO.categoryId() + " not found"));
    }

    validateSufficientFunds(account, createDTO.type(), createDTO.amount());

    Transaction transaction =
        Transaction.builder()
            .user(user)
            .type(createDTO.type())
            .category(category)
            .account(account)
            .transferAccount(transferAccount)
            .amount(createDTO.amount())
            .description(createDTO.description())
            .date(createDTO.date())
            .build();

    Transaction savedTransaction = transactionRepository.save(transaction);

    applyTransactionToBalance(account, transferAccount, createDTO.type(), createDTO.amount());
    accountRepository.save(account);
    if (transferAccount != null) {
      accountRepository.save(transferAccount);
    }

    return transactionMapper.toTransactionDTO(savedTransaction);
  }

  /**
   * Обновление существующей транзакции.
   *
   * @param transactionId идентификатор транзакции
   * @param userId идентификатор пользователя-владельца
   * @param updateDTO DTO с данными для обновления
   * @return DTO обновленной транзакции
   * @throws ResourceNotFoundException если транзакция, счет или категория не найдены
   * @throws BadRequestException если данные некорректны
   */
  @Transactional
  public TransactionDTO updateTransaction(
      Long transactionId, Long userId, TransactionUpdateDTO updateDTO) {
    if (!userRepository.existsById(userId)) {
      throw new ResourceNotFoundException("User with id " + userId + " not found");
    }

    Transaction transaction =
        transactionRepository
            .findByIdAndUserId(transactionId, userId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Transaction with id " + transactionId + " not found for user " + userId));

    TransactionType oldType = transaction.getType();
    BigDecimal oldAmount = transaction.getAmount();
    Account oldAccount = transaction.getAccount();
    Account oldTransferAccount = transaction.getTransferAccount();

    revertTransactionFromBalance(oldAccount, oldTransferAccount, oldType, oldAmount);

    updateTransactionFields(transaction, userId, updateDTO);

    validateSufficientFunds(
        transaction.getAccount(), transaction.getType(), transaction.getAmount());

    applyTransactionToBalance(
        transaction.getAccount(),
        transaction.getTransferAccount(),
        transaction.getType(),
        transaction.getAmount());

    saveUpdatedAccounts(oldAccount, oldTransferAccount, transaction);

    return transactionMapper.toTransactionDTO(transactionRepository.save(transaction));
  }

  /**
   * Обновляет поля транзакции согласно DTO.
   *
   * @param transaction транзакция для обновления
   * @param userId идентификатор пользователя
   * @param updateDTO DTO с данными для обновления
   */
  private void updateTransactionFields(
      Transaction transaction, Long userId, TransactionUpdateDTO updateDTO) {
    if (updateDTO.type() != null) {
      transaction.setType(updateDTO.type());
    }

    if (updateDTO.accountId() != null) {
      Account account =
          accountRepository
              .findByIdAndUserId(updateDTO.accountId(), userId)
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          "Account with id "
                              + updateDTO.accountId()
                              + " not found for user "
                              + userId));
      transaction.setAccount(account);
    }

    if (updateDTO.transferAccountId() != null) {
      if (transaction.getType() != TransactionType.TRANSFER
          && updateDTO.type() != TransactionType.TRANSFER) {
        throw new BadRequestException("Transfer account can only be set for TRANSFER transactions");
      }
      Account transferAccount =
          accountRepository
              .findByIdAndUserId(updateDTO.transferAccountId(), userId)
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          "Transfer account with id "
                              + updateDTO.transferAccountId()
                              + " not found for user "
                              + userId));
      transaction.setTransferAccount(transferAccount);
    }

    if (updateDTO.categoryId() != null) {
      Category category =
          categoryRepository
              .findByIdAndIsDefaultTrueOrUserId(updateDTO.categoryId(), userId)
              .orElseThrow(
                  () ->
                      new ResourceNotFoundException(
                          "Category with id " + updateDTO.categoryId() + " not found"));
      transaction.setCategory(category);
    }

    if (updateDTO.amount() != null) {
      transaction.setAmount(updateDTO.amount());
    }

    if (updateDTO.description() != null) {
      transaction.setDescription(updateDTO.description());
    }

    if (updateDTO.date() != null) {
      transaction.setDate(updateDTO.date());
    }
  }

  /**
   * Сохраняет обновленные счета после изменения транзакции.
   *
   * @param oldAccount старый основной счет
   * @param oldTransferAccount старый счет для перевода
   * @param transaction обновленная транзакция
   */
  private void saveUpdatedAccounts(
      Account oldAccount, Account oldTransferAccount, Transaction transaction) {
    accountRepository.save(oldAccount);
    if (oldTransferAccount != null) {
      accountRepository.save(oldTransferAccount);
    }
    if (!oldAccount.equals(transaction.getAccount())) {
      accountRepository.save(transaction.getAccount());
    }
    if (transaction.getTransferAccount() != null
        && !transaction.getTransferAccount().equals(oldTransferAccount)) {
      accountRepository.save(transaction.getTransferAccount());
    }
  }

  /**
   * Удаление транзакции.
   *
   * @param transactionId идентификатор транзакции
   * @param userId идентификатор пользователя-владельца
   * @throws ResourceNotFoundException если транзакция не найдена или не принадлежит пользователю
   */
  @Transactional
  public void deleteTransaction(Long transactionId, Long userId) {
    if (!userRepository.existsById(userId)) {
      throw new ResourceNotFoundException("User with id " + userId + " not found");
    }
    Transaction transaction =
        transactionRepository
            .findByIdAndUserId(transactionId, userId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Transaction with id " + transactionId + " not found for user " + userId));

    revertTransactionFromBalance(
        transaction.getAccount(),
        transaction.getTransferAccount(),
        transaction.getType(),
        transaction.getAmount());

    accountRepository.save(transaction.getAccount());
    if (transaction.getTransferAccount() != null) {
      accountRepository.save(transaction.getTransferAccount());
    }

    transactionRepository.delete(transaction);
  }

  /**
   * Применяет изменения транзакции к балансу счетов.
   *
   * @param account основной счет
   * @param transferAccount счет для перевода (может быть null)
   * @param type тип транзакции
   * @param amount сумма транзакции
   */
  private void applyTransactionToBalance(
      Account account, Account transferAccount, TransactionType type, BigDecimal amount) {
    switch (type) {
      case INCOME:
        account.setBalance(account.getBalance().add(amount));
        break;
      case EXPENSE:
        account.setBalance(account.getBalance().subtract(amount));
        break;
      case TRANSFER:
        account.setBalance(account.getBalance().subtract(amount));
        if (transferAccount != null) {
          transferAccount.setBalance(transferAccount.getBalance().add(amount));
        }
        break;
    }
  }

  /**
   * Откатывает изменения транзакции из баланса счетов.
   *
   * @param account основной счет
   * @param transferAccount счет для перевода (может быть null)
   * @param type тип транзакции
   * @param amount сумма транзакции
   */
  private void revertTransactionFromBalance(
      Account account, Account transferAccount, TransactionType type, BigDecimal amount) {
    switch (type) {
      case INCOME:
        account.setBalance(account.getBalance().subtract(amount));
        break;
      case EXPENSE:
        account.setBalance(account.getBalance().add(amount));
        break;
      case TRANSFER:
        account.setBalance(account.getBalance().add(amount));
        if (transferAccount != null) {
          transferAccount.setBalance(transferAccount.getBalance().subtract(amount));
        }
        break;
    }
  }

  /**
   * Проверяет достаточность средств на счете для выполнения транзакции.
   *
   * @param account основной счет
   * @param type тип транзакции
   * @param amount сумма транзакции
   * @throws BadRequestException если недостаточно средств
   */
  private void validateSufficientFunds(Account account, TransactionType type, BigDecimal amount) {
    if ((type == TransactionType.EXPENSE || type == TransactionType.TRANSFER)
        && (account.getBalance().compareTo(amount) < 0))
      throw new BadRequestException(
          "Insufficient funds on account "
              + account.getName()
              + ". Available: "
              + account.getBalance()
              + ", required: "
              + amount);
  }
}
