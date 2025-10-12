package ru.nsu.spendsphere.services;

import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import ru.nsu.spendsphere.exceptions.ResourceNotFoundException;
import ru.nsu.spendsphere.models.dto.AccountBalanceDTO;
import ru.nsu.spendsphere.models.dto.AccountCreateDTO;
import ru.nsu.spendsphere.models.dto.AccountDTO;
import ru.nsu.spendsphere.models.dto.AccountUpdateDTO;
import ru.nsu.spendsphere.models.entities.Account;
import ru.nsu.spendsphere.models.entities.Currency;
import ru.nsu.spendsphere.models.entities.User;
import ru.nsu.spendsphere.models.mappers.AccountMapper;
import ru.nsu.spendsphere.repositories.AccountRepository;
import ru.nsu.spendsphere.repositories.UserRepository;

/**
 * Сервис для управления счетами пользователей. Предоставляет бизнес-логику для работы со счетами,
 * включая получение, создание, обновление и удаление.
 */
@Service
@RequiredArgsConstructor
public class AccountService {

  private final AccountRepository accountRepository;
  private final UserRepository userRepository;
  private final AccountMapper accountMapper;

  /**
   * Получение всех счетов пользователя.
   *
   * @param userId идентификатор пользователя
   * @return список DTO счетов пользователя
   * @throws ResourceNotFoundException если пользователь не найден
   */
  public List<AccountDTO> getUserAccounts(Long userId) {
    if (!userRepository.existsById(userId)) {
      throw new ResourceNotFoundException("User with id " + userId + " not found");
    }
    return accountRepository.findByUserId(userId).stream()
        .map(accountMapper::toAccountDTO)
        .collect(Collectors.toList());
  }

  /**
   * Получение счета по идентификатору.
   *
   * @param accountId идентификатор счета
   * @param userId идентификатор пользователя-владельца
   * @return DTO счета
   * @throws ResourceNotFoundException если счет не найден или не принадлежит пользователю
   */
  public AccountDTO getAccountById(Long accountId, Long userId) {
    if (!userRepository.existsById(userId)) {
      throw new ResourceNotFoundException("User with id " + userId + " not found");
    }
    Account account =
        accountRepository
            .findByIdAndUserId(accountId, userId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Account with id " + accountId + " not found for user " + userId));
    return accountMapper.toAccountDTO(account);
  }

  /**
   * Создание нового счета для пользователя.
   *
   * @param userId идентификатор пользователя
   * @param createDTO DTO с данными для создания счета
   * @return DTO созданного счета
   * @throws ResourceNotFoundException если пользователь не найден
   */
  @Transactional
  public AccountDTO createAccount(Long userId, AccountCreateDTO createDTO) {
    User user =
        userRepository
            .findById(userId)
            .orElseThrow(
                () -> new ResourceNotFoundException("User with id " + userId + " not found"));

    Account account =
        Account.builder()
            .user(user)
            .accountType(createDTO.accountType())
            .balance(createDTO.balance() != null ? createDTO.balance() : BigDecimal.ZERO)
            .currency(createDTO.currency() != null ? createDTO.currency() : Currency.RUB)
            .name(createDTO.name())
            .iconUrl(createDTO.iconUrl())
            .creditLimit(
                createDTO.creditLimit() != null ? createDTO.creditLimit() : BigDecimal.ZERO)
            .includeInTotal(createDTO.includeInTotal())
            .isActive(createDTO.isActive())
            .build();
    return accountMapper.toAccountDTO(accountRepository.save(account));
  }

  /**
   * Обновление существующего счета.
   *
   * @param accountId идентификатор счета
   * @param userId идентификатор пользователя-владельца
   * @param updateDTO DTO с данными для обновления
   * @return DTO обновленного счета
   * @throws ResourceNotFoundException если счет не найден или не принадлежит пользователю
   */
  @Transactional
  public AccountDTO updateAccount(Long accountId, Long userId, AccountUpdateDTO updateDTO) {
    if (!userRepository.existsById(userId)) {
      throw new ResourceNotFoundException("User with id " + userId + " not found");
    }
    Account account =
        accountRepository
            .findByIdAndUserId(accountId, userId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Account with id " + accountId + " not found for user " + userId));

    if (updateDTO.accountType() != null) {
      account.setAccountType(updateDTO.accountType());
    }
    if (updateDTO.balance() != null) {
      account.setBalance(updateDTO.balance());
    }
    if (updateDTO.currency() != null) {
      account.setCurrency(updateDTO.currency());
    }
    if (updateDTO.name() != null) {
      account.setName(updateDTO.name());
    }
    if (updateDTO.iconUrl() != null) {
      account.setIconUrl(updateDTO.iconUrl());
    }
    if (updateDTO.creditLimit() != null) {
      account.setCreditLimit(updateDTO.creditLimit());
    }
    if (updateDTO.isActive() != null) {
      account.setIsActive(updateDTO.isActive());
    }
    if (updateDTO.includeInTotal() != null) {
      account.setIncludeInTotal(updateDTO.includeInTotal());
    }

    return accountMapper.toAccountDTO(accountRepository.save(account));
  }

  /**
   * Удаление счета пользователя.
   *
   * @param accountId идентификатор счета
   * @param userId идентификатор пользователя-владельца
   * @throws ResourceNotFoundException если счет не найден или не принадлежит пользователю
   */
  @Transactional
  public void deleteAccount(Long accountId, Long userId) {
    if (!userRepository.existsById(userId)) {
      throw new ResourceNotFoundException("User with id " + userId + " not found");
    }
    Account account =
        accountRepository
            .findByIdAndUserId(accountId, userId)
            .orElseThrow(
                () ->
                    new ResourceNotFoundException(
                        "Account with id " + accountId + " not found for user " + userId));
    accountRepository.delete(account);
  }

  /**
   * Получение общего баланса по всем активным счетам пользователя, сгруппированного по валютам.
   *
   * @param userId идентификатор пользователя
   * @return DTO с информацией о балансах по валютам
   * @throws ResourceNotFoundException если пользователь не найден
   */
  public AccountBalanceDTO getUserAccountsBalance(Long userId) {
    if (!userRepository.existsById(userId)) {
      throw new ResourceNotFoundException("User with id " + userId + " not found");
    }

    List<Account> activeAccounts =
        accountRepository.findByUserId(userId).stream()
            .filter(Account::getIsActive)
            .filter(Account::getIncludeInTotal)
            .toList();

    Map<Currency, BigDecimal> balancesByCurrency = new HashMap<>();

    for (Account account : activeAccounts) {
      Currency currency = account.getCurrency();
      BigDecimal currentBalance = balancesByCurrency.getOrDefault(currency, BigDecimal.ZERO);
      balancesByCurrency.put(currency, currentBalance.add(account.getBalance()));
    }

    return new AccountBalanceDTO(activeAccounts.size(), balancesByCurrency);
  }
}
