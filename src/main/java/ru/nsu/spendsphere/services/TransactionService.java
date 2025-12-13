package ru.nsu.spendsphere.services;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.spendsphere.exceptions.BadRequestException;
import ru.nsu.spendsphere.exceptions.ResourceNotFoundException;
import ru.nsu.spendsphere.models.dto.TransactionCreateDTO;
import ru.nsu.spendsphere.models.dto.TransactionDTO;
import ru.nsu.spendsphere.models.dto.TransactionStatisticsDTO;
import ru.nsu.spendsphere.models.dto.TransactionStatisticsDTO.CategoryTimeSeriesDTO;
import ru.nsu.spendsphere.models.dto.TransactionStatisticsDTO.MaxExpensePerCategoryDTO;
import ru.nsu.spendsphere.models.dto.TransactionStatisticsDTO.MaxExpensePerDayDTO;
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
   * Получение статистики транзакций за период.
   *
   * @param userId идентификатор пользователя
   * @param months количество месяцев для анализа (1, 3, 6 или 12)
   * @return DTO со статистикой транзакций
   * @throws ResourceNotFoundException если пользователь не найден
   * @throws BadRequestException если указано некорректное количество месяцев
   */
  public TransactionStatisticsDTO getTransactionStatistics(Long userId, Integer months) {
    if (!userRepository.existsById(userId)) {
      throw new ResourceNotFoundException("User with id " + userId + " not found");
    }

    if (months == null || (months != 1 && months != 3 && months != 6 && months != 12)) {
      throw new BadRequestException("Months parameter must be 1, 3, 6, or 12");
    }

    LocalDate endDate = LocalDate.now();
    LocalDate startDate = endDate.minusMonths(months);

    List<Transaction> transactions =
        transactionRepository.findByUserIdWithFilters(
            userId, null, null, null, startDate, endDate);

    return buildStatistics(transactions, startDate, endDate);
  }

  /**
   * Построение статистики на основе списка транзакций.
   *
   * @param transactions список транзакций
   * @param startDate дата начала периода
   * @param endDate дата окончания периода
   * @return DTO со статистикой
   */
  private TransactionStatisticsDTO buildStatistics(
      List<Transaction> transactions, LocalDate startDate, LocalDate endDate) {

    List<Transaction> expenses =
        transactions.stream()
            .filter(t -> t.getType() == TransactionType.EXPENSE)
            .collect(Collectors.toList());

    List<Transaction> incomes =
        transactions.stream()
            .filter(t -> t.getType() == TransactionType.INCOME)
            .collect(Collectors.toList());

    Map<String, BigDecimal> expensesByCategory = calculateExpensesByCategory(expenses);
    Map<String, BigDecimal> incomeByCategory = calculateIncomeByCategory(incomes);

    Map<String, BigDecimal> monthlyExpenses = calculateMonthlyExpenses(expenses);
    Map<String, BigDecimal> monthlyIncome = calculateMonthlyIncome(incomes);

    List<CategoryTimeSeriesDTO> avgExpensesByCategory = calculateAvgByCategory(expenses);
    List<CategoryTimeSeriesDTO> avgIncomeByCategory = calculateAvgByCategory(incomes);

    MaxExpensePerDayDTO maxExpensePerDay = calculateMaxExpensePerDay(expenses);
    MaxExpensePerCategoryDTO maxExpensePerCategory = calculateMaxExpensePerCategory(expenses);

    BigDecimal averageExpense = calculateAverage(expenses);
    BigDecimal averageIncome = calculateAverage(incomes);

    return new TransactionStatisticsDTO(
        expensesByCategory,
        incomeByCategory,
        monthlyExpenses,
        monthlyIncome,
        avgExpensesByCategory,
        avgIncomeByCategory,
        maxExpensePerDay,
        maxExpensePerCategory,
        averageExpense,
        averageIncome,
        startDate,
        endDate);
  }

  /**
   * Вычисляет расходы по категориям.
   *
   * @param expenses список расходных транзакций
   * @return Map с названиями категорий и суммами расходов
   */
  private Map<String, BigDecimal> calculateExpensesByCategory(List<Transaction> expenses) {
    return expenses.stream()
        .filter(t -> t.getCategory() != null)
        .collect(
            Collectors.groupingBy(
                t -> t.getCategory().getName(),
                Collectors.reducing(
                    BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)));
  }

  /**
   * Вычисляет доходы по категориям.
   *
   * @param incomes список доходных транзакций
   * @return Map с названиями категорий и суммами доходов
   */
  private Map<String, BigDecimal> calculateIncomeByCategory(List<Transaction> incomes) {
    return incomes.stream()
        .filter(t -> t.getCategory() != null)
        .collect(
            Collectors.groupingBy(
                t -> t.getCategory().getName(),
                Collectors.reducing(
                    BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)));
  }

  /**
   * Вычисляет суммы расходов по месяцам.
   *
   * @param expenses список расходных транзакций
   * @return Map с месяцами (год-месяц) и суммами расходов
   */
  private Map<String, BigDecimal> calculateMonthlyExpenses(List<Transaction> expenses) {
    Map<String, BigDecimal> result =
        expenses.stream()
            .collect(
                Collectors.groupingBy(
                    t -> YearMonth.from(t.getDate()).format(DateTimeFormatter.ofPattern("yyyy-MM")),
                    Collectors.reducing(
                        BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)));

    return result.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .collect(
            Collectors.toMap(
                Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
  }

  /**
   * Вычисляет суммы доходов по месяцам.
   *
   * @param incomes список доходных транзакций
   * @return Map с месяцами (год-месяц) и суммами доходов
   */
  private Map<String, BigDecimal> calculateMonthlyIncome(List<Transaction> incomes) {
    Map<String, BigDecimal> result =
        incomes.stream()
            .collect(
                Collectors.groupingBy(
                    t -> YearMonth.from(t.getDate()).format(DateTimeFormatter.ofPattern("yyyy-MM")),
                    Collectors.reducing(
                        BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)));

    return result.entrySet().stream()
        .sorted(Map.Entry.comparingByKey())
        .collect(
            Collectors.toMap(
                Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));
  }

  /**
   * Вычисляет средние значения по категориям во времени.
   *
   * @param transactions список транзакций
   * @return список временных рядов по категориям
   */
  private List<CategoryTimeSeriesDTO> calculateAvgByCategory(List<Transaction> transactions) {

    Map<String, Map<String, List<BigDecimal>>> categoryMonthlyData =
        transactions.stream()
            .filter(t -> t.getCategory() != null)
            .collect(
                Collectors.groupingBy(
                    t -> t.getCategory().getName(),
                    Collectors.groupingBy(
                        t ->
                            YearMonth.from(t.getDate())
                                .format(DateTimeFormatter.ofPattern("yyyy-MM")),
                        Collectors.mapping(Transaction::getAmount, Collectors.toList()))));

    List<CategoryTimeSeriesDTO> result = new ArrayList<>();
    for (Map.Entry<String, Map<String, List<BigDecimal>>> entry :
        categoryMonthlyData.entrySet()) {
      String categoryName = entry.getKey();
      Map<String, BigDecimal> timeSeries = new LinkedHashMap<>();

      for (Map.Entry<String, List<BigDecimal>> monthEntry : entry.getValue().entrySet()) {
        String month = monthEntry.getKey();
        List<BigDecimal> amounts = monthEntry.getValue();
        BigDecimal average =
            amounts.stream()
                .reduce(BigDecimal.ZERO, BigDecimal::add)
                .divide(BigDecimal.valueOf(amounts.size()), 2, RoundingMode.HALF_UP);
        timeSeries.put(month, average);
      }

      Map<String, BigDecimal> sortedTimeSeries =
          timeSeries.entrySet().stream()
              .sorted(Map.Entry.comparingByKey())
              .collect(
                  Collectors.toMap(
                      Map.Entry::getKey, Map.Entry::getValue, (e1, e2) -> e1, LinkedHashMap::new));

      result.add(new CategoryTimeSeriesDTO(categoryName, sortedTimeSeries));
    }

    return result;
  }

  /**
   * Находит максимальный расход за день.
   *
   * @param expenses список расходных транзакций
   * @return DTO с информацией о максимальном расходе за день
   */
  private MaxExpensePerDayDTO calculateMaxExpensePerDay(List<Transaction> expenses) {
    if (expenses.isEmpty()) {
      return null;
    }

    Map<LocalDate, BigDecimal> dailyExpenses =
        expenses.stream()
            .collect(
                Collectors.groupingBy(
                    Transaction::getDate,
                    Collectors.reducing(
                        BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)));

    Map.Entry<LocalDate, BigDecimal> maxEntry =
        dailyExpenses.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .orElse(null);

    if (maxEntry == null) {
      return null;
    }

    return new MaxExpensePerDayDTO(maxEntry.getKey(), maxEntry.getValue());
  }

  /**
   * Находит максимальный расход по категории.
   *
   * @param expenses список расходных транзакций
   * @return DTO с информацией о максимальном расходе по категории
   */
  private MaxExpensePerCategoryDTO calculateMaxExpensePerCategory(List<Transaction> expenses) {
    if (expenses.isEmpty()) {
      return null;
    }

    Map<String, BigDecimal> categoryExpenses =
        expenses.stream()
            .filter(t -> t.getCategory() != null)
            .collect(
                Collectors.groupingBy(
                    t -> t.getCategory().getName(),
                    Collectors.reducing(
                        BigDecimal.ZERO, Transaction::getAmount, BigDecimal::add)));

    Map.Entry<String, BigDecimal> maxEntry =
        categoryExpenses.entrySet().stream()
            .max(Map.Entry.comparingByValue())
            .orElse(null);

    if (maxEntry == null) {
      return null;
    }

    return new MaxExpensePerCategoryDTO(maxEntry.getKey(), maxEntry.getValue());
  }

  /**
   * Вычисляет среднее значение транзакций.
   *
   * @param transactions список транзакций
   * @return среднее значение
   */
  private BigDecimal calculateAverage(List<Transaction> transactions) {
    if (transactions.isEmpty()) {
      return BigDecimal.ZERO;
    }

    BigDecimal sum =
        transactions.stream()
            .map(Transaction::getAmount)
            .reduce(BigDecimal.ZERO, BigDecimal::add);

    return sum.divide(BigDecimal.valueOf(transactions.size()), 2, RoundingMode.HALF_UP);
  }
}
